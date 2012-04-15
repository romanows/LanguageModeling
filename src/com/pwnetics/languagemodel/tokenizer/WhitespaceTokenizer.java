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


package com.pwnetics.languagemodel.tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Produces a stream of whitespace-delimited tokens from a file.
 * Tokens are delimited by unicode space characters.
 *
 * @author romanows
 */
public class WhitespaceTokenizer extends AbstractFileTokenizer {

	/**
	 * Constructor.
	 * Uses default file encoding.
	 * @param srcFile file to tokenize
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public WhitespaceTokenizer(File srcFile) throws FileNotFoundException, UnsupportedEncodingException {
		super(srcFile);
	}


	/**
	 * Constructor.
	 * @param srcFile file to tokenize
	 * @param charsetName character encoding of the file; see {@link Charset} for file encoding options.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public WhitespaceTokenizer(File srcFile, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		super(srcFile, charsetName);
	}


	/**
	 * Tokenize a line of text.
	 * Override this to produce different tokenizations.
	 * @param line line of text
	 * @return tokenized text
	 */
	@Override
	protected List<String> tokenizeLine(String line) {
		List<String> tokens = new ArrayList<String>();

		// Tokenize by unicode whitespace
		int i = 0;
		for(int j=0; j<line.length(); j++) {
			if(Character.isSpaceChar(line.codePointAt(j))) {
				if(j > i) {
					tokens.add(line.substring(i, j));
					i = j + 1;
				} else {
					i = j + 1;
				}
			}
		}
		if(line.length() > i) {
			tokens.add(line.substring(i, line.length()));
		}
		return tokens;
	}
}
