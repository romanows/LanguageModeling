The 'user2020.split.training.lm.arpa' language model is an SRILM language model that I created from some kind of training data.

The 'brown.train.srilm.bigram.lm.arpa' language model was trained with SRILM like:
	./ngram-count -text brown.sentences.nltk.train.txt -order 2 -kndiscount2 -lm brown.train.srilm.bigram.lm.arpa

The 'brown.train.srilm.trigram.lm.arpa' language model was trained with SRILM like:
	./ngram-count -text brown.sentences.nltk.train.txt -order 3 -kndiscount3 -lm brown.train.srilm.trigram.lm.arpa
