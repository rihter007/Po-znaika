from django.http import HttpResponseRedirect
from django.http import HttpResponse

from forms import AddNameForm
from forms import DeleteNameForm
from forms import AddPupilForm
from forms import DeletePupilForm
from models import Class
from models import StudyHead
from models import User
from models import Teacher
from models import Pupil
from views import IsHead
from views import IsTeacher


def AddClass(request):
    if request.method == 'POST':
        form = AddNameForm(request.POST)
        if form.is_valid():
            user = request.user
            if user.is_authenticated() and IsHead(user):
                head = StudyHead.objects.get(ForUser=user)
                cls = Class(ForHead=head, Name=form.cleaned_data['Name'])
                cls.save()

    return HttpResponseRedirect("/accounts/")

def DeleteClass(request):
    if request.method == 'POST':
        Names = request.POST['Names']
        #if len(Names) == 0:            
        user = request.user
        if user.is_authenticated() and IsHead(user):
            head = StudyHead.objects.get(ForUser=user)
            Class.objects.filter(ForHead=head, Name=Names).delete()
    return HttpResponseRedirect("/accounts/")

def AddTeacher(request):
    if request.method == 'POST':
        form = AddNameForm(request.POST)
        if form.is_valid():
            user = request.user
            if user.is_authenticated() and IsHead(user):
                thUser = User.objects.create_user(
                    username=form.cleaned_data['Name'],
                    password=form.cleaned_data['Name'])
                thUser.save()
                head = StudyHead.objects.get(ForUser=user)
                th = Teacher(ForHead=head, User=thUser)
                th.save()

    return HttpResponseRedirect("/accounts/")

def DeleteTeacher(request):
    if request.method == 'POST':
        Names = request.POST['Names']
        user = request.user
        if user.is_authenticated() and IsHead(user):
            thUser = User.objects.get(username=Names)
            Teacher.objects.filter(ForHead=user, User=thUser).delete()
            thUser.delete()
    return HttpResponseRedirect("/accounts/")

def AddPupil(request):
    if request.method == 'POST':
        form = AddPupilForm(request.POST)
        if form.is_valid():
            user = request.user
            if user.is_authenticated() and IsTeacher(user):
                # Gather data
                teacher = Teacher.objects.get(User=user)
                head = teacher.ForHead
                className = form.cleaned_data['Class']
                cls = Class.objects.get(ForHead=head, Name=className)
                # Make objects
                ppUser = User.objects.create_user(
                    username=form.cleaned_data['Name'],
                    password=form.cleaned_data['Name'])
                pp = Pupil(ForClass=cls, User=ppUser)
                ppUser.save()                
                pp.save()

    return HttpResponseRedirect("/accounts/")

def DeletePupil(request):
    if request.method == 'POST':
        form = DeletePupilForm(request.POST)
        if form.is_valid():
            user = request.user
            if user.is_authenticated() and IsTeacher(user):
                pupils = form.cleaned_data['Pupils']
                for pupil in pupils:
                    pupil.User.delete()
                    pupil.delete()
    return HttpResponseRedirect("/accounts/")
    