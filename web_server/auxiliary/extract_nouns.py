import re
import codecs

f = codecs.open('dict_nouns.txt', 'w', 'cp1251')

for line in codecs.open('dict_all.txt', 'r', 'cp1251'):
    match = re.search(r'\d+\s+(\S+)\s+(\S+).*', line)
    if match and match.group(1) and match.group(2) and match.group(2) == 's':
        #print match.group(1)
        f.write(match.group(1)+'\r\n')