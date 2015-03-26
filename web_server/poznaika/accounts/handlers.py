import datetime

from django.shortcuts import render
from django.shortcuts import render_to_response
from django.contrib import auth
from django.contrib.auth.models import User
from django.http import HttpResponseRedirect
from django.http import HttpResponse
from django.http import HttpResponseForbidden
from django.http import HttpResponseBadRequest
from django.template import RequestContext
from django.utils import timezone

from forms import RegisterForm
from forms import LoginForm
from forms import AddNameForm
from forms import DeleteNameForm
from models import Exercise
from models import Course
from models import Mark
from models import License
from models import Class
from models import StudyHead
from views import IsHead


# Create your views here.

def DiaryRequest(request):
    return CreateResponse(DiaryHandler(request))

def MarkRequest(request):
    return CreateResponse(MarkHandler(request))
    
def LicenseRequest(request):
    return CreateResponse(LicenseHandler(request))
    
def CreateResponse(handler):
    try:
        userStr = handler.request.META['HTTP_LOGIN'] # required for all requests
        pwdStr = handler.request.META['HTTP_PASSWORD'] # same
        handler.ParseRequestData()
    except Exception as e:
        print "Data error:", e
        return MakeResponse(400, e)
    
    try:
        handler.user = User.objects.get(username=userStr)
        handler.FindRequiredObjects()
    except Exception as e:
        print "Find error:", e
        return MakeResponse(404, e)
        
    try:
        valid = handler.user.check_password(pwdStr)
        if not valid:
            return MakeResponse(401, "Bad password")
    except Exception as e:
        print "Auth error:", e
        return MakeResponse(401, e)

    try:
        data = handler.FormResponseData()
    except Exception as e:
        print "Processing error:", e
        return MakeResponse(403, e)
    
    return HttpResponse(data)

############################################

class BaseHandler(object):
    def __init__(self, rq):
        self.request = rq
        
class MarkHandler(BaseHandler):
    def ParseRequestData(self):
        self.exerStr = self.request.META['HTTP_EXERCISE']
        scoreStr = self.request.META['HTTP_SCORE']
        dateStr = self.request.META['HTTP_DATE']

        self.score = int(scoreStr)
        self.date = datetime.datetime.strptime(dateStr, "%a, %d %b %Y %H:%M:%S %Z")
    
    def FindRequiredObjects(self):
        self.exer = Exercise.objects.get(Name=self.exerStr)

    def FormResponseData(self):
        mark = Mark(ForUser=self.user, ForExercise=self.exer,
            Score = self.score, DateTime = self.date)
        mark.save()
        return "OK"

class DiaryHandler(BaseHandler):
    def ParseRequestData(self):
        self.exerStr = self.request.META.get('HTTP_EXERCISE')
        self.startStr = self.request.META.get('HTTP_START-DATE')
        self.endStr = self.request.META.get('HTTP_END-DATE')
        valid = self.exerStr or (self.startStr and self.endStr)
        if not valid:
            raise IndexError("No exercise number or dates")

    def FindRequiredObjects(self):
        self.exer = Exercise.objects.get(Name=self.exerStr)

    def FormResponseData(self):
        data = '['
        try:
            marks = Mark.objects.filter(ForUser=self.user, ForExercise=self.exer)
            for mark in marks:
                data += '{"User":"' + self.user.username + '",'
                data += '"Exercise":"' + self.exer.Name + '",'
                data += '"Score":'
                data += '"' + str(mark.Score) + '",'
                data += '"Date":"' + str(mark.DateTime) + '"'
                data += '}, '
        except Exception as e:
            data += '"-1"'
        data += ']'
        return data

class LicenseHandler(BaseHandler):
    def ParseRequestData(self):
        pass
        
    def FindRequiredObjects(self):
        pass

    def FormResponseData(self):
        data = '{"Type":'
        try:
            license = License.objects.get(ForUser=self.user)
        except Exception as e:
            #print e - this string fails at real server
            data += '"Trial"'
        else:
            data += '"Commercial",'
            data += '"StartDate":"' + str(license.StartDate)  + '",'
            data += '"EndDate":"' + str(license.EndDate)  + '",'
            data += '"Status":'
            now = datetime.date.today()
            active = license.StartDate <= now and now <= license.EndDate
            data += active and '"Active"' or '"Expired"'
        data += '}'
        return data

def MakeResponse(code, reason):
    response = HttpResponse()
    response.status_code = code
    response.reason_phrase = reason
    return response
