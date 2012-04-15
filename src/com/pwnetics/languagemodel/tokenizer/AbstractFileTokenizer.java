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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;


/**
 * Base class for Tokenizers that take their input text from a file.
 * Different file encodings are supported through Java's file encoding system.
 *
 * TODO: Better IOException handling needed; right now there is no explicit closing of files when an IOException occurs (although the user can do this by calling {@link #close()}.
 *
 * @author romanows
 */
public abstract class AbstractFileTokenizer implements ITokenizer {

	/** The buffered reader through which we access the file stream */
	private final BufferedReader br;

	/** True if the file stream has been completely read and closed */
	private boolean isEOF;

	/** Holds tokens yet to output */
	private Queue<String> tokenQueue;


	/**
	 * Constructor.
	 * Uses default file encoding.
	 * @param srcFile file to tokenize
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public AbstractFileTokenizer(File srcFile) throws FileNotFoundException, UnsupportedEncodingException {
		this(srcFile, Charset.defaultCharset().name());
	}


	/**
	 * Constructor.
	 * @param srcFile file to tokenize
	 * @param charsetName character encoding of the file; see {@link Charset} for file encoding options.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public AbstractFileTokenizer(File srcFile, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), charsetName));
		isEOF = false;
		tokenQueue = new ArrayDeque<String>();
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {

			@Override
			public boolean hasNext() {
				try {
					return AbstractFileTokenizer.this.hasNext();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String next() {
				try {
					String next = AbstractFileTokenizer.this.next();
					if(next == null) {
						throw new NoSuchElementException();
					}
					return next;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}


	/**
	 * Close the file stream.
	 * Call this if you want to stop reading tokens before reaching the end of the file.
	 * @throws IOException
	 */
	public void close() throws IOException {
		br.close();
		isEOF = true;
	}


	/**
	 * Tokenize a line of text.
	 * Override this to produce different tokenizations.
	 * @param line line of text
	 * @return tokenized text
	 */
	abstract protected List<String> tokenizeLine(String line);


	/**
	 * True if there are more tokens to be returned, otherwise false.
	 * @return True if there are more tokens to be returned, otherwise false
	 * @throws IOException
	 */
	private boolean hasNext() throws IOException {
		if(isEOF) {
			return false;
		}

		if(!tokenQueue.isEmpty()) {
			return true;
		}

		while(tokenQueue.isEmpty()) {
			String line;
			line = br.readLine();
			if(line == null) {
				isEOF = true;
				br.close();
				return false;
			}

			tokenQueue.addAll(tokenizeLine(line));
		}
		return true;
	}


	/**
	 * Get the next token from the file stream.
	 * @return the next token, or null if there are no more tokens available
	 * @throws IOException
	 */
	private String next() throws IOException {
		if(!hasNext()) {
			return null;
		}
		return tokenQueue.poll();
	}
}
