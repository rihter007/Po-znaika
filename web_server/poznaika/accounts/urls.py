from django.conf.urls import patterns
#from views import Register

urlpatterns = patterns('',
#    (r'^login/$', login),
    (r'^registration/$', 'poznaika.accounts.views.Register'),
    (r'^registration_completed/$', 'poznaika.accounts.views.RegistrationDone'),
    (r'^logout/$', 'poznaika.accounts.views.Logout'),
    (r'^invalid/$', 'poznaika.accounts.views.LoginFailed'),
    (r'^mark/$', 'poznaika.accounts.handlers.MarkRequest'),
    (r'^diary/$', 'poznaika.accounts.handlers.DiaryRequest'),
    (r'^license/$', 'poznaika.accounts.handlers.LicenseRequest'),
    (r'^add_class/$', 'poznaika.accounts.changers.AddClass'),
    (r'^delete_class/$', 'poznaika.accounts.changers.DeleteClass'),
    (r'^add_teacher/$', 'poznaika.accounts.changers.AddTeacher'),
    (r'^delete_teacher/$', 'poznaika.accounts.changers.DeleteTeacher'),
    (r'^add_pupil/$', 'poznaika.accounts.changers.AddPupil'),
    (r'^delete_pupil/$', 'poznaika.accounts.changers.DeletePupil'),
    (r'^$', 'poznaika.accounts.views.UsersPage'),
)