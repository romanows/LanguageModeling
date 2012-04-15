Essentially wrote the data from NLTK via:

from nltk.corpus import brown
from nltk.tokenize.punkt import PunktWordTokenizer
bsf = open('brown.sentences.nltk.txt','w')
for s in brown.sents():
    bsf.write('<s> ' + (' '.join(s)) + ' </s>\n')
bsf.close()

The full corpus was then split into a traning portion and a testing portion.
I randomly sorted the sentences in OpenOffice Calc, and set aside 8000 for testing.
