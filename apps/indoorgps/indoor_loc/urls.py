from django.conf.urls import patterns, url

from indoor_loc import views

urlpatterns = patterns('', 
	#Home Page
	url(r'^$', views.index, name='index'),
	#Data Dump
	# url(r'user/(?P<username>\w+)/$', views.dump_sensor_data, name='user_page'),
	#Registration
	url(r'register/(?P<username>\w+)/device/(?P<device_id>\d+)/$', views.register, name='register'),
	#Activity Details
	url(r'^(?P<activity_id>\d+)/$', views.activity, name='activity'),
	#User Home Page
	# url(r'^user/(?P<user_id>\d+)/$', views.user_page, name='user_page'),
	#Day Page
	# url(r'user/(?P<username>\w+)/(?P<year>\d{4})/(?P<month>\d{2})/(?P<day>\d{2})/$', views.display_data, name='display_data'),
	#Randomized
	url(r'dummy/user/(?P<username>\w+)/(?P<year>\d{4})/(?P<month>\d{2})/(?P<day>\d{2})/$', views.display_dummy_data, name='display_data'),
	#Login
	url(r'^login/', views.login_page, name='login'),
)