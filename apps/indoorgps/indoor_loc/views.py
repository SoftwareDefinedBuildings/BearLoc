from django.shortcuts import render, get_object_or_404, redirect, render_to_response
# from forms import MediaForm, MediaFormCreate
from indoor_loc.models import *
from django.http import HttpResponseRedirect, HttpResponse
from django.contrib.auth import logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.template import RequestContext
from django.contrib.auth import authenticate, login
import datetime
import random
import requests

TIME_INTERVAL_IN_MINUTES = 30
DAY_LENGTH_IN_SECONDS = 86400
ROOM_LIST = ['465', '490', '352', 'Wozniak Lounge', '630', '460']
ROOM_LIST_LENGTH = 6
def index(request):
    return HttpResponse("Hello, this is the Indoor Localization home page.")

def register(request, device_id, username):
    # if request_type == "register":
        #Create new user
        user = User.objects.get(username=username)
        user.get_profile().device_id = device_id
        user.get_profile().save()
        user.save()
        return HttpResponse("You've registered!")
    # else:
    #     pass

def dump_sensor_data(request, username):
    # if dummy_data:
        #Replace
    user = User.objects.get(username=username)
    device_id = user.get_profile().device_id
    s, created = SensorData.objects.create(accelerometer = random.uniform(1,10), 
        compass = random.uniform(1,10), device_id = device_id, gyroscope = random.uniform(1,10), 
        time = datetime.datetime.now()
    ) 
    s.save()

def display_data(request, username, year, month, day):
    year = int(year)
    month = int(month)
    day = int(day)
    date = datetime.datetime(year, month, day).strftime('%B %d, %Y')
    user = User.objects.get(username=username)
    device_id = user.get_profile().device_id
    epoch = (datetime.datetime(year,month,day) - datetime.datetime(1970,1,1)).total_seconds()
    epoch = int(epoch)
    #convert year, month, day into an epoch time
    start = 0
    interval_in_seconds = 60 * TIME_INTERVAL_IN_MINUTES
    location_array = []
    while (start < DAY_LENGTH_IN_SECONDS):
        #Find location each five minutes, build an array of these locations
        bearLocResponse = requests.get('http://bearloc.cal-sdb.org:20080/api/location/' + str(device_id) + '/' + str(epoch))
        locale = bearLocResponse.json()['building']
        location_array.append(locale)
        # location_array.append(ROOM_LIST[random.randint(0,6)])
        start += interval_in_seconds
    loc_start_time = 0
    loc_end_time = interval_in_seconds
    temp = location_array[0]
    #Parse into time chunks
    location_array = location_array_parse(location_array)
    context = {'year': year, 'month': month, 'day': day, 'date': date,
                'locations': location_array}

def display_dummy_data(request, username, year, month, day):
    year = int(year)
    month = int(month)
    day = int(day)
    date = datetime.datetime(year, month, day).strftime('%B %d, %Y')
    epoch = (datetime.datetime(year,month,day,0,0) - datetime.datetime(1970,1,1)).total_seconds()
    epoch = int(epoch)
    #convert year, month, day into an epoch time
    start = 0
    interval_in_seconds = 60 * TIME_INTERVAL_IN_MINUTES
    location_array = []
    while (start < DAY_LENGTH_IN_SECONDS):
        location_array.append(ROOM_LIST[random.randint(0, ROOM_LIST_LENGTH - 1)])
        start += interval_in_seconds
    loc_start_time = 0
    loc_end_time = interval_in_seconds
    #Parse into time chunks
    location_array = location_array_parse(location_array)
    location_array = location_array_format(location_array)
    context = {'username':username, 'year': year, 'month': month, 'day': day, 'date': date, 
                'locations': location_array}
    return render(request, 'indoor_loc/day.html', context)

def location_array_parse(location_array):
    interval_in_seconds = 60 * TIME_INTERVAL_IN_MINUTES
    temp = location_array[0]
    location_array2 = []
    loc_start_time = 0
    loc_end_time = interval_in_seconds
    for i in range(1, len(location_array)):
        if temp == location_array[i]:
            loc_end_time += interval_in_seconds
        else: 
            location_array2.append([temp, [loc_start_time, loc_end_time]])
            loc_start_time = loc_end_time
            loc_end_time += interval_in_seconds
            temp = location_array[i]
        #HERE LET'S GET DAT DATA DOE, MOVING BETWEEN ROOMS
            #Maybe???
            #device_data = get_change_data()
            #s = SensorData.get_or_create(compass = device_data[2]['compass'], accelerometer)
    location_array2.append([temp, [loc_start_time, loc_end_time]])
    return location_array2


def location_array_format(parsed_location_array):
    for location in parsed_location_array:
         location[1][0] = datetime.datetime.fromtimestamp(location[1][0]).strftime('%H:%M:%S')
         location[1][0] = datetime.datetime.strptime(location[1][0], "%H:%M:%S").strftime('%I:%M %p')
         location[1][1] = datetime.datetime.fromtimestamp(location[1][1]).strftime('%H:%M:%S')
         location[1][1] = datetime.datetime.strptime(location[1][1], "%H:%M:%S").strftime('%I:%M %p')
    return parsed_location_array



def get_change_data():
    #return device data in time slot of location change
    #return in the format of (room1, room2, [data])
    return 0

@login_required
def activity(request, activity_id):
    # unmasked_id = int(activity_id) ^ 0xABCDEFAB
    # activity = Activity.objects.get(id=unmasked_id)
    activity = Activity.objects.get(id=activity_id)
    activityType = activity.activityType
    context = {'activity': activity, 'activity_type': activityType}
    return render(request, 'indoor_loc/activity.html', context)
    # return HttpResponse("You're looking at activity {0}.".format(activity_id))

@login_required
def user_page(request, user_id):
    return HttpResponse("Hello, it's good to see you user {0}!".format(user_id))

@login_required
def day(request, user_id, day_id):
    day = Day.objects.filter(user=user_id).filter(id=day_id)
    day = day.get()
    context = {'day': day}
    return render(request, 'indoor_loc/day.html', context)
    # return HttpResponse("Hello, it's good to see you user {0}! Here is your activity for day {1}.".format(user_id, day_id))

def login_page(request):
    state = username = password = ''
    if request.POST:
        username = request.POST.get('username')
        password = request.POST.get('password')
        user = authenticate(username=username, password=password)
        if user is not None:
            if user.is_active:
                login(request, user)
                return HttpResponseRedirect('indoor_loc/')
            else:
                state = "Your account is not active, please contact the site admin."
        else:
            state = "Your username and/or password were incorrect."

    return render(request, 'indoor_loc/login.html', {'state': state, 'username': username})
