Server Database Structure
=========================

Server is using sqlite3 database. The database name is **bearloc.db**, and *locates at /bearloc/server/bearloc.db*. There are several tables in the database. Adn the details of the database are discussed below.

device
------

Table **device** stores information of devices that have reported to the database. Its structure is:

**| uuid TEXT NOT NULL | make TEXT | model TEXT |**

with **PRIMARY KEY uuid**.

**uuid** is an universal unique device-dependant ID string. **make** is the manufacturer of the device, and **model** is the model of the device.


sensormeta
----------

Table **sensormeta** stores metadata of sensors on each devices. Its structure is:

**| uuid TEXT NOT NULL | sensor TEXT NOT NULL | vendor TEXT | name TEXT | power REAL | minDelay INTEGER | maxRange REAL | version INTEGER | resolution REAL |**

with **PRIMARY KEY (uuid, sensor)**.

**sensor** is the type of the sensor, such as acc, light, etc. **vendor** is the manufacturer of the sensor. **name** is the name/model of the sensor. **power** is the power in mA used by this sensor while in use. **minDelay** is the minimum delay allowed between two events in microsecond or zero if this sensor only returns a value when the data it's measuring changes. **maxRange** is the maximum range of the sensor in the sensor's unit. **version** is the version of the sensor's module. **resolution** is the resolution of the sensor in the sensor's unit. More information is available `here <http://developer.android.com/reference/android/hardware/Sensor.html>`__.


semloc
------

Table **semloc** stores semantic locations reported by users. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | country TEXT | state TEXT | city TEXT | street TEXT | building TEXT | floor TEXT | room TEXT |**

with **PRIMARY KEY (uuid, epoch)**.

**epoch** is Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. **country**, **state**, **city**, **street**, **building**, **floor**, **room** are all pre-defined semantics to describe the semantic of locations. All semantic locations have to conform to this semantic schema.

wifi
----

Table **wifi** stores Wi-Fi received signal strengths sampled by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | BSSID TEXT NOT NULL | SSID TEST | RSSI REAL NOT NULL | capability TEXT | freq REAL |**

with **PRIMARY KEY (uuid, epoch, BSSID)**.

**BSSID** is the media access control address (MAC address) of the access point. It is globally unique. **SSID** is the Wi-Fi network name. **RSSI** is received signal strength indicator, which is the received/detected signal level in dBm. **capability** describes the authentication, key management, and encryption schemes supported by the access point. **freq** is the frequency in MHz of the channel over which the client is communicating with the access point. More information is available `here <http://developer.android.com/reference/android/net/wifi/ScanResult.html>`__.


audio
-----

Table **audio** stores audio data recorded by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | source TEXT | channel INTEGER NOT NULL | sampwidth INTEGER NOT NULL | framerate INTEGER NOT NULL | nframes INTEGER NOT NULL | raw BLOB NOT NULL |**

with **PRIMARY KEY (uuid, epoch)**.

**source** is the source of audio, such as *MIC* and *CAMCORDER* [`ref <http://developer.android.com/reference/android/media/MediaRecorder.AudioSource.html>`__]. **channel** describes the number of separated streams of audio data. **sampwidth** is the number of bytes per audio sample (a.k.a. frame). **framerate** is the sample rate expressed in Hertz, which indicates the number of sample/frame recorded every second. **nframes** is the number of frames the audio data has.


To convert the audio data to wav file, here is an example python codes.

.. code-block:: python

   import wave
   import sqlite3

   dbpath = 'bearloc.db'
   wavfpath = 'audio.wav'

   conn = sqlite3.connect(database = dbpath)
   cur = conn.cursor()

   conn.text_factory = str 

   # extract attributes and store in db
   operation = "SELECT channel, sampwidth, framerate, nframes, raw FROM audio;"
   cur.execute(operation)
   audio = cur.fetchall()
   (channel, sampwidth, framerate, nframes, raw) = audio[0]

   wavf = wave.open(wavfpath, 'wb')
   wavf.setnchannels(channel)
   wavf.setsampwidth(sampwidth)
   wavf.setframerate(framerate)
   wavf.setnframes(nframes)
   wavf.writeframesraw(raw)
   wavf.close()


geoloc
------

Table **geoloc** stores geographical location points collected by the device GPS or network service. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | longitude REAL NOT NULL | latitude REAL NOT NULL | altitude REAL | bearing REAL | speed REAL | accuracy REAL | provider TEXT |**

with **PRIMARY KEY (uuid, epoch)**.

**longitude** and **latitude** are longitude and latitude data in degrees. **altitude** is in meters above sea level. **bearing** is bearing in degrees. Bearing is the horizontal direction of travel of this device, and is not related to the device orientation. It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing. **speed** in meters/second over ground. If speed is 0.0 then it means this location does not have a speed data. 

