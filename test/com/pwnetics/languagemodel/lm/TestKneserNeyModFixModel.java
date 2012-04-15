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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.pwnetics.helper.ItemCounter;
import com.pwnetics.languagemodel.PerplexityMetric;
import com.pwnetics.languagemodel.arpa.ARPAModelLoader;
import com.pwnetics.languagemodel.ngram.AbstractNGram;
import com.pwnetics.languagemodel.ngram.Bigram;
import com.pwnetics.languagemodel.ngram.Trigram;
import com.pwnetics.languagemodel.ngram.Unigram;

public class TestKneserNeyModFixModel {


	private List<List<String>> readSentences(File sentFile) throws IOException {
		List<List<String>> referenceSentenceList = new ArrayList<List<String>>();

		// FIXME: Should eat our own dogfood and use the string tokenizer for this
		BufferedReader br = new BufferedReader(new FileReader(sentFile));
		String line;
		while( (line = br.readLine()) != null ) {
			line = line.trim();
			if(!line.equals("")) {
				referenceSentenceList.add(Arrays.asList(line.split("\\s+")));
			}
		}
		return referenceSentenceList;
	}


	private KneserNeyModFixModel2 trainBigram(List<List<String>> referenceSentenceList) {
		ItemCounter<AbstractNGram> unigramCounter = new ItemCounter<AbstractNGram>();
		ItemCounter<AbstractNGram> bigramCounter = new ItemCounter<AbstractNGram>();
		for(List<String> sentence : referenceSentenceList) {
			for(int i=0; i<sentence.size()-1; i++) {
				unigramCounter.increment(new Unigram(sentence.get(i)));
				bigramCounter.increment(new Bigram(sentence.get(i), sentence.get(i+1)));
			}
			unigramCounter.increment(new Unigram(sentence.get(sentence.size()-1)));
		}

		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(unigramCounter);
		orderToNGramCounter.add(bigramCounter);
		return new KneserNeyModFixModel2(2, 10, orderToNGramCounter);
	}


	@Test
	public void testLogProbabilitySRILMBigram() throws IOException {
		// Compare LM perplexity to SRILM's Kneser-Ney implementation
		// Should be close, but SRILM uses a different implementation that our simpler version.
		List<List<String>> trainSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.train.txt"));
		AbstractNGramLanguageModel lm = trainBigram(trainSentenceList);

		ARPAModelLoader aml = new ARPAModelLoader(new File("testData/languageModels/brown.train.srilm.bigram.lm.arpa"));
		AbstractNGramLanguageModel rlm = aml.getLanguageModel();

		List<List<String>> testSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.test.txt"));

		PerplexityMetric ppTrain = new PerplexityMetric(trainSentenceList);
		PerplexityMetric ppTest = new PerplexityMetric(testSentenceList);

		System.out.println("Bigram, Training SRILM PP: " + ppTrain.score(rlm) + "\tTraining PP: " + ppTrain.score(lm));
		System.out.println("Bigram, Testing SRILM PP: " + ppTest.score(rlm) + "\tTesting PP: " + ppTest.score(lm));
//		assertEquals(ppTrain.score(aml.getLanguageModel()), ppTrain.score(lm), 1.0);
//		assertEquals(ppTest.score(aml.getLanguageModel()), ppTest.score(lm), 1.0);
	}


	@Test
	public void testBackoffBigram() throws IOException {
		// Compare LM perplexity to SRILM's Kneser-Ney implementation
		// Should be close, but SRILM uses a different implementation that our simpler version.
		List<List<String>> trainSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.train.txt"));
		trainSentenceList = trainSentenceList.subList(0, 1000);  System.err.println("WARNING: training a small model");
		KneserNeyModFixModel2 lm = trainBigram(trainSentenceList);
		BackoffLanguageModel blm = lm.calcBackoff();

		List<List<String>> testSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.test.txt"));

		PerplexityMetric ppTrain = new PerplexityMetric(trainSentenceList);
		PerplexityMetric ppTest = new PerplexityMetric(testSentenceList);

		System.out.println("Backoff Bigram Convert, Training SRILM PP: " + ppTrain.score(blm) + "\tTraining PP: " + ppTrain.score(lm));
		System.out.println("Backoff Bigram Convert, Testing SRILM PP: " + ppTest.score(blm) + "\tTesting PP: " + ppTest.score(lm));
		assertEquals(ppTrain.score(blm).perplexity, ppTrain.score(lm).perplexity, 0.000001);
		assertEquals(ppTest.score(blm).perplexity, ppTest.score(lm).perplexity, 0.000001);
	}


