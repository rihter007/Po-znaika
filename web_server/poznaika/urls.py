from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

from django.contrib import auth
from django.contrib.auth.views import login, logout
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

urlpatterns = patterns('',
    # Examples:
    # url(r'^blog/', include('blog.urls')),
    url(r'^admin/', include(admin.site.urls)),
    
    url(r'^$', 'poznaika.start.views.StartPage'),
    url(r'^products/$', 'poznaika.products.views.ProductsPage'),
    url(r'^contacts/$', 'poznaika.company.views.ContactsPage'),
    url(r'^about/$', 'poznaika.company.views.AboutPage'),
    
    (r'^accounts/', include('poznaika.accounts.urls')),
)

#if settings.DEBUG:
urlpatterns += staticfiles_urlpatterns()
