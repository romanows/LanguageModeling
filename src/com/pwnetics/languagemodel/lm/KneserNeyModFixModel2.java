/*
Copyright 2011 Brian Romanowski. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY BRIAN ROMANOWSKI ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BRIAN ROMANOWSKI OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors.
*/


package com.pwnetics.languagemodel.lm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.pwnetics.helper.ItemCounter;
import com.pwnetics.languagemodel.ngram.AbstractNGram;


/**
 * Kneser-Ney language model implemented according to Chen and Goodman's kneser-ney-mod-fix description,
 * see the Harvard technical report: S. Chen and J. Goodman, An empirical study of smoothing techniques for language modeling (Harvard, 1998).
 *
 * Kneser-ney-mod-fix differs from kneser-ney-mod (the best performing model tested) in that it does not
 * optimize the lambda interpolation weights on a held-out dataset.  Rather, it calculates them according
 * to what they theoretically should be.
 *
 * @author romanows
 */
public class KneserNeyModFixModel2 extends AbstractNGramLanguageModel {

	/** NGram counts used to estimate smoothed KN probabilities */
	private List<ItemCounter<AbstractNGram>> orderToNGramCounter;

	/** Number of unigrams in training data */
	private int sumUnigrams;

	/** D_n(1); the discount D for ngram of order n that appears 1 time in the training data */
	private final double [] d1;

	/** D_n(2); the discount D for ngram of order n that appears 2 times in the training data */
	private final double [] d2;

	/** D_n(3+); the discount D for ngram of order n that appears 3+ times in the training data */
	private final double [] d3p;

	/** Maps histories to ngrams that have those histories.  Speed up finding ngrams with common histories */
	private Map<AbstractNGram,List<AbstractNGram>> historyToNGramMap;

	/** Caches the most recently used constants for ngram histories.  Significant speed up. */
	private LinkedHashMap<AbstractNGram, CachedIntermediateValues> historyToIntermediateValueCache;


	/** Container for caching KN constants in {@link KneserNeyModFixModel2#historyToIntermediateValueCache} */
	private static class CachedIntermediateValues {
		/** Number of unique words that appear [once, twice, three-or-more-times] after the history in the training data */
		public int [] Nc;

		/** Denominator for the first and gamma terms in the KN equation */
		public int den;
	}


	/** Standard implementation of a LRU cache */
	private static class ConstantCacheQueue<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;
		private final int maxEntries;

