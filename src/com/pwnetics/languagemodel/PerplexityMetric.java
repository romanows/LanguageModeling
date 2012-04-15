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


package com.pwnetics.languagemodel;

import java.util.LinkedList;
import java.util.List;

import com.pwnetics.languagemodel.lm.AbstractNGramLanguageModel;
import com.pwnetics.languagemodel.ngram.AbstractNGram;


/**
 * Calculates the perplexity of a language model with respect to some reference text.
 * The reference sentences are assumed to be independent.
 * @author romanows
 */
public class PerplexityMetric {

	/**
	 * Perplexity calculated over this data, a list of sentences which are themselves lists of tokens.
	 * The first and last tokens must be the start of sentence tokens, according to {@link AbstractNGramLanguageModel#logProbability(List)}.
	 */
	private final List<List<String>> referenceSentenceList;


	/**
	 * Holds perplexity score and related information.
	 * @author romanows
	 */
	public static class Score {
		/** Reported perplexity for a language model on some given dataset */
		public double perplexity = 0.0;

		/** Number of NGrams that were skipped (counted as occurring with a probability of 1.0) because they contained OOV words that are not counted when calculating perplexity */
		public int skippedOOVNGrams = 0;

		/**
		 * Add the given score information into this object.
		 * @param s score to accumulate
		 */
		public void accumulate(Score s) {
			perplexity += s.perplexity;
			skippedOOVNGrams += s.skippedOOVNGrams;
		}

		@Override
		public String toString() {
			return "(Perplexity.Score " + perplexity + " " + skippedOOVNGrams + ")";
		}
	}


	/**
	 * Constructor.
	 * @param referenceSentenceList reference text for calculation; list of sentences which are themselves lists of tokens
	 *     The first and last tokens must be the start of sentence tokens.
	 */
	public PerplexityMetric(List<List<String>> referenceSentenceList) {
		this.referenceSentenceList = referenceSentenceList;
	}


	/**
	 * Calculates perplexity on the reference sentences for the given language model.
	 * Jurafsky and Martin chapter 4.4: log(PP(W)) = (1/N) * log(P(W)).
	 * See also the <a href="http://www-speech.sri.com/projects/srilm/manpages/srilm-faq.7.html">SRILM FAQ</a>.
	 *
	 * Will "skip" any OOV words encountered, considering P(OOV) = 1, for the purposes of comparing perplexity.
	 * Same behavior as SRILM, as described in their FAQ.
	 *
	 * @param languageModel language model to score
	 * @return perplexity of the language model on the reference sentences
	 */
	public Score score(AbstractNGramLanguageModel languageModel) {
		Score s = new Score();
		int N = 0;
		for(List<String> sentence : referenceSentenceList) {
			// adding individual sentences assumes sentences in P(W) are are independent
			s.accumulate(logProbability(languageModel, sentence));

			// We don't calculate the probability of the first, start-of-sentence, token
			N += (sentence.size()-1) - languageModel.getOOV(sentence).size();
		}
		s.perplexity = languageModel.antilog(-s.perplexity / N);  // Rewrite accumulating log-probability as perplexity
		return s;
	}


	/**
	 * Calculate the log-probability of a sequence of words.
	 * Takes P(OOV) = 1 which effectively ignores OOV words, so perplexity can be calculated over a word sequence that contains some OOV.
	 * @param delimitedSentence sequence of words starting with a beginning-of-sentence delimiter token and an end-of-sentence delimiter token
	 * @return not really the "score", but instead, the log-probability of the given sequence of words and number of ngrams skipped when computing OOV.
	 */
	private Score logProbability(AbstractNGramLanguageModel languageModel, List<String> delimitedSentence) {
		Score s = new Score();
		LinkedList<String> src = new LinkedList<String>();

		for(String n : delimitedSentence) {
			src.addLast(n);
			if(src.size() > 1) {
				if(src.size() > languageModel.order()) {
					src.removeFirst();
				}
				double temp = languageModel.logProbability(AbstractNGram.factory(src));
				if(Double.isInfinite(temp)) {
					temp = 0.0;  // log(P(OOV)) = 1
					s.skippedOOVNGrams++;
				}
				s.perplexity += temp;
			}
		}
		return s;
	}
}
