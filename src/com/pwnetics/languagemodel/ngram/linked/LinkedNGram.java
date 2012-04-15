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


package com.pwnetics.languagemodel.ngram.linked;

import java.util.Map;

import com.pwnetics.languagemodel.ngram.AbstractNGram;
import com.pwnetics.languagemodel.ngram.Bigram;


/**
 * Compact store for NGrams used in a language model.
 *
 * In a language-model like collection of NGrams, NGrams will share parts with other NGrams.
 * Thus there will often be many trigrams like (y,z,a), (y,z,b), and (y,z,c) which share the
 * common history (y,z), which is also contained in the model.
 *
 * Thus, memory can be saved in a collection of NGrams when N > 3 by storing references to the
 * constituents of the ngram as (history, head).
 *
 * Cons: Construction time, if memory is to be saved, requires looking up the canonical history
 * from a collection of already-created NGrams.  Other operations are a bit slower but for the
 * language model use case where other NGrams are referenced against a created-once repository
 * of these LinkedNGrams, this mostly affects construction/initialization time.
 *
 * If you find yourself manipulating a LinkedNGram (calling any of the methods) rather than simply
 * referencing against it, you are probably better off using another NGram implementation.
 *
 * Do not use the {@link #add(String)} method; it works but it does not necessarily create a LinkedNGram and does not necessarily save any memory.
 *
 * @author romanows
 */
public class LinkedNGram extends AbstractNGram {

	/** last gram in this ngram; if null, this ngram is a zerogram */
	private final String head;

	/** history preceeding the head; if null, this ngram is a unigram or a zerogram */
	private final AbstractNGram history;


	/**
	 * Constructor.
	 * @param history should be reference to canonical history NGram that all other LinkedNGrams share
	 * @param head last gram, following the history, of this NGram;
	 *    should be a reference to a canonical string that all NGrams share.
	 */
	public LinkedNGram(AbstractNGram history, String head) {
		if(head == null) {
			throw new IllegalArgumentException();
		}
		this.history = history;
		this.head = head;
	}


	/**
	 * Constructor.
	 * May be convenient, but if you have your own pool storage type, you can use this code
	 * for reference and construct LinkedNGrams with {@link #LinkedNGram(AbstractNGram, String)}.
	 * @param ngramPool holds canonical NGrams; any new reusable ngrams (including this newly constructed ngram) will be added to it
	 * @param ngram NGram information to add; ngramPool will be consulted and any existing NGram pieces will be used
	 */
	public LinkedNGram(Map<AbstractNGram, AbstractNGram> ngramPool, AbstractNGram ngram) {
		switch (ngram.size()) {
		case 0:
			throw new IllegalArgumentException();
		case 1:
			head = ngram.get(0);
			history = null;
			break;
		default:
			head = ngram.get(ngram.size()-1);
			AbstractNGram h1 = ngram.history();
			AbstractNGram h2 = ngramPool.get(h1);
			if(h2 != null) {
				this.history = h2;
			} else {
				ngramPool.put(h1, h1);
				this.history = h1;
			}
			break;
		}
		ngramPool.put(this, this);
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#get(int)
	 */
	@Override
	public String get(int index) {
		if(index == 0) {
			if(history == null) {
				return head;
			} else {
				return history.get(0);
			}
		} else if(history == null) {
			throw new ArrayIndexOutOfBoundsException();
		} else if(index < history.size()) {
			return history.get(index);
		} else if(index == history.size()) {
			return head;
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#toArray()
	 */
	@Override
	public String[] toArray() {
		if(history == null) {
			return new String[] {head};
		}
		String [] ret = new String[size()];
		for(int i=0; i<history.size(); i++) {
			// This is probably faster than recursive toArray() calls
			// ... because if we're likely using LinkedNGrams for backoff and history,
			// ... which would entail a tree of array allocation and merging.
			ret[i] = history.get(i);
		}
		ret[ret.length-1] = head;
		return ret;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#size()
	 */
	@Override
	public int size() {
		if(history == null) {
			return 1;
		}
		return history.size() + 1;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#backoff()
	 */
	@Override
	public AbstractNGram backoff() {
		if(history == null) {
			return null;
		}
		return AbstractNGram.factory(toArray()).backoff();
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#history()
	 */
	@Override
	public AbstractNGram history() {
		return history;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.ngram.AbstractNGram#add(java.lang.String)
	 */
	@Override
	public AbstractNGram add(String nextGram) {
		if(history == null) {
			return new Bigram(head, nextGram);
		}
		String [] ob = toArray();
		String [] nb = new String[ob.length + 1];
		System.arraycopy(ob, 0, nb, 0, ob.length);
		nb[ob.length] = nextGram;
		return AbstractNGram.factory(nb);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(history == null) {
			return head;
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<history.size(); i++) {
			sb.append(history.get(i)).append(" ");
		}
		return sb.append(head).toString();
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(history == null) {
			return 31 + head.hashCode();
		}

		final int prime = 31;
		int result = 1;
		for(int i=0; i<history.size(); i++) {
			result = prime * result + history.get(i).hashCode();
		}
		result = prime * result + head.hashCode();
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
		if(size() != other.size()) {
			return false;
		}
		for(int i=other.size()-1; i>=0; i--) {
			if(!get(i).equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}
}