		public ConstantCacheQueue(int maxEntries) {
			super(maxEntries, 0.75f, true);
			this.maxEntries = maxEntries;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > maxEntries;
		}
	}


	/**
	 * Constructor.
	 * @param order model order
	 * @param logBase base of logarithm in which probabilities are reported
	 * @param orderToNGramCounter ngram order to counts of ngrams; the raw data for log probability estimates
	 */
	public KneserNeyModFixModel2(int order, double logBase, List<ItemCounter<AbstractNGram>> orderToNGramCounter) {
		super(order, logBase);
		if(order > orderToNGramCounter.size()) {
			throw new IllegalArgumentException("requested language model order is too large for supplied ngram counts");
		}
		this.orderToNGramCounter = orderToNGramCounter;
		sumUnigrams = (int) orderToNGramCounter.get(0).sum();
		historyToIntermediateValueCache = new ConstantCacheQueue<AbstractNGram, KneserNeyModFixModel2.CachedIntermediateValues>((int)Math.pow(2,14));

		// Calculate D's
		d1 = new double[order];
		d2 = new double[order];
		d3p = new double[order];
		for(int i=0; i<orderToNGramCounter.size(); i++) {
			ItemCounter<Integer> countOfCounts = orderToNGramCounter.get(i).countOfCounts();
			int n1 = countOfCounts.get(1);
			int n2 = countOfCounts.get(2);
			int n3 = countOfCounts.get(3);
			int n4 = countOfCounts.get(4);

			d1[i] = 1.0 - ((2.0 * n1 * n2) / ((n1+2.0*n2) * n1));
			d2[i] = 2.0 - ((3.0 * n1 * n3) / ((n1+2.0*n2) * n2));
			d3p[i] = 3.0 - ((4.0 * n1 * n4) / ((n1+2.0*n2) * n3));
		}

		// Calculate history map
		historyToNGramMap = new HashMap<AbstractNGram, List<AbstractNGram>>();
		for(int i=1; i<orderToNGramCounter.size(); i++) {
			ItemCounter<AbstractNGram> ngramCounter = orderToNGramCounter.get(i);
			for(AbstractNGram ngram : ngramCounter.getItems()) {
				AbstractNGram history = ngram.history();
				List<AbstractNGram> ngramList = historyToNGramMap.get(history);
				if(ngramList == null) {
					ngramList = new ArrayList<AbstractNGram>();
					historyToNGramMap.put(history, ngramList);
				}
				ngramList.add(ngram);
			}
		}
	}


	/**
	 * Helper function to get the discount factor for a given order of ngram and a given observed count.
	 * @param n order of ngram
	 * @param c number of times the ngram appears in the training data
	 * @return Kneser-Ney absolute discounting factor
	 */
	private double getD(int n, int c) {
		switch (c) {
		case 0:
			return 0.0;
		case 1:
			return d1[n-1];
		case 2:
			return d2[n-1];
		default:
			return d3p[n-1];
		}
	}


	/**
	 * Calculate the gamma normalization factor.
	 * @param history history of the ngram whose second KN term is being calculated
	 * @param den denominator of the gamma factor
	 * @param Nc number of unique words that appear [once, twice, three-or-more-times] after the history in the training data
	 * @return the gamma normalization factor
	 */
	private double calcGamma(AbstractNGram history, int den, int [] Nc) {
		double gamma = getD(history.size()+1, 1) * Nc[0];
		gamma += getD(history.size()+1, 2) * Nc[1];
		gamma += getD(history.size()+1, 3) * Nc[2];
		return gamma / den;
	}


	/**
	 * Calculate the first term in the KN equation.
	 * @param ngram
	 * @param den denominator of the first term
	 * @return the first term in the KN equation
	 */
	private Double calcNGramProbability(AbstractNGram ngram, int den) {
		if(ngram.size() == 1) {
			Integer count = orderToNGramCounter.get(0).get(ngram);
			return count / (double) sumUnigrams;
		}

		Integer count = orderToNGramCounter.get(ngram.size()-1).get(ngram);
		if(count == 0) {
			return 0.0;
		}
		return (count - getD(ngram.size(), count)) / den;
	}


	private CachedIntermediateValues getIntermediateValues(AbstractNGram history) {
		CachedIntermediateValues cc = historyToIntermediateValueCache.get(history);
		if(cc == null) {
			// Calculate denominator and Nc's
			cc = new CachedIntermediateValues();
			cc.den = 0;
			cc.Nc = new int[3];
			if(historyToNGramMap.containsKey(history)) {
				ItemCounter<AbstractNGram> ngramCounter = orderToNGramCounter.get(history.size());
				for(AbstractNGram ng : historyToNGramMap.get(history)) {
					cc.den += ngramCounter.get(ng);
					switch (ngramCounter.get(ng)) {
					case 0:
						break;
					case 1:
						cc.Nc[0]++;
						break;
					case 2:
						cc.Nc[1]++;
						break;
					default:
						cc.Nc[2]++;
						break;
					}
				}
			}
			historyToIntermediateValueCache.put(history, cc);
		}
		return cc;
	}


	/**
	 * Perform Chen and Goodman's recursive interpolation calculation of kneser-ney-mod.
	 * @param ngram
	 * @return the estimated/smoothed ngram probability
	 */
	private Double recurseNGramProbability(AbstractNGram ngram) {
		if(ngram.size() == 1) {
			return calcNGramProbability(ngram, 0);  // the den is not used in this case
		}

		AbstractNGram history = ngram.history();
		if(historyToNGramMap.containsKey(history)) {
			CachedIntermediateValues cc = getIntermediateValues(history);
			return calcNGramProbability(ngram,cc.den) + calcGamma(history, cc.den, cc.Nc)*recurseNGramProbability(ngram.backoff());
		} else {
			return recurseNGramProbability(ngram.backoff());
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.lm.AbstractNGramLanguageModel#getVocabulary()
	 */
	@Override
	public Set<String> getVocabulary() {
		Set<String> wordSet = new HashSet<String>();
		for(AbstractNGram u : orderToNGramCounter.get(0).getItems()) {
			wordSet.add(u.getFirst());
		}
		return wordSet;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.lm.AbstractNGramLanguageModel#logProbability(com.pwnetics.languagemodel.ngram.AbstractNGram)
	 */
	@Override
	public double logProbability(AbstractNGram ngram) {
		return log(recurseNGramProbability(ngram));
	}


	public BackoffLanguageModel calcBackoff() {
		// Unigrams backoffs are the same as the interpolated version
		if(order == 1) {
			Map<AbstractNGram, NGramProbability> highOrderNGrams = new HashMap<AbstractNGram, NGramProbability>();
			for(AbstractNGram ngram : orderToNGramCounter.get(0).getItems()) {
				highOrderNGrams.put(ngram, new NGramProbability(log(calcNGramProbability(ngram, 0))));  // the den is not used in this case
			}
			return new BackoffLanguageModel(logBase, highOrderNGrams, null);
		}

		// Higher order backoff models share the same probabilities where there are counts in our model
		List<Map<AbstractNGram, NGramProbabilityBackoff>> lowerOrderToNGrams = new ArrayList<Map<AbstractNGram,NGramProbabilityBackoff>>();
		for(int i=0; i<order-1; i++) {
			Map<AbstractNGram, NGramProbabilityBackoff> ngramToPB = new HashMap<AbstractNGram, NGramProbabilityBackoff>();
			for(AbstractNGram ngram : orderToNGramCounter.get(i).getItems()) {
				ngramToPB.put(ngram, new NGramProbabilityBackoff(recurseNGramProbability(ngram)));
			}
			lowerOrderToNGrams.add(ngramToPB);
		}

		Map<AbstractNGram, NGramProbability> highOrderNGrams = new HashMap<AbstractNGram, NGramProbability>();
		for(AbstractNGram ngram : orderToNGramCounter.get(order-1).getItems()) {
			highOrderNGrams.put(ngram, new NGramProbability(recurseNGramProbability(ngram)));
		}

		// Calculate backoff weights
		for(int i=1; i<order-1; i++) {
			for(AbstractNGram ngram : orderToNGramCounter.get(i).getItems()) {
				AbstractNGram history = ngram.history();
				NGramProbabilityBackoff ngpb = lowerOrderToNGrams.get(i-1).get(history);
				if(!Double.isNaN(ngpb.backoff)) {
					continue;
				}

				double probLeftover = 1.0;
				for(AbstractNGram ngramWithHistory : historyToNGramMap.get(history)) {
					probLeftover -= lowerOrderToNGrams.get(i).get(ngramWithHistory).probability;
				}

				double probToDistribute = 0.0;
				List<AbstractNGram> expandHistory = historyToNGramMap.get(history);
				Collection<AbstractNGram> unigrams = lowerOrderToNGrams.get(0).keySet();
				// Add up all in the expandHistoryBackoff that don't have the same word in the last word as anything in expandHistory
				for(AbstractNGram unigram : unigrams) {
					boolean toAdd = true;
					for(AbstractNGram expandHistoryNGram : expandHistory) {
						if(unigram.getLast().equals(expandHistoryNGram.getLast())) {
							toAdd = false;
							break;
						}
					}
					if(toAdd) {
						if(history.size() == 1) {
							probToDistribute += recurseNGramProbability(unigram);
						} else {
							probToDistribute += recurseNGramProbability(history.backoff().add(unigram.getFirst()));
						}
					}
				}

				if(probToDistribute == 0.0) {
					ngpb.backoff = 0.0;
				} else {
					ngpb.backoff = probLeftover/probToDistribute;
				}
			}
		}

		for(AbstractNGram ngram : orderToNGramCounter.get(order-1).getItems()) {
			AbstractNGram history = ngram.history();
			NGramProbabilityBackoff ngpb = lowerOrderToNGrams.get(order-2).get(history);
			if(!Double.isNaN(ngpb.backoff)) {
				continue;
			}
			double probLeftover = 1.0;
			for(AbstractNGram ngramWithHistory : historyToNGramMap.get(history)) {
				probLeftover -= highOrderNGrams.get(ngramWithHistory).probability;
			}

			double probToDistribute = 0.0;
			HashSet<AbstractNGram> expandHistory = new HashSet<AbstractNGram>(historyToNGramMap.get(history));
			Collection<AbstractNGram> unigrams = lowerOrderToNGrams.get(0).keySet();
			// Add up all in the expandHistoryBackoff that don't have the same word in the last word as anything in expandHistory
			for(AbstractNGram unigram : unigrams) {
				boolean toAdd = true;
				for(AbstractNGram expandHistoryNGram : expandHistory) {
					if(unigram.getLast().equals(expandHistoryNGram.getLast())) {
						expandHistory.remove(expandHistoryNGram);
						toAdd = false;
						break;
					}
				}
				if(toAdd) {
					if(history.size() == 1) {
						probToDistribute += recurseNGramProbability(unigram);
					} else {
						probToDistribute += recurseNGramProbability(history.backoff().add(unigram.getFirst()));
					}
				}
			}

			if(probToDistribute == 0.0) {
				ngpb.backoff = 0.0;
			} else {
				ngpb.backoff = probLeftover/probToDistribute;
			}
		}

		// Ugh, crappy bookkeeping, some backoffs will still be NaN and should be zero
		for(Map<AbstractNGram, NGramProbabilityBackoff> ngramToPB : lowerOrderToNGrams) {
			for(Entry<AbstractNGram, NGramProbabilityBackoff> ngpb : ngramToPB.entrySet()) {
				ngpb.getValue().probability = Math.log10(ngpb.getValue().probability);
				if(Double.isNaN(ngpb.getValue().backoff)) {
					ngpb.getValue().backoff = Double.NEGATIVE_INFINITY;
				} else {
					ngpb.getValue().backoff = Math.log10(ngpb.getValue().backoff);
				}
			}
		}
		for(Entry<AbstractNGram, NGramProbability> ngp : highOrderNGrams.entrySet()) {
			ngp.getValue().probability = Math.log10(ngp.getValue().probability);
		}

		return new BackoffLanguageModel(10.0, highOrderNGrams, lowerOrderToNGrams);
	}


	// sanity checks during testing
//	public double addUnigrams() {
//		double sum = 0.0;
//		for(AbstractNGram ngram : orderToNGramCounter.get(0).getItems()) {
//			sum += antilog(logProbability(ngram));
//		}
//		System.out.println("Unigrams: " + sum);
//		return sum;
//	}
//
//	public void addBigrams() {
//		for(AbstractNGram history : orderToNGramCounter.get(0).getItems()) {
//			double sum = 0.0;
//			for(AbstractNGram wj : orderToNGramCounter.get(0).getItems()) {
//				sum += antilog(logProbability(new Bigram(history.get(0), wj.get(0))));
//			}
//			if(Math.abs(1.0 - sum) > 0.001) {
//				System.out.println("****************************Bigrams: " + sum);
//			} else {
//				System.out.println("Bigrams: " + sum);
//			}
//		}
//	}
}
