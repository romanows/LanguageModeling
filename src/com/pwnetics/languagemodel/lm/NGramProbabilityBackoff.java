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

/**
 * Container for an ngram probability/backoff pair
 * @author romanows
 */
public class NGramProbabilityBackoff extends NGramProbability {

	/** Backoff probability associated with an ngram */
	protected double backoff;


	/**
	 * Constructor.
	 * Sets the backoff probability to {@link Double#NaN}.
	 * @param probability {@link NGramProbability#probability}
	 */
	public NGramProbabilityBackoff(double probability) {
		super(probability);
		backoff = Double.NaN;
	}


	/**
	 * Constructor.
	 * @param probability {@link NGramProbability#probability}
	 * @param backoff {@link #backoff}
	 */
	public NGramProbabilityBackoff(double probability, double backoff) {
		super(probability);
		this.backoff = backoff;
	}


	/**
	 * @return the backoff
	 */
	public double getBackoff() {
		return backoff;
	}


	/**
	 * @param backoff the backoff to set
	 */
	public void setBackoff(double backoff) {
		this.backoff = backoff;
	}
}
