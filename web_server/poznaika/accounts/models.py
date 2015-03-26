from django.db import models
from django.contrib.auth.models import User

class Exercise(models.Model):
    Name = models.CharField(max_length=100, primary_key=True)
    Description = models.CharField(max_length=255, blank=True)
    
class Course(models.Model):
    Name = models.CharField(max_length=100, primary_key=True)
    Description = models.CharField(max_length=255, blank=True)
    
    Courses = models.ManyToManyField('self', symmetrical=False, blank=True)
    Exercises = models.ManyToManyField(Exercise, symmetrical=False)

class Mark(models.Model):
    ForUser = models.ForeignKey(User)
    ForExercise = models.ForeignKey(Exercise)
    Score = models.IntegerField()
    DateTime = models.DateTimeField()

class License(models.Model):
    ForUser = models.ForeignKey(User)
    StartDate = models.DateField()
    EndDate = models.DateField()

    
class StudyHead(models.Model):
    ForUser = models.OneToOneField(User, primary_key=True)
    Description = models.CharField(max_length=255, blank=True)

class Teacher(models.Model):
    User = models.OneToOneField(User)
    ForHead = models.ForeignKey(StudyHead)
    Description = models.CharField(max_length=255, blank=True)

class Class(models.Model):
    ForHead = models.ForeignKey(StudyHead)
    Name = models.CharField(max_length=100)

class Pupil(models.Model):
    User = models.OneToOneField(User)
    ForClass = models.ForeignKey(Class)
    