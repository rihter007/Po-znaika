from django.shortcuts import render
from django.shortcuts import render_to_response
#from django.template.defaultfilters import linebreaksbr

# Create your views here.

def CompanyPage(request):
   return render_to_response('company.html')