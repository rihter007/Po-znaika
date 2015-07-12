# coding=cp1251

from django import forms
from django.contrib import auth
from django.contrib.auth.models import User

from models import Pupil
from models import Class


def ToUtf(str):
    return str.decode('cp1251').encode('utf8')
    

class RegisterForm(forms.Form):
    UserName = forms.CharField(max_length=30, label=ToUtf("���� ���"))
    Password1 = forms.CharField(max_length=30, label=ToUtf("������"))
    Password2 = forms.CharField(max_length=30, label=ToUtf("������ ��� ���"))
    
    def clean_UserName(self):
        name = self.cleaned_data['UserName']
        users = User.objects.filter(username=name)
        if len(users) > 0:
            raise forms.ValidationError(u"��� ��� ����������!")
        return name
        
    def clean_Password2(self):
        pwd1 = self.cleaned_data['Password1']
        pwd2 = self.cleaned_data['Password2']
        if pwd1 != pwd2:
            raise forms.ValidationError(u"��������� ������ �����������!")
        return pwd1

class LoginForm(forms.Form):
    UserName = forms.CharField(max_length=30, label=ToUtf("���� ���"))
    Password = forms.CharField(max_length=30, label=ToUtf("������"))
    
    def clean_Password(self):
        cd = self.cleaned_data
        pwd = password=cd['Password']
        user = auth.authenticate(username=cd['UserName'], password=pwd)
        if user is None or not user.is_active:
            raise forms.ValidationError(u"��������� �����+������ �����������!")
        return pwd

        
class AddNameForm(forms.Form):
    Name = forms.CharField(max_length=30)

class DeleteNameForm(forms.Form):
    Names = forms.MultipleChoiceField()
    
    def __init__(self, choices, labelText, *args):
        super(DeleteNameForm, self).__init__(*args)
        self.fields['Names'] = forms.MultipleChoiceField(choices=choices)
        self.fields['Names'].label = labelText

class AddPupilForm(AddNameForm):
    Class = forms.ModelChoiceField(Class.objects.all(), empty_label=None,
        to_field_name="Name")

class DeletePupilForm(forms.Form):
    Pupils = forms.ModelMultipleChoiceField(Pupil.objects.all(), 
        label=ToUtf("������ ��������"))
        