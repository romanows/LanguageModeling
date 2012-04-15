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

import com.pwnetics.languagemodel.PerplexityMetric;
import com.pwnetics.languagemodel.arpa.ARPAModelLoader;


/**
 * Not really a unit test; we'll test the perplexity of a backoff model created in SRILM.
 * In fact, this tests the backoff model, the ARPA loader, and the perplexity calculation.
 * @author romanows
 */
public class TestBackoffLanguageModelPerplexity {

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


	@Test
	public void testBackoffLanguageModelPerplexity() throws IOException {
		List<List<String>> referenceSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.train.txt"));
		PerplexityMetric pp = new PerplexityMetric(referenceSentenceList);

		ARPAModelLoader aml = new ARPAModelLoader(new File("testData/languageModels/brown.train.srilm.bigram.lm.arpa"));
		BackoffLanguageModel blm = aml.getLanguageModel();
		assertEquals(115.619, pp.score(blm).perplexity, 0.001); // reference score from SRILM bigram on training data in testData/results directory

		aml = new ARPAModelLoader(new File("testData/languageModels/brown.train.srilm.trigram.lm.arpa"));
		BackoffLanguageModel tlm = aml.getLanguageModel();
		assertEquals(87.4618, pp.score(tlm).perplexity, 0.001); // reference score from SRILM trigram on training data in testData/results directory


		// Now compute perplexity on test sentences.
		// This is tricky, as they have vocabulary that isn't in our language model.
		// We will follow the SRILM route and consider any OOV words to occur with P(OOV) = 1 for this perplexity calculation.
		// Justification is given in their FAQ at: http://www-speech.sri.com/projects/srilm/manpages/srilm-faq.7.html
		referenceSentenceList = readSentences(new File("testData/trainingText/brown.sentences.nltk.test.txt"));
		pp = new PerplexityMetric(referenceSentenceList);
		assertEquals(311.505, pp.score(blm).perplexity, 0.001);
		assertEquals(287.046, pp.score(tlm).perplexity, 0.001);
	}
}