	private KneserNeyModFixModel2 trainTrigram(List<List<String>> referenceSentenceList) {
		ItemCounter<AbstractNGram> unigramCounter = new ItemCounter<AbstractNGram>();
		ItemCounter<AbstractNGram> bigramCounter = new ItemCounter<AbstractNGram>();
		ItemCounter<AbstractNGram> trigramCounter = new ItemCounter<AbstractNGram>();
		for(List<String> sentence : referenceSentenceList) {
			for(int i=0; i<sentence.size()-2; i++) {
				unigramCounter.increment(new Unigram(sentence.get(i)));
				bigramCounter.increment(new Bigram(sentence.get(i), sentence.get(i+1)));
				trigramCounter.increment(new Trigram(sentence.get(i), sentence.get(i+1), sentence.get(i+2)));
			}
			unigramCounter.increment(new Unigram(sentence.get(sentence.size()-1)));
			unigramCounter.increment(new Unigram(sentence.get(sentence.size()-2)));
			bigramCounter.increment(new Bigram(sentence.get(sentence.size()-2), sentence.get(sentence.size()-1)));
		}

		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(unigramCounter);
		orderToNGramCounter.add(bigramCounter);
		orderToNGramCounter.add(trigramCounter);
		return new KneserNeyModFixModel2(3, 10, orderToNGramCounter);
	}


	@Test
	public void testLogProbabilitySRILMTrigram() throws IOException {
		// Compare LM perplexity to SRILM's Kneser-Ney implementation
		// Should be close, but SRILM uses a different implementation that our simpler version.
		List<List<String>> trainSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.train.txt"));
		AbstractNGramLanguageModel lm = trainTrigram(trainSentenceList);

		ARPAModelLoader aml = new ARPAModelLoader(new File("testData/languageModels/brown.train.srilm.trigram.lm.arpa"));
		AbstractNGramLanguageModel rlm = aml.getLanguageModel();

		List<List<String>> testSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.test.txt"));

		PerplexityMetric ppTrain = new PerplexityMetric(trainSentenceList);
		PerplexityMetric ppTest = new PerplexityMetric(testSentenceList);

		System.out.println("Trigram, Training SRILM PP: " + ppTrain.score(rlm) + "\tTraining PP: " + ppTrain.score(lm));
		System.out.println("Trigram, Testing SRILM PP: " + ppTest.score(rlm) + "\tTesting PP: " + ppTest.score(lm));
//		assertEquals(ppTrain.score(aml.getLanguageModel()), ppTrain.score(lm), 1.0);
//		assertEquals(ppTest.score(aml.getLanguageModel()), ppTest.score(lm), 1.0);
	}


	@Test
	public void testBackoffTrigram() throws IOException {
		// Compare LM perplexity to SRILM's Kneser-Ney implementation
		// Should be close, but SRILM uses a different implementation that our simpler version.
		List<List<String>> trainSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.train.txt"));
		trainSentenceList = trainSentenceList.subList(0, 1000);  System.err.println("WARNING: training a small model");
		KneserNeyModFixModel2 lm = trainTrigram(trainSentenceList);
		AbstractNGramLanguageModel blm = lm.calcBackoff();

		List<List<String>> testSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.test.txt"));

		PerplexityMetric ppTrain = new PerplexityMetric(trainSentenceList);
		PerplexityMetric ppTest = new PerplexityMetric(testSentenceList);

		System.out.println("Backoff Trigram Convert, Training SRILM PP: " + ppTrain.score(blm) + "\tTraining PP: " + ppTrain.score(lm));
		System.out.println("Backoff Trigram Convert, Testing SRILM PP: " + ppTest.score(blm) + "\tTesting PP: " + ppTest.score(lm));
		assertEquals(ppTrain.score(blm).perplexity, ppTrain.score(lm).perplexity, 0.000001);
		assertEquals(ppTest.score(blm).perplexity, ppTest.score(lm).perplexity, 0.000001);
	}


	@Test
	public void testLogProbabilityUnigram() {
		// Test unigram estimation (verify on paper and with SRILM)
		ItemCounter<AbstractNGram> ngramCounter = new ItemCounter<AbstractNGram>();
		ngramCounter.set(new Unigram("a"), 10);
		ngramCounter.set(new Unigram("b"), 20);
		ngramCounter.set(new Unigram("c"), 40);
		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(ngramCounter);

		KneserNeyModFixModel2 lm = new KneserNeyModFixModel2(1, 10, orderToNGramCounter);
		assertEquals(Math.log10(10.0/70.0), lm.logProbability(new Unigram("a")), 0.0);
		assertEquals(Math.log10(20.0/70.0), lm.logProbability(new Unigram("b")), 0.0);
		assertEquals(Math.log10(40.0/70.0), lm.logProbability(new Unigram("c")), 0.0);
	}


