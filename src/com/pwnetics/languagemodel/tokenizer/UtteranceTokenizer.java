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
import java.util.List;


/**
 * Extends {@link WhitespaceTokenizer} to produce a stream of whitespace-delimited tokens, with beginning and ending line tags, from a file.
 * This can be used to insert "<s>" and "</s>" tokens (or any other token strings) to mark the beginning and end of utterances when utterances appear on a single line in a file.
 *
 * @author romanows
 */
public class UtteranceTokenizer extends WhitespaceTokenizer {

	private static final String DEFAULT_BEGIN_UTTERANCE = "<s>";
	private static final String DEFAULT_END_UTTERANCE = "</s>";

	/** String to represent the beginning-of-utterance token */
	private final String beginUtterance;

	/** String to represent the end-of-utterance token */
	private final String endUtterance;

	/**
	 * True if we do not add beginning and ending tokens when the given line is empty or contains only whitespace.
	 * If false, we may produced tokenized lines that are simply [beginUtterance,endUtterance].
	 */
	private final boolean skipEmptyLines;


	/**
	 * Create an utterance tokenizer with default behaviors of inserting &lt;s&gt; and &lt;/s&gt; only when a line has at least one token.
	 * Uses default file encoding.
	 * @param srcFile file to tokenize
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public UtteranceTokenizer(File srcFile) throws FileNotFoundException, UnsupportedEncodingException {
		this(srcFile, DEFAULT_BEGIN_UTTERANCE, DEFAULT_END_UTTERANCE, true);
	}


	/**
	 * Create an utterance tokenizer with default behaviors of inserting &lt;s&gt; and &lt;/s&gt;.
	 * Uses default file encoding.
	 * @param srcFile file to tokenize
	 * @param skipEmptyLines if true, will not add utterance delimiter tokens when line of text contains no tokens; otherwise, will always add utterance delimiter tokens.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public UtteranceTokenizer(File srcFile, boolean skipEmptyLines) throws FileNotFoundException, UnsupportedEncodingException {
		this(srcFile, DEFAULT_BEGIN_UTTERANCE, DEFAULT_END_UTTERANCE, skipEmptyLines);
	}


	/**
	 * Create an utterance tokenizer with user-supplied utterance delimiters and behavior.
	 * Uses default file encoding.
	 * @param srcFile file to tokenize
	 * @param beginUtterance token to use at the beginning of an utterance
	 * @param endUtterance token to use at the end of an utterance
	 * @param skipEmptyLines if true, will not add utterance delimiter tokens when line of text contains no tokens; otherwise, will always add utterance delimiter tokens.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public UtteranceTokenizer(File srcFile, String beginUtterance, String endUtterance, boolean skipEmptyLines) throws FileNotFoundException, UnsupportedEncodingException {
		super(srcFile);
		this.beginUtterance = beginUtterance;
		this.endUtterance = endUtterance;
		this.skipEmptyLines = skipEmptyLines;
	}


	/**
	 * Create an utterance tokenizer with user-supplied utterance delimiters and behavior.
	 * @param srcFile file to tokenize
	 * @param charsetName character encoding of the file; see {@link Charset} for file encoding options.
	 * @param beginUtterance token to use at the beginning of an utterance
	 * @param endUtterance token to use at the end of an utterance
	 * @param skipEmptyLines if true, will not add utterance delimiter tokens when line of text contains no tokens; otherwise, will always add utterance delimiter tokens.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public UtteranceTokenizer(File srcFile, String charsetName, String beginUtterance, String endUtterance, boolean skipEmptyLines) throws FileNotFoundException, UnsupportedEncodingException {
		super(srcFile, charsetName);
		this.beginUtterance = beginUtterance;
		this.endUtterance = endUtterance;
		this.skipEmptyLines = skipEmptyLines;
	}


	/*
	 * (non-Javadoc)
	 * @see com.pwnetics.languagemodel.WhitespaceTokenizer#tokenizeLine(java.lang.String)
	 */
	@Override
	protected List<String> tokenizeLine(String line) {
		List<String> s = super.tokenizeLine(line);
		if(skipEmptyLines && s.isEmpty()) {
			return s;
		}
		s.add(0, beginUtterance);
		s.add(endUtterance);
		return s;
	}
}
