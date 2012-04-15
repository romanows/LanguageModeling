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

import java.util.Iterator;
import java.util.LinkedList;

import com.pwnetics.languagemodel.ngram.AbstractNGram;
import com.pwnetics.languagemodel.tokenizer.ITokenizer;

/**
 * Will output all n-grams for n in [1,q] in a {@link ITokenizer}, adding all words to a given {@link Vocabulary}.
 *
 * Given an input with the tokens "a b c d" and specifying a size of 3 will give the output: [a,b,ab,c,bc,abc,d,cd,bcd].
 * Words "a b c d" will be added to the vocabulary.
 *
 * If the array is initialized with a start-of-utterance token "a", then the above tokens will give the output: [b,ab,aab,c,bc,abc,d,cd,bcd].
 * Words "b c d" will be added to the vocabulary.  The start-of-utterance token is not added.
 *
 * Apart from accumulating a list of words {@link Vocabulary}, the vocabulary is used to provide a canonical String reference for each token in an NGram.
 * This prevents duplication of string objects that compare as equal.
 *
 * @author romanows
 */
public class NGramEnumerator implements Iterable<AbstractNGram> {
	/*
	 * Not sure how I feel about using a String to indicate sentence/utterance breaks in the token stream.
	 * Really, the tokenizer should emit events; OTOH, that adds complexity to the code and the <s> </s> convention is extremely common.
	 */


	/** Produces tokens that will become part of ngrams */
	private final Iterator<String> tokenIter;

	/** Vocabulary that accumulates new tokens and provides a canonical String reference for new tokens */
	private final Vocabulary vocabulary;

	/** Max order of ngrams, e.g. "3" for trigrams.  All ngrams produced by this class will be this order or lower. */
	private final int size;

	/** Token string that identifies a start-of-utterance in the token stream */
	private final String beginUtterance;

	/** New words added onto the end of the queue.  Limited to {@link #size} number of elements. */
	private final LinkedList<String> queue;

	/** Moves backwards through the queue, indicating the start (inclusive) of ever-longer n-grams */
	private int nextIdx;


	/**
	 * Constructor.
	 * @param vocabulary collects tokens and serves as a string pool that all created ngrams will reference
	 * @param tokenizer provides tokens for ngrams
	 * @param size max ngram order to output
	 */
	public NGramEnumerator(Vocabulary vocabulary, ITokenizer tokenizer, int size) {
		this(vocabulary, tokenizer, size, null);
	}


	/**
	 * Constructor.
	 * @param vocabulary collects tokens and serves as a string pool that all created ngrams will reference
	 * @param tokenizer provides tokens for ngrams
	 * @param size max ngram order to output
	 * @param beginUtterance this string will be treated as a beginning-of-utterance token.  It is not added to the vocabulary.  See this class's description for details.
	 */
	public NGramEnumerator(Vocabulary vocabulary, ITokenizer tokenizer, int size, String beginUtterance) {
		if(tokenizer == null || size < 1) {
			throw new IllegalArgumentException();
		}

		this.vocabulary = vocabulary;
		this.tokenIter = tokenizer.iterator();
		this.size = size;
		this.beginUtterance = beginUtterance;

		queue = new LinkedList<String>();
		nextIdx = -1;
	}


	/**
	 * Get the vocabulary built by this enumerator.
	 * @return the vocabulary built by this enumerator
	 */
	public Vocabulary getVocabulary() {
		return vocabulary;
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AbstractNGram> iterator() {
		return new Iterator<AbstractNGram>() {

			@Override
			public boolean hasNext() {
				// Make next() do most of the work.  Either our next ngram is in the queue or we expect to get a new ngram.
				if(queue.size() > 0 && nextIdx >= 0) {
					return true;
				}
				return tokenIter.hasNext();
			}

			@Override
			public AbstractNGram next() {
				// Enough tokens in queue
				if(queue.size() > 0 && nextIdx >= 0) {
					return AbstractNGram.factory(queue.subList(nextIdx--, queue.size()));
				}

				// Not enough desired tokens; grab a new token
				String s;

				// Get the next token after any optional start-of-utterance markers
				while( (s = tokenIter.next()).equals(beginUtterance) ) {
					queue.clear();

					// Beginning utterance token used to pad the start of new utterances
					if(beginUtterance != null) {
						for(int i=0; i<size-1; i++) {
							queue.add(beginUtterance);
						}
					}
				}

				// Add the next non-start-of-utterance marker.
				queue.add(vocabulary.addCanonical(s));

				// Enforce queue size
				while(queue.size() > size) {
					queue.removeFirst();
				}
				nextIdx = queue.size() - 1;

				return AbstractNGram.factory(queue.subList(nextIdx--, queue.size()));
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
