# Overview
This project has code to demonstrate how statistical backoff language models are trained and applied.
ARPA-format language models can be loaded and used, and new language models can be trained with a version of the Kneser-Ney language model training algorithm.  
A document explaining the theoretical and mathematical aspects of backoff language models and Kneser-Ney training is included in the <tt>doc/</tt> directory.

This code should not be used for actual language model training; use <a href="http://www.speech.sri.com/projects/srilm/">SRILM</a> instead.
This code may be useful as an educational aid towards understanding language modeling and aspects of the Kneser-Ney approach that aren't clear in the literature.
Despite these caveats, the light testing included in this project indicates that the perplexity of the models trained by this code is at least as good as the perplexity of models generated by SRILM. 

Continued development will be aimed at correcting bugs, clarifying fundamental concepts in the code and documentation, and improving the efficiency of the current algorithms.
In particular, creating a Kneser-Ney backoff language model is extremely inefficient, and I'd like to fix and document a more efficient method. 

Brian Romanowski  
romanows@gmail.com  


# Dependencies
This code requires the <a href="https://github.com/romanows/ItemCounter">ItemCounter</a> project to be added to the build path.


# License
This code is licensed under one of the BSD variants, please see LICENSE.txt for full details.
The language model document is released under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/">Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>.