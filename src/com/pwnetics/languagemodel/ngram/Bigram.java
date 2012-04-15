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


/**
 * A bigram object holds 2 words.
 * @author romanows
 */
//@Invariant({"w1 != null", "w2 != null"})
public class Bigram extends AbstractNGram {

	private final String w1;
	private final String w2;


	/**
	 * Constructor.
	 * @param gram1 first word in bigram, cannot be null
	 * @param gram2 second word in bigram, cannot be null
	 */
	public Bigram(String gram1, String gram2) {
		if(gram1 == null || gram2 == null) {
			throw new IllegalArgumentException();
		}
		w1 = gram1;
		w2 = gram2;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#get(int)
	 */
	@Override
	public String get(int index) {
		switch (index) {
		case 0:
			return w1;
		case 1:
			return w2;
		default:
			throw new ArrayIndexOutOfBoundsException();
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#getFirst()
	 */
	@Override
	public String getFirst() {
		return w1;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#getLast()
	 */
	@Override
	public String getLast() {
		return w2;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#value()
	 */
	@Override
	public String[] toArray() {
		return new String[] {w1,w2};
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#size()
	 */
	@Override
	public int size() {
		return 2;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#backoff()
	 */
	@Override
	public AbstractNGram backoff() {
		return new Unigram(w2);
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#history()
	 */
	@Override
	public AbstractNGram history() {
		return new Unigram(w1);
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#add(java.lang.String)
	 */
	@Override
	public AbstractNGram add(String nextGram) {
		return new Trigram(w1, w2, nextGram);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append(w1).append(" ").append(w2).toString();
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + w1.hashCode();
		result = prime * result + w2.hashCode();
		return result;
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof AbstractNGram)) {
			return false;
		}
		AbstractNGram other = (AbstractNGram) obj;
		if(other.size() != 2) {
			return false;
		}
		return w2.equals(other.get(1)) && w1.equals(other.get(0));
	}
}