**accuracy** is the estimated accuracy of this location, in meters. We define accuracy as the radius of 68% confidence. In other words, if you draw a circle centered at this location's latitude and longitude, and with a radius equal to the accuracy, then there is a 68% probability that the true location is inside the circle. In statistical terms, it is assumed that location errors are random with a normal distribution, so the 68% confidence circle represents one standard deviation. Note that in practice, location errors do not always follow such a simple distribution. This accuracy estimation is only concerned with horizontal accuracy, and does not indicate the accuracy of bearing, velocity or altitude if those are included in this Location. If this location does not have an accuracy, then 0.0 is returned. 

**provider** is the name of the provider that generated this location.

More information is available `here <http://developer.android.com/reference/android/location/Location.html>`__.


acc
---

Table **acc** stores accelerometer data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**sysnano** is the timestamp of the most precise timer available on the local system, in nanoseconds. Equivalent to Linux's CLOCK_MONOTONIC. This timestamp should only be used to measure a duration by comparing it against another timestamp on the same device. Values returned by this method do not have a defined correspondence to wall clock times; the zero value is typically whenever the device last booted [`ref <http://developer.android.com/reference/java/lang/System.html#nanoTime()>`__]. **eventnano** is the device uptime in nanosecond at which the sensor data sampling (event) happened [`ref <https://code.google.com/p/android/issues/detail?id=7981>`__]. **x**, **y**, and **z** are acceleration minus gravity on the each axis, in SI units (m/s^2) [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]. **accuracy** is the accuracy of the sensor data [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#accuracy>`__].


lacc
----

Table **lacc** stores linear accelerometer data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

A three dimensional vector indicating acceleration along each device axis, not including gravity. All values have units of m/s^2. The coordinate system is the same as is used by the acceleration sensor. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


gravity
-------

Table **gravity** stores gravity data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

A three dimensional vector indicating the direction and magnitude of gravity. Units are m/s^2. The coordinate system is the same as is used by the acceleration sensor. When the device is at rest, the output of the gravity sensor should be identical to that of the accelerometer. The output of the accelerometer, gravity and linear-acceleration sensors must obey the following relation [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]:

**acceleration = gravity + linear-acceleration**


gyro
----

Table **gyro** stores gyroscope data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

All values are in radians/second and measure the rate of rotation around the device's local X, Y and Z axis. The coordinate system is the same as is used for the acceleration sensor. Rotation is positive in the counter-clockwise direction. That is, an observer looking from some positive location on the x, y or z axis at a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the standard mathematical definition of positive rotation. In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


rotation
--------

Table **rotation** stores rotation data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | xr REAL NOT NULL | yr REAL NOT NULL | zr REAL NOT NULL | cos REAL | head_accuracy REAL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

The rotation vector represents the orientation of the device as a combination of an angle and an axis, in which the device has rotated through an angle θ around an axis (x, y, z). The three elements of the rotation vector are (x\*sin(θ/2), y\*sin(θ/2), z\*sin(θ/2)), such that the magnitude of the rotation vector is equal to sin(θ/2), and the direction of the rotation vector is equal to the direction of the axis of rotation. The three elements of the rotation vector are equal to the last three components of a unit quaternion (cos(θ/2), x\*sin(θ/2), y\*sin(θ/2), z\*sin(θ/2)).
Elements of the rotation vector are unitless. The x,y, and z axis are defined in the same way as the acceleration sensor.

The reference coordinate system is defined as a direct orthonormal basis, where:
X is defined as the vector product Y.Z (It is tangential to the ground at the device's current location and roughly points East). Y is tangential to the ground at the device's current location and points towards magnetic north. Z points towards the sky and is perpendicular to the ground. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]

**accuracy** is estimated heading accuracy in radians. If the value is -1, it means estimated heading accuracy is not available.


magnetic
--------

Table **magnetic** stores magnetic data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

All values are in micro-Tesla (uT) and measure the ambient magnetic field in the X, Y and Z axis. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


light
-----

Table **light** stores light sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | light REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**light** is ambient light level in SI lux units. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


temp
----

Table **temp** stores ambient temperature data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | temp REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**temp** is ambient (room) temperature in degree Celsius. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


pressure
--------

Table **pressure** stores atmospheric pressure data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | pressure REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**pressure** is atmospheric pressure in hPa (millibar). [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


proximity
---------

Table **proximity** stores proximity sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | proximity REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**proximity** is proximity sensor distance measured in centimeters. Some proximity sensors only support a binary near or far measurement. In this case, the sensor should report its *maximum range value* in the far state and a lesser value in the near state. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]


humidity
--------

Table **humidity** stores humidity sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | humidity REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**humidity** is relative ambient air humidity in percent. When relative ambient air humidity and ambient temperature are measured, the dew point and absolute humidity can be calculated. [`ref <http://developer.android.com/reference/android/hardware/SensorEvent.html#values>`__]
