from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
# Create your models here.

class ActivityType(models.Model):
	typeName = models.CharField(max_length=255)
	caloriesPerMinute = models.FloatField()

	def __unicode__(self):
		return self.typeName

class Activity(models.Model):
	name=models.CharField(max_length=255)
	duration=models.FloatField()	
	activityType = models.ForeignKey(ActivityType)
	locationName = models.CharField(max_length=255, default='')
	locationLatitude = models.FloatField()
	locationLongitude = models.FloatField()
	date = models.DateField()
	time = models.FloatField()

	def locationNamed(self):
		return self.locationName != ''
	
	def __unicode__(self):
		return self.name

class Day(models.Model):
	# activities = models.ManyToOneField(Activity)
	date = models.DateField()
	user = models.ForeignKey(User)

	def __unicode__(self):
		return self.date

class SensorData(models.Model):
	device_id = models.IntegerField()
	compass = models.FloatField()
	accelerometer = models.FloatField()
	gyroscope = models.FloatField()
	time = models.DateTimeField(unique=True)



class UserProfile(models.Model):
    user = models.ForeignKey(User, unique=True)
    device_id = models.IntegerField(unique=True)

    def user_post_save(sender, instance, created, **kwargs):
        """Create a user profile when a new user account is created"""
        if created == True:
            up = UserProfile()
            up.user = instance
            up.save()
    post_save.connect(user_post_save, sender=User)

    def __unicode__(self):
        return self.user.username

    class Admin:
        pass
