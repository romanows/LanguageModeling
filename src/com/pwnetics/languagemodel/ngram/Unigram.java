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
 * A unigram object holds 1 word.
 * @author romanows
 */
//@Invariant("w1 != null")
public class Unigram extends AbstractNGram {

	private final String w1;


	/**
	 * Constructor.
	 * @param gram unigram string, cannot be null
	 */
	public Unigram(String gram) {
		if(gram == null) {
			throw new IllegalArgumentException();
		}
		w1 = gram;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#get(int)
	 */
	@Override
	public String get(int index) {
		if(index == 0) {
			return w1;
		}
		throw new ArrayIndexOutOfBoundsException();
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
		return w1;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#value()
	 */
	@Override
	public String[] toArray() {
		return new String[] {w1};
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#size()
	 */
	@Override
	public int size() {
		return 1;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#backoff()
	 */
	@Override
	public AbstractNGram backoff() {
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#history()
	 */
	@Override
	public AbstractNGram history() {
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#add(java.lang.String)
	 */
	@Override
	public AbstractNGram add(String nextGram) {
		return new Bigram(w1, nextGram);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return w1;
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 + w1.hashCode();
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
		if(other.size() != 1) {
			return false;
		}
		return w1.equals(other.get(0));
	}
}
