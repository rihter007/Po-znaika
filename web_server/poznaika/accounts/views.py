from django.shortcuts import render
from django.shortcuts import render_to_response
from django.contrib import auth
from django.contrib.auth.models import User
from django.http import HttpResponseRedirect
from django.http import HttpResponse
from django.template import RequestContext

from forms import RegisterForm
from forms import LoginForm
from models import Exercise
from models import Course
from models import Mark

# Create your views here.

def MakeDiary(user):
    diariesList = ""
    
    courses = Course.objects.all()
    for course in courses:
        diariesList += "* "+course.Name+"\r\n"
        exercises = course.Exercises.all()
        for exercise in exercises:
            diariesList += "  - "+exercise.Name+": "
            marks = Mark.objects.filter(ForUser = user, ForExercise = exercise)
            if len(marks) == 0:
                diariesList += "?"
            elif len(marks) == 1:
                diariesList += str(marks[0].Score)+" ("+str(marks[0].DateTime)[:16]+")"
            else:
                diariesList += "too many marks!"
            diariesList += "\r\n"
    return diariesList


def UsersPage(request):
    if request.method == 'POST':
        form = LoginForm(request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            user = auth.authenticate(username=cd['UserName'], password=cd['Password'])
            auth.login(request, user)
            return HttpResponseRedirect("/accounts/") # reload page
    else:
        form = LoginForm()
        
    user = request.user
    is_logged = user.is_authenticated()
    diariesList = is_logged and MakeDiary(user) or ""
    
    users = User.objects.all()
    courses = Course.objects.all()
    exercises = Exercise.objects.all()
    marks = Mark.objects.all()
    
    return render(request, 'account_main.html', locals())

def Register(request):
    if request.method == 'POST':
        form = RegisterForm(request.POST)
        if form.is_valid(): # All validation rules pass
            user = User.objects.create_user(username=form.cleaned_data['UserName'],
                password=form.cleaned_data['Password1'])
            user.save()
            return HttpResponseRedirect('/accounts/registration_completed/') # Redirect after POST
    else:        
        form = RegisterForm()
        
    return render(request, 'registration/register.html', {'form': form})

def RegistrationDone(request):
    return render_to_response('registration/completed.html')

def LoginFailed(request):
    return render_to_response('account_invalid.html')

def Logout(request):
    auth.logout(request)
    # Redirect to a success page.
    return HttpResponseRedirect("/accounts/")
