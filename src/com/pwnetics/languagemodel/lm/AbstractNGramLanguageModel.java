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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.pwnetics.languagemodel.ngram.AbstractNGram;


/**
 * NGram language models calculate the probability of a sequence of words, that is, an ngram.
 * @author romanows
 */
public abstract class AbstractNGramLanguageModel {

	/** Maximum ngram size that the model contains */
	protected final int order;

	/** Base of the logarithms used to represent probabilities */
	protected final double logBase;


	/**
	 * Constructor.
	 * @param order Maximum ngram size that the model contains
	 */
	public AbstractNGramLanguageModel(int order, double logBase) {
		if(order < 1) {
			throw new IllegalArgumentException();
		}
		this.order = order;
		this.logBase = logBase;
	}


	/**
	 * Compiles and returns the vocabulary stored in this model.
	 * This may take some time; it is generally better to obtain the vocabulary from another source if possible.
	 * @return vocabulary stored in the model
	 */
	public abstract Set<String> getVocabulary();


	/**
	 * Calculate the log-probability of a sequence of words.
	 * @param delimitedSentence sequence of words starting with a beginning-of-sentence delimiter token and an end-of-sentence delimiter token
	 * @return log-probability of the given sequence of words
	 */
	public double logProbability(List<String> delimitedSentence) {
		double logProb = 0.0;
		LinkedList<String> src = new LinkedList<String>();

		for(String n : delimitedSentence) {
			src.addLast(n);
			if(src.size() > 1) {
				if(src.size() > order) {
					src.removeFirst();
				}
				logProb += logProbability(AbstractNGram.factory(src));
			}
		}
		return logProb;
	}


	/**
	 * Get the words in a list that are not in the language model.
	 * @param delimitedSentence sequence of words starting with a beginning-of-sentence delimiter token and an end-of-sentence delimiter token
	 * @return the words in a list that are not in the language model
	 */
	public List<String> getOOV(List<String> delimitedSentence) {
		List<String> oov = new ArrayList<String>();
		for(String word : delimitedSentence) {
			if(Double.isInfinite(logProbability(AbstractNGram.factory(new String[] {word})))) {
				oov.add(word);
			}
		}
		return oov;
	}


	/**
	 * Calculate the log-probability of an ngram.
	 * @param ngram ngram whose log-probability is desired
	 * @return log-probability of given ngram
	 */
	abstract public double logProbability(AbstractNGram ngram);


	/**
	 * Get the maximum ngram size that the model contains.
	 * @return the maximum ngram size
	 */
	public int order() {
		return order;
	}


	/**
	 * Get the base of the logarithms used to represent probabilities.
	 * @return logarithm base
	 */
	public double logBase() {
		return logBase;
	}


	/**
	 * Get the log of the given decimal-domain value.
	 * @param value given value
	 * @return logBase^log(value)
	 */
	public double log(double value) {
		if(logBase == 10.0) {
			return Math.log10(value);
		} else if(logBase == Math.E) {
			return Math.log(value);
		}
		return Math.log10(value) / Math.log10(logBase);
	}


	/**
	 * Get the antilog of a log value.
	 * @param logValue given value in log domain
	 * @return logBase^logValue
	 */
	public double antilog(double logValue) {
		return Math.pow(logBase, logValue);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Language Model: ").append(order).append("-gram");
		return sb.toString();
	}
}
