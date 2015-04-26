# coding=1251

from django.shortcuts import render
from django.shortcuts import render_to_response
from django.contrib import auth
from django.contrib.auth.models import User
from django.http import HttpResponseRedirect
from django.http import HttpResponse
from django.template import RequestContext
from django import forms

from forms import RegisterForm
from forms import LoginForm
from forms import AddNameForm
from forms import DeleteNameForm
from forms import AddPupilForm
from forms import DeletePupilForm
from forms import ToUtf
from models import Exercise
from models import Course
from models import Mark
from models import StudyHead
from models import Teacher
from models import Class
from models import Pupil

# Create your views here.

def MakeSchool(head):
    schoolList = ""
    h = StudyHead.objects.get()

    schoolList += "Teachers: "
    teachers = Teacher.objects.filter(ForHead = h)
    for teacher in teachers:
        schoolList += teacher.User.username + ", "
    schoolList += "\r\n"

    schoolList += "Classes and pupils: \r\n"
    classes = Class.objects.filter(ForHead = h)
    for cls in classes:
        schoolList += "* " + cls.Name + "\r\n"
        pupils = Pupil.objects.filter(ForClass = cls)
        for pupil in pupils:
            schoolList += "  - " + pupil.User.username + "\r\n"        
        
    return schoolList

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
            else:
                for mark in marks:
                    diariesList += str(mark.Score)+" ("+str(mark.DateTime)[:16]+")"
                    if len(marks) > 1 and mark != marks[len(marks)-1]:
                        diariesList += "; "
            diariesList += "\r\n"
    return diariesList

def IsHead(user):
    heads = StudyHead.objects.filter(ForUser=user)
    return len(heads) == 1
    
def IsTeacher(user):
    teachers = Teacher.objects.filter(User=user)
    return len(teachers) == 1

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
    if is_logged:
        is_head = IsHead(user)
        if is_head:
            schoolList = is_logged and MakeSchool("sh") or ""    
            heads = StudyHead.objects.all()
            teachers = Teacher.objects.all()
            classes = Class.objects.all()
            pupils = Pupil.objects.all()
            
            addTeacherForm = AddNameForm()
            addTeacherForm.fields['Name'].label = ToUtf("Имя учителя")
            choices = []
            for teacher in teachers:
                teacherInfo = teacher.User.username
                if teacher.Description != "":
                    teacherInfo += ' (' + teacher.Description + ')'
                pair = (teacher.User.username, teacherInfo)
                
                choices.append(pair)
            deleteTeacherForm = DeleteNameForm(choices, ToUtf("Список учителей"))
            
            addClassForm = AddNameForm()
            addClassForm.fields['Name'].label = ToUtf("Имя класса")
            choices = []
            for cls in classes:
                pair = (cls.Name, cls.Name)
                choices.append(pair)
            deleteClassForm = DeleteNameForm(choices, ToUtf("Список классов"))
            
        is_teacher = IsTeacher(user)
        if is_teacher:
            teachers = Teacher.objects.all()
            classes = Class.objects.all()
            pupils = Pupil.objects.all()
            
            choices = []
            addPupilForm = AddPupilForm()
            #addPupilForm.SetChoices(choices)
            addPupilForm.fields['Name'].label = ToUtf("Имя ученика")
            addPupilForm.fields['Class'].label = ToUtf("Его класс")
            choices = []
            for pupil in pupils:
                pair = (pupil.User.username, pupil.User.username)
                choices.append(pair)
            deletePupilForm = DeletePupilForm()

    else:
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
