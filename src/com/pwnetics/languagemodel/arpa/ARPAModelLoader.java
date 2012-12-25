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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.pwnetics.languagemodel.Vocabulary;
import com.pwnetics.languagemodel.lm.BackoffLanguageModel;
import com.pwnetics.languagemodel.lm.NGramProbability;
import com.pwnetics.languagemodel.lm.NGramProbabilityBackoff;
import com.pwnetics.languagemodel.ngram.AbstractNGram;


/**
 * Loads an ARPA format backoff language model.
 *
 * The ARPA format is described at: http://www-speech.sri.com/projects/srilm/manpages/ngram-format.5.html
 *
 * Notable differences from the Sphinx 4 ARPA model loader:
 * <li> does not lowercase all n-gram words
 * <li> does not warn or otherwise restrict vocabulary to some predefined vocabulary
 * <li> reads a log probability of "-99" in a model file as "log(0)"
 *
 * @author romanows
 */
public class ARPAModelLoader {
	/*
	 * The loader code was copied from the SphinxDissect project, which is a refactored version of Sphinx 4.
	 * The ARPA loader code had been refactored, and this is a further refactoring.
	 *
	 * Decided to break it up into sections rather than running everything in one big loop.
	 * This should technically make it cleaner, although this appears to be arguable.
	 *
	 * Decided to use an enum to represent parser state.
	 */

	/** Value in ARPA file that represents the quantity "log(0)" */
	private static final double FILE_LOG_ZERO = -99.0;

	/** Value in languge model that represents the quantity "log(0)" */
	public static final double LOG_ZERO = Double.NEGATIVE_INFINITY;

    private static final Pattern splitNGramLenPattern = Pattern.compile("\\s+|=");
    private static final Pattern whitespacePattern = Pattern.compile("\\s+");

	private static enum FileParsingState {
		READING_NGRAMS,
		LOOKING_FOR_NEXT_NGRAMS,
		AFTER_NGRAMS,
		AFTER_END
	};


	/** File read as the ARPA language model file */
	private final File modelFile;

	/** Language model created from the ARPA file */
	private final BackoffLanguageModel backoffLanguageModel;

	/** Vocabulary created when creating the language model */
	private final Vocabulary vocabulary;


	/**
	 * Load an ARPA format backoff language model file.
	 * See class javadoc for more details.
	 *
	 * Reads all order ngrams in the model file.
	 *
	 * @param modelFile ARPA language model file to read and parse
	 * @throws IllegalArgumentException on problems reading or parsing the file
	 */
	public ARPAModelLoader(File modelFile) {
		this(modelFile, -1);
	}


	/**
	 * Load an ARPA format backoff language model file.
	 * See class javadoc for more details.
	 *
	 * @param modelFile ARPA language model file to read and parse
	 * @param readDepth do not read model information of order above this quantity; a value less than zero will read all model information.
	 * This is useful when using a higher-order model as a lower-order model.
	 * E.g., given an ARPA trigram model, setting readDepth = 2 will load a bigram BackoffLanguageModel.
	 *
	 * @throws IllegalArgumentException on problems reading or parsing the file
	 */
	public ARPAModelLoader(File modelFile, int readDepth) {
		this.modelFile = modelFile;
		vocabulary = new Vocabulary();

		String line;
        int lineNumber = 0;
        FileParsingState state = null;
        int order = 0;
        int orderCount = -1;
        int count = 0;
        int maxDepth = 0;
        boolean [] orderSeen = null;

    	Map<Integer, Integer> ngramLenMap = new HashMap<Integer, Integer>();

        BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(modelFile));

			// Scan for the start of the data section
	        while((line = br.readLine()) != null) {
	        	lineNumber++;
	    		if(line.equals("\\data\\")) {
	    			break;
	    		}
	        }

	        // Scan for ngram section lengths
	        while((line = br.readLine()) != null) {
	        	lineNumber++;
	            if(line.startsWith("ngram")) {
	            	String [] tok = splitNGramLenPattern.split(line);
	            	if(tok.length != 3) {
	            		throw new IOException("Corrupt Language Model " + modelFile.getPath() + " at line " + lineNumber + ": invalid ngram length declaration");
	            	}

	            	try {
	            		int depth = Integer.parseInt(tok[1]);
	            		int len = Integer.parseInt(tok[2]);
	                	if(depth > maxDepth) {
	                		maxDepth = depth;
	                	}
	                	ngramLenMap.put(depth, len);
	            	} catch (NumberFormatException e) {
	            		throw new IOException("Corrupt Language Model " + modelFile.getPath() + " at line " + lineNumber + ": problem parsing ngram section length", e);
	            	}
	            } else if(line.endsWith("-grams:")) {
	            	// Read order information and maintain that state
	            	readDepth = readDepth < 1 ? maxDepth : Math.min(readDepth, maxDepth);
	    			order = Integer.parseInt(line.substring(1).split("-")[0]);
	    			orderCount = ngramLenMap.get(order);
	    			orderSeen = new boolean[maxDepth];
	    			orderSeen[order-1] = true;
	    			state = FileParsingState.READING_NGRAMS;
	    			break;
	            }
	        }

