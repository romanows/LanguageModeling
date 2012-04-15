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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class TestNGramHashCodeEquals {

	@Test
	public void testUnigram() {
		String [] s = new String[] {"foo"};
		String [] t = new String[] {"foo"};
		assertTrue(s != t);
		assertTrue(Arrays.equals(s,t));

		List<AbstractNGram> ngrams = new ArrayList<AbstractNGram>();
		ngrams.add(AbstractNGram.factory(s));
		ngrams.add(AbstractNGram.factory(t));
		ngrams.add(new NGram(s));
		ngrams.add(new NGram(t));
		ngrams.add(new Unigram(s[0]));
		ngrams.add(new Unigram(t[0]));

		for(AbstractNGram p : ngrams) {
			for(AbstractNGram q : ngrams) {
				assertTrue(p.equals(q));
				assertTrue(p.hashCode() == q.hashCode());
			}
		}
	}

	@Test
	public void testBigram() {
		String [] s = new String[] {"foo", "bar"};
		String [] t = new String[] {"foo", "bar"};
		assertTrue(s != t);
		assertTrue(Arrays.equals(s,t));

		List<AbstractNGram> ngrams = new ArrayList<AbstractNGram>();
		ngrams.add(AbstractNGram.factory(s));
		ngrams.add(AbstractNGram.factory(t));
		ngrams.add(new NGram(s));
		ngrams.add(new NGram(t));
		ngrams.add(new Bigram(s[0], s[1]));
		ngrams.add(new Bigram(t[0], t[1]));

		for(AbstractNGram p : ngrams) {
			for(AbstractNGram q : ngrams) {
				assertTrue(p.equals(q));
				assertTrue(p.hashCode() == q.hashCode());
			}
		}
	}

	@Test
	public void testTrigram() {
		String [] s = new String[] {"foo", "bar", "bat"};
		String [] t = new String[] {"foo", "bar", "bat"};
		assertTrue(s != t);
		assertTrue(Arrays.equals(s,t));

		List<AbstractNGram> ngrams = new ArrayList<AbstractNGram>();
		ngrams.add(AbstractNGram.factory(s));
		ngrams.add(AbstractNGram.factory(t));
		ngrams.add(new NGram(s));
		ngrams.add(new NGram(t));
		ngrams.add(new Trigram(s[0], s[1], s[2]));
		ngrams.add(new Trigram(t[0], t[1], t[2]));

		for(AbstractNGram p : ngrams) {
			for(AbstractNGram q : ngrams) {
				assertTrue(p.equals(q));
				assertTrue(p.hashCode() == q.hashCode());
			}
		}
	}

	@Test
	public void testFourgram() {
		String [] s = new String[] {"foo", "bar", "bat", "baz"};
		String [] t = new String[] {"foo", "bar", "bat", "baz"};
		assertTrue(s != t);
		assertTrue(Arrays.equals(s,t));

		List<AbstractNGram> ngrams = new ArrayList<AbstractNGram>();
		ngrams.add(AbstractNGram.factory(s));
		ngrams.add(AbstractNGram.factory(t));
		ngrams.add(new NGram(s));
		ngrams.add(new NGram(t));
		ngrams.add(new Fourgram(s[0], s[1], s[2], s[3]));
		ngrams.add(new Fourgram(t[0], t[1], t[2], t[3]));

		for(AbstractNGram p : ngrams) {
			for(AbstractNGram q : ngrams) {
				assertTrue(p.equals(q));
				assertTrue(p.hashCode() == q.hashCode());
			}
		}
	}

	@Test
	public void testFivegram() {
		String [] s = new String[] {"foo", "bar", "bat", "baz", "bad"};
		String [] t = new String[] {"foo", "bar", "bat", "baz", "bad"};
		assertTrue(s != t);
		assertTrue(Arrays.equals(s,t));

		List<AbstractNGram> ngrams = new ArrayList<AbstractNGram>();
		ngrams.add(AbstractNGram.factory(s));
		ngrams.add(AbstractNGram.factory(t));
		ngrams.add(new NGram(s));
		ngrams.add(new NGram(t));
		ngrams.add(new Fivegram(s[0], s[1], s[2], s[3], s[4]));
		ngrams.add(new Fivegram(t[0], t[1], t[2], t[3], t[4]));

		for(AbstractNGram p : ngrams) {
			for(AbstractNGram q : ngrams) {
				assertTrue(p.equals(q));
				assertTrue(p.hashCode() == q.hashCode());
			}
		}
	}

	@Test
	public void UnigramUnequal() {
		String [] s = new String[] {"foo"};
		String [] t = new String[] {"poo"};
		assertTrue(s != t);
		assertTrue(!Arrays.equals(s,t));

		List<AbstractNGram> sNgrams = new ArrayList<AbstractNGram>();
		List<AbstractNGram> tNgrams = new ArrayList<AbstractNGram>();

		sNgrams.add(AbstractNGram.factory(s));
		sNgrams.add(new NGram(s));
		sNgrams.add(new Unigram(s[0]));

		tNgrams.add(AbstractNGram.factory(t));
		tNgrams.add(new NGram(t));
		tNgrams.add(new Unigram(t[0]));

		for(AbstractNGram p : sNgrams) {
			for(AbstractNGram q : tNgrams) {
				assertTrue(!p.equals(q));
				if(s[0].hashCode() != t[0].hashCode()) {
					assertTrue(p.hashCode() != q.hashCode());
				}
			}
		}
	}

	@Test
	public void BigramUnequal() {
		// Different words
		String [] s = new String[] {"foo", "bar"};
		String [] t = new String[] {"poo", "par"};
		assertTrue(s != t);
		assertTrue(!Arrays.equals(s,t));

		List<AbstractNGram> sNgrams = new ArrayList<AbstractNGram>();
		List<AbstractNGram> tNgrams = new ArrayList<AbstractNGram>();

		sNgrams.add(AbstractNGram.factory(s));
		sNgrams.add(new NGram(s));
		sNgrams.add(new Bigram(s[0], s[1]));

		tNgrams.add(AbstractNGram.factory(t));
		tNgrams.add(new NGram(t));
		tNgrams.add(new Bigram(t[0], t[1]));

		for(AbstractNGram p : sNgrams) {
			for(AbstractNGram q : tNgrams) {
				assertTrue(!p.equals(q));
				assertTrue(p.hashCode() != q.hashCode());
			}
		}

		// Same words, but different order
		s = new String[] {"foo", "bar"};
		t = new String[] {"bar", "foo"};
		assertTrue(s != t);
		assertTrue(!Arrays.equals(s,t));

		sNgrams = new ArrayList<AbstractNGram>();
		tNgrams = new ArrayList<AbstractNGram>();

		sNgrams.add(AbstractNGram.factory(s));
		sNgrams.add(new NGram(s));
		sNgrams.add(new Bigram(s[0], s[1]));

		tNgrams.add(AbstractNGram.factory(t));
		tNgrams.add(new NGram(t));
		tNgrams.add(new Bigram(t[0], t[1]));

		for(AbstractNGram p : sNgrams) {
			for(AbstractNGram q : tNgrams) {
				assertTrue(!p.equals(q));
				assertTrue(p.hashCode() != q.hashCode());
			}
		}
	}
}
