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


package com.pwnetics.languagemodel.ngram;

import java.util.Iterator;
import java.util.List;

/**
 * Represents NGrams.
 *
 * Subclasses must override toString(), hashCode(), and equals().
 * @author romanows
 */
//@Invariant("size() > 0")
public abstract class AbstractNGram {

	/**
	 * Return the word at the given index in the NGram.
	 * The first word ("oldest" word) is at index 0, while the last word ("newest" word) is at {@link #size()}-1.
	 * @param index zero-based index
	 * @return the word at the given index
	 */
//	@Requires("index >= 0 && index < size()")
	abstract public String get(int index);


	/**
	 * Return the first word in the NGram.
	 * Convenience method, equivalent to get(0).
	 * @return the first word
	 */
	public String getFirst() {
		return get(0);
	}


	/**
	 * Return the last word in the NGram.
	 * Convenience method, equivalent to get(size()-1).
	 * @return the last word
	 */
	public String getLast() {
		return get(size()-1);
	}


	/**
	 * @return the ngram
	 */
//	@Ensures("result != null")
	abstract public String[] toArray();


	/**
	 * Get the order of this NGram.
	 * For example, this method will return 2 if the NGram is a bigram.
	 * @return the order of this NGram
	 */
//	@Ensures("result > 0")
	abstract public int size();


	/**
	 * Get a new (N-1)-Gram object that contains words 1 through {@link #size()}-1.
	 * Will return null when backing off of a unigram.
	 * Contrast with {@link #history()}.
	 * @return the backoff version of this NGram or null when backing off from a unigram
	 */
//	@Ensures("result == null || result.size() == size() - 1")
	abstract public AbstractNGram backoff();


	/**
	 * Get the "history" of this NGram as an NGram object that contains words 0 through {@link #size()}-2.
	 * The history of a unigram is null.
	 * Contrast with {@link #backoff()}.
	 * @return the history of this NGram; null if called on a unigram
	 */
//	@Ensures("result == null || result.size() == size() - 1")
	abstract public AbstractNGram history();


	/**
	 * Get a new (N+1)-Gram object that contains words 1 through {@link #size()} of this N-Gram plus the given word.
	 * @return an (N+1)-Gram that is this N-Gram concatenated with a given word
	 */
//	@Requires("nextGram != null")
//	@Ensures("result.size() == size() + 1")
	abstract public AbstractNGram add(String nextGram);


	/**
	 * Get a NGram object for the given array of words.
	 * @param grams array to store as ngrams
	 * @return an NGram object encapsulating the given grams
	 */
//	@Requires("grams != null")
	public static AbstractNGram factory(String [] grams) {
		switch (grams.length) {
		case 0:
			throw new IllegalArgumentException();
		case 1:
			return new Unigram(grams[0]);
		case 2:
			return new Bigram(grams[0], grams[1]);
		case 3:
			return new Trigram(grams[0], grams[1], grams[2]);
		case 4:
			return new Fourgram(grams[0], grams[1], grams[2], grams[3]);
		case 5:
			return new Fivegram(grams[0], grams[1], grams[2], grams[3], grams[4]);
		default:
			return new NGram(grams);
		}
	}


	/**
	 * Get a NGram object for the given collection of words.
	 * @param grams array to store as ngrams
	 * @return an NGram object encapsulating the given grams
	 */
//	@Requires("grams != null")
	public static AbstractNGram factory(List<String> grams) {
		switch (grams.size()) {
		case 0:
			throw new IllegalArgumentException();
		case 1:
			return new Unigram(grams.get(0));
		case 2:
			return new Bigram(grams.get(0), grams.get(1));
		case 3:
			Iterator<String> it = grams.iterator();
			return new Trigram(it.next(), it.next(), it.next());
		case 4:
			it = grams.iterator();
			return new Fourgram(it.next(), it.next(), it.next(), it.next());
		case 5:
			it = grams.iterator();
			return new Fivegram(it.next(), it.next(), it.next(), it.next(), it.next());
		default:
			return new NGram(grams.toArray(new String[grams.size()]));
		}
	}
}
