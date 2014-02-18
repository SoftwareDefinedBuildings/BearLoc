Data Schema
===========

The data exchanged between client and server are always JSON array of JSON object of events. JSON object of event is only required to have **"type"** and **"id"** keys. **"type"** is a string that describes the type of the event. **"id"** is a string that describes the ID of creator (e.g. smart phone, server) of the event. Here we describe several event JSON object schema we are using for localization service. In each event, we specify the value of "type", and the details of other fields.


Device Info
-----------

Device Info is information of devices reporting to the server.


**Type**

"device info"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
make            string       The name of the manufacturer of the device. (Example Value: "LGE")
model           string       The model of the device. (Example Value: "VS910 4G")
=============== ============ ====================================================================================================


Sensor Info
-----------

Sensor Info is metadata of sensors on a devices.


**Type**

"sensor info"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
sensor          string       The type of the sensor. (Example Value: "accelerometer")
vendor          string       The manufacturer of the sensor. (Example Value: "st micro")
model           string       The model of the sensor. (Example Value: "kr3dh")
version         string       The the version of the sensor's module. (Example Value: "1.1b2")
unit            string       The unit of the sensor. (Example Value: "m/s\ :sup:`2`")
power           number       The power in mA used by this sensor while in use. (Example Value: 20)
min delay       number       The minimum delay allowed between two events in microsecond or zero if this sensor only returns a value when the data it's measuring changes. (Example Value: 0)
max range       number       The maximum range of the sensor in the sensor's unit. (Example Value: 100)
resolution      number       The resolution of the sensor in the sensor's unit. (Example Value: 1)
=============== ============ ====================================================================================================


.. _estimated-semantic-location:

Estimated Semantic Location
---------------------------

Estimated Semantic Location is semantic location estimated by BearLoc server.


**Type**

"estimated semloc"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
target id       string       The ID of the device which the query of estimated location targets. Note that this is different of the **"id"** of this event. **"id"** of this event is the generator of this event and usually is a server. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
country         string       The name of the country of the location. (Example Value: "US")
state           string       The name of the state of the location. (Example Value: "CA")
city            string       The name of the city of the location. (Example Value: "Berkeley")
street          string       The name of the street of the location. (Example Value: "Leroy Ave")
building        string       The name of the building of the location. (Example Value: "Soda Hall")
locale          string       The name of the location. (Example Value: "RADLab Kitchen")
=============== ============ ====================================================================================================


Reported Semantic Location
--------------------------

Reported Semantic Location is semantic location manually reported by user.


**Type**

"reported semloc"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
country         string       The name of the country of the location. (Example Value: "US")
state           string       The name of the state of the location. (Example Value: "CA")
city            string       The name of the city of the location. (Example Value: "Berkeley")
street          string       The name of the street of the location. (Example Value: "Leroy Ave")
building        string       The name of the building of the location. (Example Value: "Soda Hall")
locale          string       The name of the location. (Example Value: "RADLab Kitchen")
=============== ============ ====================================================================================================


Wi-Fi Scan
----------

Wi-Fi Scan is the information of a Basic Service Set Identifier (BSSID) after a Wi-Fi scan.


**Type**

"wifi"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
BSSID           string       The media access control (MAC) address of the access point. (Example Value: "00:1a:df:a7:33:12")
SSID            string       The Wi-Fi network name. (Example Value: "EECS-Open")
RSSI            number       Received Signal Strength Indicator in dBm. (Example Value: -67)
capability      string       The authentication, key management, and encryption schemes supported by the access point. (Example Value: "[WPA2-EAP-CCMP]")
frequency       number       The frequency in MHz of the channel over which the client is communicating with the access point. (Example Value: 2462)
=============== ============ ====================================================================================================


Audio
-----

TODO: add audio data description


Geographic Coordinate 
---------------------

Geographic Coordinate is the estimated geographic coordinate and its information returned by other positioning systems (e.g. Global Positioning System).


**Type**

"geocoord"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
longitude       number       Longitude in degrees. (Example Value: -122.258582475)
latitude        number       Latitude data in degrees. (Example Value: 37.8754162875)
altitude        number       Altitude in meters above sea level (Example Value: 100.0)
bearing         number       Bearing in degrees. Bearing is the horizontal direction of travel of this device, and is not related to the device orientation. It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing. (Example Value: 0.0)
speed           number       Speed in meters/second over ground. (Example Value: 4.0)
accuracy        number       The estimated accuracy of this location, in meters. We define accuracy as the radius of 68% confidence. In other words, if you draw a circle centered at this location's latitude and longitude, and with a radius equal to the accuracy, then there is a 68% probability that the true location is inside the circle. In statistical terms, it is assumed that location errors are random with a normal distribution, so the 68% confidence circle represents one standard deviation. Note that in practice, location errors do not always follow such a simple distribution. This accuracy estimation is only concerned with horizontal accuracy, and does not indicate the accuracy of bearing, velocity or altitude if those are included in this Location. (Example Value: 45.0)
provider        string       The name of the provider that generated this location. (Example Value: "gps")
=============== ============ ====================================================================================================


