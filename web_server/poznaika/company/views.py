from django.shortcuts import render
from django.shortcuts import render_to_response
#from django.template.defaultfilters import linebreaksbr

# Create your views here.

def ContactsPage(request):
   return render_to_response('contacts.html')
   
def AboutPage(request):
   return render_to_response('about.html')