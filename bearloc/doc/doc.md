# BearLoc Documentation


## 1. Server RESTful Interface



## 2. Server Database Structure

Server is using sqlite3 database. The database name is **bearloc.db**, and *locates at /bearloc/server/bearloc.db*. There are several tables in the database. Adn the details of the database are discussed below.

### 2.1. device

Table **device** stores information of devices that have reported to the database. Its structure is:

**| uuid TEXT NOT NULL | make TEXT | model TEXT |**

with **PRIMARY KEY uuid**.

**uuid** is an universal unique device-dependant ID string. **make** is the manufacturer of the device, and **model** is the model of the device.


### 2.2. sensormeta

Table **sensormeta** stores metadata of sensors on each devices. Its structure is:

**| uuid TEXT NOT NULL | sensor TEXT NOT NULL | vendor TEXT | name TEXT | power REAL | minDelay INTEGER | maxRange REAL | version INTEGER | resolution REAL |**

with **PRIMARY KEY (uuid, sensor)**.

**sensor** is the type of the sensor, such as acc, light, etc. **vendor** is the manufacturer of the sensor. **name** is the name/model of the sensor. **power** is the power in mA used by this sensor while in use. **minDelay** is the minimum delay allowed between two events in microsecond or zero if this sensor only returns a value when the data it's measuring changes. **maxRange** is the maximum range of the sensor in the sensor's unit. **version** is the version of the sensor's module. **resolution** is the resolution of the sensor in the sensor's unit. More information is available [here](http://developer.android.com/reference/android/hardware/Sensor.html).


### 2.3. semloc

Table **semloc** stores semantic locations reported by users. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | country TEXT | state TEXT | city TEXT | street TEXT | building TEXT | floor TEXT | room TEXT |**

with **PRIMARY KEY (uuid, epoch)**.

**epoch** is Unix time in millisecond, defined as the number of milliseconds that have elapsed since 00:00:00.000 Coordinated Universal Time (UTC), Thursday, 1 January 1970, not counting leap seconds. **country**, **state**, **city**, **street**, **building**, **floor**, **room** are all pre-defined semantics to describe the semantic of locations. All semantic locations have to conform to this semantic schema.

### 2.4. wifi

Table **wifi** stores Wi-Fi received signal strengths sampled by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | BSSID TEXT NOT NULL | SSID TEST | RSSI REAL NOT NULL | capability TEXT | freq REAL |**

with **PRIMARY KEY (uuid, epoch, BSSID)**.

**BSSID** is the media access control address (MAC address) of the access point. It is globally unique. **SSID** is the Wi-Fi network name. **RSSI** is received signal strength indicator, which is the received/detected signal level in dBm. **capability** describes the authentication, key management, and encryption schemes supported by the access point. **freq** is the frequency in MHz of the channel over which the client is communicating with the access point. More information is available [here](http://developer.android.com/reference/android/net/wifi/ScanResult.html).


### 2.5. audio

Table **audio** stores audio data recorded by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | source TEXT | channel INTEGER NOT NULL | sampwidth INTEGER NOT NULL | framerate INTEGER NOT NULL | nframes INTEGER NOT NULL | raw BLOB NOT NULL |**

with **PRIMARY KEY (uuid, epoch)**.

**source** is the source of audio, such as *MIC* and *CAMCORDER* [[ref](http://developer.android.com/reference/android/media/MediaRecorder.AudioSource.html)]. **channel** describes the number of separated streams of audio data. **sampwidth** is the number of bytes per audio sample (a.k.a. frame). **framerate** is the sample rate expressed in Hertz, which indicates the number of sample/frame recorded every second. **nframes** is the number of frames the audio data has.


To convert the audio data to wav file, here is an example python codes.
```python

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
```

### 2.6. geoloc

Table **geoloc** stores geographical location points collected by the device GPS or network service. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | longitude REAL NOT NULL | latitude REAL NOT NULL | altitude REAL | bearing REAL | speed REAL | accuracy REAL | provider TEXT |**

with **PRIMARY KEY (uuid, epoch)**.

**longitude** and **latitude** are longitude and latitude data in degrees. **altitude** is in meters above sea level. **bearing** is bearing in degrees. Bearing is the horizontal direction of travel of this device, and is not related to the device orientation. It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing. **speed** in meters/second over ground. If speed is 0.0 then it means this location does not have a speed data. 

**accuracy** is the estimated accuracy of this location, in meters. We define accuracy as the radius of 68% confidence. In other words, if you draw a circle centered at this location's latitude and longitude, and with a radius equal to the accuracy, then there is a 68% probability that the true location is inside the circle. In statistical terms, it is assumed that location errors are random with a normal distribution, so the 68% confidence circle represents one standard deviation. Note that in practice, location errors do not always follow such a simple distribution. This accuracy estimation is only concerned with horizontal accuracy, and does not indicate the accuracy of bearing, velocity or altitude if those are included in this Location. If this location does not have an accuracy, then 0.0 is returned. 

**provider** is the name of the provider that generated this location.

More information is available [here](http://developer.android.com/reference/android/location/Location.html).


### 2.7. acc

Table **acc** stores accelerometer data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**sysnano** is the timestamp of the most precise timer available on the local system, in nanoseconds. Equivalent to Linux's CLOCK_MONOTONIC. This timestamp should only be used to measure a duration by comparing it against another timestamp on the same device. Values returned by this method do not have a defined correspondence to wall clock times; the zero value is typically whenever the device last booted [<a href="http://developer.android.com/reference/java/lang/System.html#nanoTime()">ref</a>]. **eventnano** is the device uptime in nanosecond at which the sensor data sampling (event) happened [[ref](https://code.google.com/p/android/issues/detail?id=7981)]. **x**, **y**, and **z** are acceleration minus gravity on the each axis, in SI units (m/s^2) [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]. **accuracy** is the accuracy of the sensor data [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#accuracy)].


### 2.8. lacc

Table **lacc** stores linear accelerometer data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

A three dimensional vector indicating acceleration along each device axis, not including gravity. All values have units of m/s^2. The coordinate system is the same as is used by the acceleration sensor. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.9. gravity

Table **gravity** stores gravity data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

A three dimensional vector indicating the direction and magnitude of gravity. Units are m/s^2. The coordinate system is the same as is used by the acceleration sensor. When the device is at rest, the output of the gravity sensor should be identical to that of the accelerometer. The output of the accelerometer, gravity and linear-acceleration sensors must obey the following relation [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]:

**acceleration = gravity + linear-acceleration**

### 2.10. gyro

Table **gyro** stores gyroscope data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

All values are in radians/second and measure the rate of rotation around the device's local X, Y and Z axis. The coordinate system is the same as is used for the acceleration sensor. Rotation is positive in the counter-clockwise direction. That is, an observer looking from some positive location on the x, y or z axis at a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the standard mathematical definition of positive rotation. In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.11. rotation

Table **rotation** stores rotation data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | xr REAL NOT NULL | yr REAL NOT NULL | zr REAL NOT NULL | cos REAL | head_accuracy REAL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

The rotation vector represents the orientation of the device as a combination of an angle and an axis, in which the device has rotated through an angle θ around an axis (x, y, z). The three elements of the rotation vector are (x\*sin(θ/2), y\*sin(θ/2), z\*sin(θ/2)), such that the magnitude of the rotation vector is equal to sin(θ/2), and the direction of the rotation vector is equal to the direction of the axis of rotation. The three elements of the rotation vector are equal to the last three components of a unit quaternion (cos(θ/2), x\*sin(θ/2), y\*sin(θ/2), z\*sin(θ/2)).
Elements of the rotation vector are unitless. The x,y, and z axis are defined in the same way as the acceleration sensor.

The reference coordinate system is defined as a direct orthonormal basis, where:
X is defined as the vector product Y.Z (It is tangential to the ground at the device's current location and roughly points East). Y is tangential to the ground at the device's current location and points towards magnetic north. Z points towards the sky and is perpendicular to the ground. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]

**accuracy** is estimated heading accuracy in radians. If the value is -1, it means estimated heading accuracy is not available.


### 2.12. magnetic

Table **magnetic** stores magnetic data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | x REAL NOT NULL | y REAL NOT NULL | z REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

All values are in micro-Tesla (uT) and measure the ambient magnetic field in the X, Y and Z axis. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.13. light

Table **light** stores light sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | light REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**light** is ambient light level in SI lux units. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.14. temp

Table **temp** stores ambient temperature data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | temp REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**temp** is ambient (room) temperature in degree Celsius. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.15. pressure

Table **pressure** stores atmospheric pressure data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | pressure REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**pressure** is atmospheric pressure in hPa (millibar). [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.16. proximity

Table **proximity** stores proximity sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | proximity REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**proximity** is proximity sensor distance measured in centimeters. Some proximity sensors only support a binary near or far measurement. In this case, the sensor should report its *maximum range value* in the far state and a lesser value in the near state. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]


### 2.17. humidity

Table **humidity** stores humidity sensor data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | sysnano INTEGER NOT NULL | eventnano INTEGER NOT NULL | humidity REAL NOT NULL | accuracy REAL |**

with **PRIMARY KEY (uuid, epoch, sysnano, eventnano)**.

**humidity** is relative ambient air humidity in percent. When relative ambient air humidity and ambient temperature are measured, the dew point and absolute humidity can be calculated. [[ref](http://developer.android.com/reference/android/hardware/SensorEvent.html#values)]



## 3. Android App Interface

BearLoc has an Android library that takes care of all communication and data collection, providing simple interfaces for Android applications using an Android service [[ref](http://developer.android.com/guide/components/services.html)]. This section describes how to build your application using BearLoc Android library.


### 3.1. Bind BearLocService


### 3.2. Interfaces

Interfaces are the only thing you need to know after binding to BearLocService.



#### 3.1.1. SemLocListener Interface
**SemLocListener** is the interface to listen to returned estimated location back from BearLoc server. It is defined as follows.

```java
public interface SemLocListener {
  public abstract void onSemLocInfoReturned(JSONObject semLocInfo);
}
```

When your application asks for localization, it should pass an object that implements this interface, as we will discuss soon. 

There is one method 
```java
public abstract void onSemLocInfoReturned(JSONObject semLocInfo);
```
in this interface, which will be called by BearLocService when location is returned by localziation service, and the parameter semLocInfo contains all the information of returned semantic location. 

**semLocInfo** is a JSONObject [[ref](http://www.json.org/)], which has following structure (as an example). 

```json
{
  "semloc": {
    "country": "US",
    "state": "CA",
    "city": "berkeley",
    "street": "Leroy Ave",
    "building": "Soda Hall",
    "floor": "Floor 4",
    "room": "494"
  },
  "alter": {
    "country": {
      "US": 0.95,
      "Canada": 0.05
    },
    "state": {
      "CA": 0.87,
      "MA": 0.21,
      "TX": 0.02
    },
    "city": {
      "berkeley": 0.98,
      "San Francisco": 0.02
    },
    "street": {
      "Leroy Ave": 1.0,
    },
    "building": {
      "Soda Hall": 1.0,
     },
    "floor": {
      "Floor 3": 0.67,
      "Floor 4": 0.33
    },
    "room": {
      "494": 0.54,
      "410": 0.33,
      "RADLab Kitchen": 0.13
    }
  },
  "sem": {
    "country": {
      "state": {
        "city": {
          "street": {
            "building": {
              "floor": {
                "room": {
                }
              }
            }
          }
        }
      }
    }
}
```

In this example, `"semloc"`, `"alter"`, `"sem"` are fixed top-layer keys, indicating the best estimated semantic location, alternative locations under different semantics, and tree structure of semantics, respectively. And the second-layer keys `"country"`, `"state"`, `"city"`, `"street"`, `"building"`, `"floor"`, and `"room"` are all semantics predefined in schema in `"sem"`. In `"semloc"`, all values are strings, which are locations reported by users. In `"alter"`, all values are numbers that are less than 1, which are the confidence about each estimated location that the server has. All confidences under one semantic should sum to 1. in `"sem"`, it is a tree describing the semantic schema. This represents how the server understands the semantics. Currently every semantic only have child, but presumably, they can have multiple children. For example, besides *room*, we can have *ventilation zone* or *lighting zone* inside one floor. We may remove the `"sem"` part in the future, so please don't make your application reply on it.


#### 3.1.2. MetaListener Interface
**MetaListener** is the listener to listen to returned metadata back from BearLoc server. It is defined as follows.

```java
public interface MetaListener {
  public abstract void onMetaReturned(JSONObject meta);
}
```

Similar to SemLocListener, your application should pass an object that implements this interface when asking for metadata of a semantic location. In addition, your application also needs to provide an semantic location to query the metadata. We will discuss the details in next part.

There is also one method 
```java
public abstract void onMetaReturned(JSONObject meta);
```
in this interface, which will be called by BearLocService when metadata is returned from server.

**meta** is also an JSONObject, with an example of it as follows.

```json
{
  "country": ["US", "Canada"], 
  "state": ["CA", "MA"],
  "city": ["Berkeley", "San Francisco", "Mountain View"], 
  "street": ["Leroy Ave", "Telegraph Ave"], 
  "building": ["Soda Hall"],
  "floor": ["Floor 3", "Floor 4"],
  "room": ["410", "494", "RADLab Kitchen", "417", "415", "Wozniak Lounge"]
}
```

In each semantic, it is a list of all known locations on server that are under the same semantic and **location context** of the semantic location that your application provides. **Location context** means the list of locations that are under the semantics that are higher than the semantic of the location your are talking about (targeting) in the semantic tree. For example, ["US", "CA", "Berkeley", "Leroy Ave"] is the location context of "Soda Hall", but not the location context of "Floor 4" or "Cory Hall". A location context and location consist of one concrete location on the world.



#### 3.1.3. SemLocService Interface
SemLocService is the interface that should be implemented by any semantic localization service, such as BearLocService. It is defined as follows.

```java
public interface SemLocService {
  public abstract boolean localize(SemLocListener listener);
  public abstract boolean report(JSONObject semloc);
  public abstract boolean meta(JSONObject semloc, MetaListener listener);
}
```

Call them


**semloc** should be 
```json
{
  "country": "US",
  "state": "CA",
  "city": "berkeley",
  "street": "Leroy Ave",
  "building": "Soda Hall",
  "floor": "Floor 4",
  "room": "494"
}
```



### 3.3. Utilities
