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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pwnetics.languagemodel.ngram.AbstractNGram;


/**
 * Compute probabilities for a ngram backoff language model.
 * @author romanows
 */
public class BackoffLanguageModel extends AbstractNGramLanguageModel {
	/** Highest-order ngrams and their probability estimates */
	protected final Map<AbstractNGram, NGramProbability> highOrderNGrams;

	/** lower-order ngrams and their associated probability and backoff weights or null if model order is 1 and there is no low order ngram information; e.g., lowerOrderToNGrams.get(0) contains the unigram information in a trigram model. */
	protected final List<Map<AbstractNGram, NGramProbabilityBackoff>> lowerOrderToNGrams;


	/**
	 * Constructor.
	 * @param logBase base used for the logarithms
	 * @param highOrderNGrams highest-order ngrams and their probability estimates
	 * @param lowerOrderToNGrams lower-order ngrams and their associated probability and backoff weights or null if model order is 1 and there is no low order ngram information; e.g., lowerOrderToNGrams.get(0) contains the unigram information in a trigram model.
	 */
	public BackoffLanguageModel(double logBase, Map<AbstractNGram, NGramProbability> highOrderNGrams, List<Map<AbstractNGram, NGramProbabilityBackoff>> lowerOrderToNGrams) {
		super(lowerOrderToNGrams == null ? 1 : lowerOrderToNGrams.size()+1, logBase);
		this.highOrderNGrams = highOrderNGrams;
		if(order > 1) {
			this.lowerOrderToNGrams = lowerOrderToNGrams;
		} else {
			this.lowerOrderToNGrams = null;
		}
	}


	/**
	 * Constructor.
	 * Used by extending classes that will fill-in model parameters.
	 * @param order model order
	 * @param logBase base used for the logarithms
	 */
	protected BackoffLanguageModel(int order, double logBase) {
		super(order, logBase);
		highOrderNGrams = new HashMap<AbstractNGram, NGramProbability>();
		if(order > 1) {
			lowerOrderToNGrams = new ArrayList<Map<AbstractNGram, NGramProbabilityBackoff>>();
			for(int i=1; i<order; i++) {
				lowerOrderToNGrams.add(new HashMap<AbstractNGram, NGramProbabilityBackoff>());
			}
		} else {
			lowerOrderToNGrams = null;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.lm.AbstractNGramLanguageModel#getVocabulary()
	 */
	@Override
	public Set<String> getVocabulary() {
		Set<AbstractNGram> unigramSet;
		if(lowerOrderToNGrams != null) {
			unigramSet = lowerOrderToNGrams.get(0).keySet();
		} else {
			unigramSet = highOrderNGrams.keySet();
		}
		Set<String> wordSet = new HashSet<String>();
		for(AbstractNGram u : unigramSet) {
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
		if(ngram.size() > order) {
			throw new IllegalArgumentException("ngram order exceeds model order");
		}

		if(ngram.size() < 1) {
			throw new IllegalArgumentException("must supply ngram of positive valued order");
		}

		// See if the given ngram is in our model
		if(ngram.size() == order) {
			NGramProbability logProb = highOrderNGrams.get(ngram);
			if(logProb != null) {
				return logProb.probability;
			}
		} else if(order > 1) {
			NGramProbability logProb = lowerOrderToNGrams.get(ngram.size()-1).get(ngram);
			if(logProb != null) {
				return logProb.probability;
			}
		}

		// No sense in backoff from a unigram
		if(ngram.size() < 2) {
			return Double.NEGATIVE_INFINITY;
		}

		// Try the backoff version of the ngram
		NGramProbabilityBackoff historyPb = lowerOrderToNGrams.get(ngram.size()-2).get(ngram.history());
		if(historyPb != null && historyPb.backoff != Double.NEGATIVE_INFINITY) {
			return historyPb.backoff + logProbability(ngram.backoff());
		}
		return logProbability(ngram.backoff());
	}


	/**
	 * The number of ngram/prob/backoff(optional) parameters in this model.
	 * @return number of ngram/prob/backoff(optional) parameters in this model
	 */
	public int size() {
		int size = highOrderNGrams.size();
		for(Map<AbstractNGram, NGramProbabilityBackoff> orderData : lowerOrderToNGrams) {
			size += orderData.size();
		}
		return size;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.lm.AbstractNGramLanguageModel#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Backoff Language Model: ").append(order).append("-gram, size: ").append(size());
		return sb.toString();
	}
}
