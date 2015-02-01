from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

from django.contrib import auth
from django.contrib.auth.views import login, logout

urlpatterns = patterns('',
    # Examples:
    # url(r'^blog/', include('blog.urls')),
    url(r'^admin/', include(admin.site.urls)),
    
    url(r'^$', 'poznaika.start.views.StartPage'),
    url(r'^products/$', 'poznaika.products.views.ProductsPage'),
    url(r'^company/$', 'poznaika.company.views.CompanyPage'),
    
    (r'^accounts/', include('poznaika.accounts.urls')),
)
