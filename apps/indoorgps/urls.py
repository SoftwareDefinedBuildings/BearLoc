from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
	url(r'^indoor_loc/', include('indoor_loc.urls')),
	url(r'^admin/', include(admin.site.urls)),
)