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
from models import Exercise
from models import Course
from models import Mark
from models import License
import add

# Create your views here.

def MakeResponse(code, reason):
    response = HttpResponse()
    response.status_code = code
    response.reason_phrase = reason
    return response

def MarkRequest(request):
    try:
        userStr = request.META['HTTP_LOGIN']
        exerStr = request.META['HTTP_EXERCISE']
        scoreStr = request.META['HTTP_SCORE']
        dateStr = request.META['HTTP_DATE']
        score = int(scoreStr)
        date = datetime.datetime.strptime(dateStr, "%a, %d %b %Y %H:%M:%S %Z")
    except Exception as e:
        print "Data error:", e
        return MakeResponse(400, e)
    
    try:
        user = User.objects.get(username=userStr)
        exer = Exercise.objects.get(Name=exerStr)
    except Exception as e:
        print "Find error:", e
        return MakeResponse(404, e)

    try:
        updateValues = { 'Score': score, 'DateTime': date }
        mark, created = Mark.objects.get_or_create(
            ForUser=user, ForExercise=exer, defaults=updateValues)
        if not created:
            mark.Score = score
            mark.DateTime = date
            mark.save()
    except Exception as e:
        print "Processing error:", e
        return MakeResponse(403, e)
    
    return MakeResponse(200, "OK")

def DiaryRequest(request):
    try:
        userStr = request.META['HTTP_LOGIN']
        exerStr = request.META.get('HTTP_EXERCISE')
        startStr = request.META.get('HTTP_START-DATE')
        endStr = request.META.get('HTTP_END-DATE')
        #start = datetime.datetime.strptime(dateStr, "%a, %d %b %Y %H:%M:%S %Z")
    except Exception as e:
        print "Data error:", e
        return MakeResponse(400, e)
    
    try:
        user = User.objects.get(username=userStr)
        exer = Exercise.objects.get(Name=exerStr)
    except Exception as e:
        print "Find error:", e
        return MakeResponse(404, e)

    try:
        data = '[{'
        data += '"User":"' + user.username + '",'
        data += '"Exercise":"' + exer.Name + '",'
        data += '"Score":'
        try:
            mark = Mark.objects.get(ForUser=user, ForExercise=exer)
        except Exception as e:
            data += '"-1"'
        else:
            data += '"' + str(mark.Score) + '",'
            data += '"Date":"' + str(mark.DateTime) + '"'
        data += '}]'
    except Exception as e:
        print "Processing error:", e
        return MakeResponse(403, e)
    
    return HttpResponse(data)

def LicenseRequest(request):
    try:
        userStr = request.META['HTTP_LOGIN']
    except Exception as e:
        print "Data error:", e
        return MakeResponse(400, e)
    
    try:
        user = User.objects.get(username=userStr)
    except Exception as e:
        print "Find error:", e
        return MakeResponse(404, e)

    try:
        data = '{"Type":'
        try:
            license = License.objects.get(ForUser=user)
        except Exception as e:
            print e
            data += '"Trial"'
        else:
            data += '"Commerce",'
            data += '"StartDate":"' + str(license.StartDate)  + '",'
            data += '"EndDate":"' + str(license.EndDate)  + '",'
            data += '"Status":'
            now = datetime.date.today()
            active = license.StartDate <= now and now <= license.EndDate
            data += active and '"Active"' or '"Expired"'
        data += '}'
    except Exception as e:
        print "Processing error:", e
        return MakeResponse(403, e)
    
    return HttpResponse(data)
    