	        // These maps hold the ngram-probability-backoff tuples from the ARPA model file
	    	Map<AbstractNGram, NGramProbability> highOrderNGrams = new HashMap<AbstractNGram, NGramProbability>();
	    	List<Map<AbstractNGram, NGramProbabilityBackoff>> lowerOrderToNGrams = null;
	    	if(readDepth > 1) {
	    		lowerOrderToNGrams = new ArrayList<Map<AbstractNGram, NGramProbabilityBackoff>>();
				for(int i=1; i<readDepth; i++) {
					lowerOrderToNGrams.add(new HashMap<AbstractNGram, NGramProbabilityBackoff>());
				}
	    	}

	        // Left above state having read a "X-grams" section header, onto reading the counts and future "X-grams" sections
	        while((line = br.readLine()) != null && !state.equals(FileParsingState.AFTER_NGRAMS)) {
	        	lineNumber++;
	        	if(state.equals(FileParsingState.LOOKING_FOR_NEXT_NGRAMS)) {
	    			if(line.endsWith("-grams:")) {
	    				state = FileParsingState.READING_NGRAMS;

	                	// Read order information and maintain that state
	        			order = Integer.parseInt(line.substring(1).split("-")[0]);
	        			orderCount = ngramLenMap.get(order);
	        			orderSeen[order-1] = true;
	        			count = 0;
	    			} else if(!line.trim().isEmpty()) {
	    				throw new IOException("Corrupt Language Model " + modelFile.getPath() + " at line " + lineNumber + ": unexpected information in ngrams section (maybe incorrect ngram length in header?)");
	    			}
	        	} else {
	        		// Parse the ngram line
	        		String [] tok = whitespacePattern.split(line);
	        		if(!(tok.length == (2 + order) || tok.length == (1 + order))) {
	        			throw new IOException("Corrupt Language Model " + modelFile.getPath() + " at line " + lineNumber + ": ngram entry has incorrect number of items (maybe incorrect ngram length in header or ngram entry contains extra information after words and backoff weights?)");
	        		}

	        		// Ignore ngrams of order above our readDepth
	        		if(order <= readDepth) {
		        		// Gather the words into an ngram
		        		String [] words = new String[order];
		        		for(int i=1; i<=order; i++) {
		        			words[i-1] = vocabulary.addCanonical(tok[i]);
		        		}
		        		AbstractNGram ngram = AbstractNGram.factory(words);

		        		// Store the ngram and associated probs/backoffs
		        		double log10Prob = Double.parseDouble(tok[0]);
		        		if(order < readDepth) {
		        			double log10Backoff;
		        			if(tok.length == (2 + order)) {
		        				log10Backoff = Double.parseDouble(tok[tok.length-1]);
			        			if(log10Backoff == FILE_LOG_ZERO) {
			        				log10Backoff = LOG_ZERO;
			        			}
		        			} else {
		        				// Absence of a backoff prob where there should be a backoff prob we'll just set it as zero likelihood
		        				log10Backoff = LOG_ZERO;
		        			}
		        			lowerOrderToNGrams.get(order-1).put(ngram, new NGramProbabilityBackoff(log10Prob, log10Backoff));
		        		} else {
		        			highOrderNGrams.put(ngram, new NGramProbability(log10Prob));
		        		}
	        		}
	                count++;

	                if(count >= orderCount) {
	                	boolean ngramsRemain = false;
	                	for(boolean b : orderSeen) {
	                		if(!b) {
	                			ngramsRemain = true;
	                			break;
	                		}
	                	}
	                	if(ngramsRemain) {
	                		state = FileParsingState.LOOKING_FOR_NEXT_NGRAMS;
	                	} else {
	                		state = FileParsingState.AFTER_NGRAMS;
	                	}
	                }
	            }
	        }

	    	// Search for end marker of ARPA file
	        while((line = br.readLine()) != null && !state.equals(FileParsingState.AFTER_END)) {
	        	lineNumber++;
	    		if("\\end\\".equals(line)) {
	    			state = FileParsingState.AFTER_END;
	    		}
	        }

	        if(!state.equals(FileParsingState.AFTER_END)) {
				throw new IOException("Corrupt Language Model " + modelFile.getPath() + " at line " + lineNumber + ": reached end of file without reading all required information");
	        }

	        backoffLanguageModel = new BackoffLanguageModel(10.0, highOrderNGrams, lowerOrderToNGrams);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}


	/**
	 * Get the ARPA language model file used to instantiate this object.
	 * @return the ARPA language model file used to instantiate this object
	 */
	public File getModelFile() {
		return modelFile;
	}


	/**
	 * Get the language model created when the ARPA language model file was parsed.
	 * @return the language model created when the ARPA language model file was parsed
	 */
	public BackoffLanguageModel getLanguageModel() {
		return backoffLanguageModel;
	}


	/**
	 * Get the vocabulary created when building the language model.
	 * @return the vocabulary created when building the language model
	 */
	public Vocabulary getVocabulary() {
		return vocabulary;
	}
}
