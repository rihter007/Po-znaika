import sys
import urllib2

# Prepare data

#BaseUrl = 'http://localhost/accounts/'
BaseUrl = 'http://po-znaika.ru/accounts/'
fullUrl = BaseUrl + sys.argv[1]

headers = {}
for i in range(2, len(sys.argv), 2):
    headers[sys.argv[i]] = sys.argv[i+1]
    
print fullUrl
print headers

# Send request

req = urllib2.Request(fullUrl, None, headers)
try:
    handler = urllib2.urlopen(req)
except Exception as e:
    print "Error:", e
else:
    print handler.getcode() 
    print handler.read()
