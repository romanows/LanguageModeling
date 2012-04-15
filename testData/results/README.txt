SRILM used to generate the result files like:
	./ngram -lm brown.train.srilm.bigram.lm.arpa -ppl brown.sentences.nltk.test.txt -debug 1 > brown.train.srilm.bigram.testppl1.txt
	./ngram -lm brown.train.srilm.bigram.lm.arpa -ppl brown.sentences.nltk.test.txt > brown.train.srilm.bigram.testppl0.txt
