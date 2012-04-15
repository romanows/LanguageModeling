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
 * Simple NGram container.
 * Does not attempt to verify NGram contents, nor make copies of NGrams to prevent external modification of values.
 *
 * @author romanows
 */
//@Invariant("ngram != null")
public class NGram extends AbstractNGram {

	/** The ngram tokens */
	protected final String [] ngram;


	/**
	 * Constructor.
	 * @param grams array to store as ngrams, cannot be null
	 */
	public NGram(String [] grams) {
		if(grams == null || grams.length < 1) {
			throw new IllegalArgumentException();
		}
		ngram = grams;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#get(int)
	 */
	@Override
	public String get(int index) {
		return ngram[index];
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#getFirst()
	 */
	@Override
	public String getFirst() {
		return ngram[0];
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#getLast()
	 */
	@Override
	public String getLast() {
		return ngram[ngram.length-1];
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#value()
	 */
	@Override
	public String[] toArray() {
		return ngram;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#size()
	 */
	@Override
	public int size() {
		return ngram.length;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.AbstractNGram#backoff()
	 */
	@Override
	public AbstractNGram backoff() {
		if(ngram.length <= 1) {
			return null;
		}

		String [] backoffNGram = new String[ngram.length - 1];
		System.arraycopy(ngram, 1, backoffNGram, 0, ngram.length - 1);
		return factory(backoffNGram);
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#history()
	 */
	@Override
	public AbstractNGram history() {
		if(ngram.length <= 1) {
			return null;
		}

		String [] historyNGram = new String[ngram.length - 1];
		System.arraycopy(ngram, 0, historyNGram, 0, ngram.length - 1);
		return factory(historyNGram);
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#add(java.lang.String)
	 */
	@Override
	public AbstractNGram add(String nextGram) {
		String [] s = new String[ngram.length + 1];
		System.arraycopy(ngram,0,s,0,ngram.length);
		s[s.length-1] = nextGram;
		return factory(s);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Seems like this would be faster for unigrams and maybe bigrams... however I don't know if there is any real improvement versus the increased verbosity.

		switch (ngram.length) {
		case 1:
			return ngram[0];
		case 2:
			return new StringBuffer().append(ngram[0]).append(" ").append(ngram[1]).toString();
		case 3:
			return new StringBuffer().append(ngram[0]).append(" ").append(ngram[1]).append(" ").append(ngram[2]).toString();
		default:
			StringBuffer sb = new StringBuffer();
			for(String s : ngram) {
				sb.append(s).append(" ");
			}
			return sb.substring(0, sb.length()-1);
		}
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for(int i=0; i<ngram.length; i++) {
			result = prime * result + ngram[i].hashCode();
		}
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
		if(other.size() != ngram.length) {
			return false;
		}
		for(int i=ngram.length-1; i>=0; i--) {
			if(!ngram[i].equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}
}