	@Test
	public void testBackoffSimpleUnigram() {
		// Test unigram estimation (verify on paper and with SRILM)
		ItemCounter<AbstractNGram> ngramCounter = new ItemCounter<AbstractNGram>();
		ngramCounter.set(new Unigram("a"), 10);
		ngramCounter.set(new Unigram("b"), 20);
		ngramCounter.set(new Unigram("c"), 40);
		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(ngramCounter);

		KneserNeyModFixModel2 lm = new KneserNeyModFixModel2(1, 10, orderToNGramCounter);
		BackoffLanguageModel blm = lm.calcBackoff();

		assertEquals(lm.logProbability(new Unigram("a")), blm.logProbability(new Unigram("a")), 0.0);
		assertEquals(lm.logProbability(new Unigram("b")), blm.logProbability(new Unigram("b")), 0.0);
		assertEquals(lm.logProbability(new Unigram("c")), blm.logProbability(new Unigram("c")), 0.0);
	}


	@Test
	public void testBackoffSimpleBigram() {
		// Random string to test, just shuffled 20 each of a, b, c's on Random.org.
		ItemCounter<AbstractNGram> unigramCounter = new ItemCounter<AbstractNGram>();
		ItemCounter<AbstractNGram> bigramCounter = new ItemCounter<AbstractNGram>();
		String text = "b c a b c a c a c b b a c a b b b b b b c a c c a b b a b b b a a a a a c c a a b a b c b a c c c c a b c c c c a b a c";
		String [] words = text.split("\\s+");
		for(int i=0; i<words.length-1; i++) {
			unigramCounter.increment(new Unigram(words[i]));
			bigramCounter.increment(new Bigram(words[i], words[i+1]));
		}
		unigramCounter.increment(new Unigram(words[words.length-1]));

		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(unigramCounter);
		orderToNGramCounter.add(bigramCounter);

		KneserNeyModFixModel2 lm = new KneserNeyModFixModel2(2, 10, orderToNGramCounter);
		BackoffLanguageModel blm = lm.calcBackoff();
		assertEquals(lm.logProbability(new Unigram("a")), blm.logProbability(new Unigram("a")), 0.0);
		assertEquals(lm.logProbability(new Unigram("b")), blm.logProbability(new Unigram("b")), 0.0);
		assertEquals(lm.logProbability(new Unigram("c")), blm.logProbability(new Unigram("c")), 0.0);
		assertEquals(lm.logProbability(new Bigram("a","a")), blm.logProbability(new Bigram("a","a")), 0.0);
		assertEquals(lm.logProbability(new Bigram("a","b")), blm.logProbability(new Bigram("a","b")), 0.0);
		assertEquals(lm.logProbability(new Bigram("a","c")), blm.logProbability(new Bigram("a","c")), 0.0);
		assertEquals(lm.logProbability(new Bigram("b","a")), blm.logProbability(new Bigram("b","a")), 0.0);
		assertEquals(lm.logProbability(new Bigram("b","b")), blm.logProbability(new Bigram("b","b")), 0.0);
		assertEquals(lm.logProbability(new Bigram("b","c")), blm.logProbability(new Bigram("b","c")), 0.0);
		assertEquals(lm.logProbability(new Bigram("c","a")), blm.logProbability(new Bigram("c","a")), 0.0);
		assertEquals(lm.logProbability(new Bigram("c","b")), blm.logProbability(new Bigram("c","b")), 0.0);
		assertEquals(lm.logProbability(new Bigram("c","c")), blm.logProbability(new Bigram("c","c")), 0.0);
	}


	@Test
	public void testLog() {
		ItemCounter<AbstractNGram> ngramCounter = new ItemCounter<AbstractNGram>();
		ngramCounter.set(new Unigram("a"), 10);
		ngramCounter.set(new Unigram("b"), 20);
		ngramCounter.set(new Unigram("c"), 40);
		List<ItemCounter<AbstractNGram>> orderToNGramCounter = new ArrayList<ItemCounter<AbstractNGram>>();
		orderToNGramCounter.add(ngramCounter);

		KneserNeyModFixModel2 lm = new KneserNeyModFixModel2(1, 10, orderToNGramCounter);
		assertEquals(Double.NEGATIVE_INFINITY ,lm.log(0.0),0.0);
		assertEquals(-0.37675071, lm.log(0.42), 0.000000001);
		assertEquals(0.0, lm.log(1.0), 0.0);
		assertEquals(0.301029996, lm.log(2.0), 0.000000001);
		assertEquals(1.0, lm.log(10.0), 0.000000001);
		assertEquals(1.62324929, lm.log(42.0), 0.000000001);

		lm = new KneserNeyModFixModel2(1, 2, orderToNGramCounter);
		assertEquals(Double.NEGATIVE_INFINITY ,lm.log(0.0),0.0);
		assertEquals(-1.251538767, lm.log(0.42), 0.000000001);
		assertEquals(0.0, lm.log(1.0), 0.0);
		assertEquals(1.0, lm.log(2.0), 0.000000001);
		assertEquals(3.321928095, lm.log(10.0), 0.000000001);
		assertEquals(5.392317423, lm.log(42.0), 0.000000001);
	}
}
