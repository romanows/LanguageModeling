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


package com.pwnetics.languagemodel.arpa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pwnetics.languagemodel.Vocabulary;

public class TestARPAModelLoader {

	@Test
	public void testARPAModelLoader() {
		ARPAModelLoader aml = new ARPAModelLoader(new File("testData/languageModels/user2020.split.training.lm.arpa"), 3);
		Vocabulary vocabulary = aml.getVocabulary();

		List<String> sentence = new ArrayList<String>();
		sentence.add(vocabulary.getCanonical("<s>"));
		sentence.add(vocabulary.getCanonical("yes"));
		sentence.add(vocabulary.getCanonical("</s>"));
		double logProb = aml.getLanguageModel().logProbability(sentence);

		// SRILM figures this out as: log10(P(yes|<s>) * P(</s>|<s>,yes)) =-> -1.145158 + -0.1574371 = -1.3025951
		assertEquals(-1.3025951, logProb, 1e-10);


		// Sentence where we need to have a sliding window, but no backoff
		sentence.clear();
		sentence.add(vocabulary.getCanonical("<s>"));
		sentence.add(vocabulary.getCanonical("and"));
		sentence.add(vocabulary.getCanonical("a"));
		sentence.add(vocabulary.getCanonical("</s>"));
		logProb = aml.getLanguageModel().logProbability(sentence);

		// Should be and|<s> + a|<s>,and + </s>|and,a = -1.045542 + -0.3802426 + -1.453959 = -2.8797436
		assertEquals(-2.8797436, logProb, 1e-10);


		// What about an all-backoff sentence?  Each step in the sentence below backs off to the unigram prog * the backoff prob of the preceding unigram.
		sentence.clear();
		sentence.add(vocabulary.getCanonical("<s>"));
		sentence.add(vocabulary.getCanonical("zombie"));
		sentence.add(vocabulary.getCanonical("red"));
		sentence.add(vocabulary.getCanonical("zombie"));
		sentence.add(vocabulary.getCanonical("</s>"));
		logProb = aml.getLanguageModel().logProbability(sentence);

		// <s> zombie red zombie </s> = (-0.6852149+-3.994933) + (-0.1806371+-2.633205) + (-0.3080671+-3.994933) + (-0.1806371+-0.7425652) = -12.7201924
		assertEquals(-12.7201924, logProb, 1e-10);


		// This sentence is just randomly picked by concatenating strings from the arpa file, we check against what srilm says
		sentence.clear();
		for(String s : new String[] {"<s>", "egyptian", "where", "should", "table", "is", "wooden", "there", "is", "fruit", "neither", "motifs", "see", "a", "coin", "to", "a", "different", "with", "a", "crown", "</s>"}) {
			sentence.add(vocabulary.getCanonical(s));
		}
		logProb = aml.getLanguageModel().logProbability(sentence);

		/*
		 * This was originally failing for an odd reason.
		 * SRILM doesn't list a backoff probability for "<s> egyptian".
		 * This should be impossible (?) because for a trigram model when everything is surrounded by "<s>" and "</s>" tags, at least the string "<s> egyptian </s>" would appear if no other third word was present.
		 * In any case, SRILM doesn't list a backoff probability when there is no higher order example, so our model should realize this and backoff to just "egyptian".
		 */
		assertEquals(-39.5724, logProb, 1e-3);
	}
}
