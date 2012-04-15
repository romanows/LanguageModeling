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


package com.pwnetics.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.pwnetics.languagemodel.Vocabulary;
import com.pwnetics.languagemodel.ngram.AbstractNGram;
import com.pwnetics.languagemodel.ngram.Fivegram;
import com.pwnetics.languagemodel.ngram.NGram;
import com.pwnetics.languagemodel.ngram.Trigram;

/**
 * Test memory usage of the {@link AbstractNGram} family of objects.
 *
 * The upshot is that the specialized ngrams like {@link Trigram} and {@link Fivegram} do save significant amounts of memory when compared to the generic {@link NGram}.
 * When using 5-grams, you can fit about 30% more 5-grams in memory (a 600,000 more than the 2,294,943 that can fit in 256M when using {@link NGram}).
 *
 * @author romanows
 */
public class NGramInMemory {

	private enum NGramType {NGram, AbstractFactory};

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		/*
		 * Uncomment this to see how many "words" you can fit in a Vocabulary object before memory runs out.
		 */

//		printMaxVocabWords();


		/*
		 * Compare memory usage of NGram objects and the specialized ngrams produced by AbstractNGram.factory().
		 * Creates a bunch of n-grams from "words"
		 */
		final NGramType type = NGramType.AbstractFactory;

		// Create a vocabulary for trigrams
		List<String> vocabList = new ArrayList<String>();
		final int radix = Character.MAX_RADIX;
		long word = Long.parseLong("1", radix);
		for(int i=0; i<500000; i++) {
			vocabList.add(Long.toString(word++, radix));
		}


		Map<AbstractNGram, AbstractNGram> ngramContainer = new HashMap<AbstractNGram, AbstractNGram>();
		final int order = 5;
		printMem(true);
		try {
			Random rnd = new Random(0x42);
			LinkedList<String> queue = new LinkedList<String>();
			while(true) {
				for(String w : vocabList) {
					queue.addLast(w);

					if(queue.size() >= order) {
//						for(int i=order-1; i>=0; i--) {  // Create all order n-grams
						for(int i=0; i>=0; i--) {        // Create only order-n n-grams
							String [] ngram = queue.subList(i, order).toArray(new String[0]);

							AbstractNGram ang;
							switch (type) {
							case AbstractFactory:
								ang = AbstractNGram.factory(ngram);
								break;
							case NGram:
								ang = new NGram(ngram);
							default:
								throw new RuntimeException();
							}

							ngramContainer.put(ang, ang);
						}
						queue.removeFirst();
					}
				}
				Collections.shuffle(vocabList, rnd);
			}
		} catch (OutOfMemoryError e) {
			printMem(true);
		}
		System.out.println("Ngrams in Container: " + ngramContainer.size());  // Needs to be here so we won't garbage collect it before printing memory status
		System.out.println("(ignore this: " + vocabList.size() + ")"); // Needs to be here so we won't garbage collect it before printing memory status
	}


	// Avoid creating objects when memory is tight
	static final private double mb = 1024 * 1024;
	static final private Runtime runtime = Runtime.getRuntime();


	/**
	 * Print memory stats.
	 * @param doGarbageCollect if true, will do garbage collection to hopefully provide a tighter bound on the memory-in-use when this is called
	 */
	static final void printMem(boolean doGarbageCollect) {
		if(doGarbageCollect) {
			System.gc();
		}

		System.out.println("\n*******************************************************");
		System.out.append("\tMax Memory:\t").append(Double.toString(runtime.maxMemory() / mb)).append("\n");
		System.out.append("\tFree Memory:\t").append(Double.toString(runtime.freeMemory() / mb)).append("\n");
		System.out.append("\tTotal Memory:\t").append(Double.toString(runtime.totalMemory() / mb)).append("\n");
		System.out.append("*******************************************************\n");
	}


	/**
	 * Fill up memory with "words", printing memory stats on how many we could make.
	 */
	static void printMaxVocabWords() {
		/*
		 * On Vimes, Ubuntu 9,04 running on JDK 1.6.0.22
		 * Added 626076 "words" from 100 to 2306735 in radix 8 with -Xmx64M
		 */

		Vocabulary vocabulary = new Vocabulary();
		final int radix = 8;  // Can tune this parameter to change the distribution of short/long strings
		long word = Long.parseLong("100", radix);
		System.out.println("First word: " + Long.toString(word, radix));
		printMem(true);
		try {
			while(true) {
				String s = Long.toString(word++, radix);
				vocabulary.add(s);
			}
		} catch (OutOfMemoryError e) {
			printMem(true);
		}
		System.out.println("Last word: " + Long.toString(word++, radix));
		System.out.println("Words added before OutOfMemory: " + vocabulary.size());
	}

}
