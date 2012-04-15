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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A set of words to which new words can only be added.
 *
 * Maintains a canonical version of the words in a string pool, to minimize memory usage for repeatedly created words.
 * This may come in handy when scanning a large amount of text and creating ngrams; m word strings will only ever take up 1 string's worth of memory.
 *
 * {@link UnsupportedOperationException} is thrown for any set operations that attempt to change the state of the set.
 *
 * @author romanows
 */
public class Vocabulary implements Set<String> {

	/** Used to hold the canonical word string in the value position */
	private final Map<String,String> wordPool;


	/** Constructor */
	public Vocabulary() {
		wordPool = new HashMap<String, String>();
	}


	/**
	 * Get a String reference to the canonical form of the string.
	 * @param s any string
	 * @return the canonical String object, or null if the vocabulary does not contain a canonical string for the requested string
	 */
	public String getCanonical(String s) {
		return wordPool.get(s);
	}

	/**
	 * Get a String reference to the canonical form of the string, adding the requested String if it is not already recorded as a canonical string.
	 * @param s any string
	 * @return the canonical String object
	 */
	public String addCanonical(String s) {
		String t = wordPool.get(s);
		if(t != null) {
			return t;
		}
		wordPool.put(s, s);
		return s;
	}

	@Override
	public int size() {
		return wordPool.size();
	}

	@Override
	public boolean isEmpty() {
		return wordPool.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return wordPool.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wordPool.keySet().containsAll(c);
	}

	@Override
	public Iterator<String> iterator() {
		return Collections.unmodifiableMap(wordPool).values().iterator();
	}

	@Override
	public Object[] toArray() {
		return Collections.unmodifiableMap(wordPool).values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return Collections.unmodifiableMap(wordPool).values().toArray(a);
	}

	@Override
	public boolean add(String s) {
		if(wordPool.containsKey(s)) {
			return false;
		}
		wordPool.put(s,s);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		boolean changed = false;
		for(String s : c) {
			changed = changed || add(s);
		}
		return changed;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
