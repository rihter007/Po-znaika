import codecs
import itertools


def load_dict(file):
    words = set()
    for line in codecs.open(file, 'r', 'cp1251'):
        words.add(line.strip())
    return words
    
def extract_words(word):
    words = []
    for i in range(3, len(word)):
        perms = itertools.permutations(word, i)
        perms_str = [''.join(p) for p in perms]
        words += perms_str
    return set(words)

def find_words(word, dict):
    words = set()
    new_words = extract_words(word)
    for small_word in new_words:
        if small_word in dict:
            words.add(small_word)
    return words

def print_list(list):
    print('[')
    for item in list:
        print(item + ', ')
    print(']')
    
dict = load_dict('dict_nouns.txt')
f = codecs.open('dict_over5words.txt', 'w', 'cp1251')

i = 0
for line in codecs.open('dict_7chars.txt', 'r', 'cp1251'):
    words = find_words(line.strip(), dict)
    if len(words) < 5:
        continue
    f.write(line.strip() + ' ' + str(len(words)) + ' ')
    f.write(', '.join(words))
    f.write('\r\n')
    i += 1
    if i >= 100:
        break
    