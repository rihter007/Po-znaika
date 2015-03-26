from django.http import HttpResponseRedirect
from django.http import HttpResponse

from forms import AddNameForm
from forms import DeleteNameForm
from models import Class
from models import StudyHead
from models import User
from models import Teacher
from views import IsHead


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
        print Names
        user = request.user
        if user.is_authenticated() and IsHead(user):
            thUser = User.objects.get(username=Names)
            Teacher.objects.filter(ForHead=user, User=thUser).delete()
            thUser.delete()
    return HttpResponseRedirect("/accounts/")
    