Accelerometer
-------------

Accelerometer is data collected by accelerometer sensor. 


**Type**

"accelerometer"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
x               number       Acceleration on the x axis in m/s\ :sup:`2`. (Example Value: 0.599295318126678)
y               number       Acceleration on the y axis in m/s\ :sup:`2`. (Example Value: 1.389275431633)
z               number       Acceleration on the z axis in m/s\ :sup:`2`. (Example Value: 9.68406677246094)
accuracy        number       The accuracy of the sensor data (Example Value: 3.0)
=============== ============ ====================================================================================================

TODO: define the device coordinate system.


Gyroscope
---------

Gyroscope is data collected by gyroscope sensor. Rotation is positive in the counter-clockwise direction. That is, an observer looking from some positive location on the x, y or z axis at a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the standard mathematical definition of positive rotation.


**Type**

"gyroscope"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
x               number       Angular speed around the x axis in radian per second. (Example Value: 1.0)
y               number       Angular speed around the y axis in radian per second. (Example Value: -2.3)
z               number       Angular speed around the z axis in radian per second. (Example Value: 1.6)
accuracy        number       The accuracy of the sensor data (Example Value: 3.0)
=============== ============ ====================================================================================================

TODO: define the device coordinate system.


Magnetic Field
--------------

Magnetic Field is data collected by compass/magnetic sensor.


**Type**

"magnetic"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
x               number       Ambient magnetic field on the x axis in micro-Tesla (uT). (Example Value: -12.875)
y               number       Ambient magnetic field on the y axis in micro-Tesla (uT). (Example Value: 13.4375)
z               number       Ambient magnetic field on the z axis in micro-Tesla (uT). (Example Value: -34.75)
accuracy        number       The accuracy of the sensor data (Example Value: 3.0)
=============== ============ ====================================================================================================


Light
-----

Light is data collected by light sensor.


**Type**

"light"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
light           number       Ambient light level in SI lux. (Example Value: 124.)
accuracy        number       The accuracy of the sensor data (Example Value: 0.0)
=============== ============ ====================================================================================================


Ambient Temperature
-------------------

Ambient Temperature is ambient temperature data collected near/on the device. 


**Type**

"temperature"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
temperature     number       Ambient temperature in degree Celsius. (Example Value: 27.9627552032471)
accuracy        number       The accuracy of the sensor data (Example Value: 0.0)
=============== ============ ====================================================================================================


Atmospheric Pressure
--------------------

Atmospheric Pressure is atmospheric pressure data collected near/on the device. 


**Type**

"pressure"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
pressure        number       Atmospheric pressure in hPa (millibar). (Example Value: 1009.80999755859)
accuracy        number       The accuracy of the sensor data (Example Value: 3.0)
=============== ============ ====================================================================================================


Proximity
---------

Proximity is distance measured in centimeters by proximity sensor.


**Type**

"proximity"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
proximity       number       Distance measured in centimeters. (Example Value: 5.00030517578125)
accuracy        number       The accuracy of the sensor data (Example Value: 3.0)
=============== ============ ====================================================================================================


Humidity
--------

Humidity is relative ambient air humidity in percent by humidity sensor.


**Type**

"humidity"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
epoch           number       The Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. (Example Value: 1387670483532)
proximity       number       Relative ambient air humidity in percent. (Example Value: -194.317001342773)
accuracy        number       The accuracy of the sensor data (Example Value: 0.0)
=============== ============ ====================================================================================================


.. _data-reception:

Data Reception
--------------

The event that data are received by server.


**Type**

"data reception"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
posted          number       The number of event received by server. (Example Value: 5)
accepted        number       The number of event accepted by server. This value will not be larger than **"posted"**. (Example Value: 4)
=============== ============ ====================================================================================================


.. _location-candidates:

Location Candidates
-------------------

The event that location candidates of a given location is generated.


**Type**

"location candidates"


**Keys**

=============== ============ ====================================================================================================
Key             Value        Description
=============== ============ ====================================================================================================
country         string       The name of the country of the query location, if exists. (Example Value: "US")
state           string       The name of the state of the query location, if exists. (Example Value: "CA")
city            string       The name of the city of the query location, if exists. (Example Value: "Berkeley")
street          string       The name of the street of the query location, if exists. (Example Value: "Leroy Ave")
building        string       The name of the building of the query location, if exists. (Example Value: "Soda Hall")
target semantic string       The name of the semantic of location to query. (Example Value: "building")
candidates      array        The array of candidates, which is a JSON array of strings. (Example Value: ["410", "494", "RADLab Kitchen", "417", "415", "Wozniak Lounge"])
=============== ============ ====================================================================================================
