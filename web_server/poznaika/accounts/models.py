from django.db import models
from django.contrib.auth.models import User

class Exercise(models.Model):
    Name = models.CharField(max_length=100, primary_key=True)
    Description = models.CharField(max_length=255, blank=True)
    def __str__(self):
        text = self.Description if self.Description else self.Name
        return text.encode('utf8')
    
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
    Count = models.IntegerField()

    
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
    
    def __str__(self):
        return self.Name

class Pupil(models.Model):
    User = models.OneToOneField(User)
    ForClass = models.ForeignKey(Class)
    
    def __str__(self):
        return (self.User.username + " (" + self.ForClass.Name + ")").encode('utf8')
