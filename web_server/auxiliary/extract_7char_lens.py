import codecs

f = codecs.open('dict_7chars.txt', 'w', 'cp1251')

for line in codecs.open('dict_nouns.txt', 'r', 'cp1251'):
    if len(line) >= 7 + 2:  # 2 is CrLf
        f.write(line)