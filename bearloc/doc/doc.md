# BearLoc Documentation


## 1. Server RESTful Interface



## 2. Server Database Structure

Server is using sqlite3 database. The database name is **bearloc.db**, and *locates at /bearloc/server/bearloc.db*. There are several tables in the database. Adn the details of the database are discussed below.

### 2.1. device

Table **device** stores uuid, make, and model of devices that have reported to the database. Its structure is:

**| uuid TEXT NOT NULL | make TEXT | model TEXT |**

And the **PRIMARY KEY** is **uuid**.


### 2.2. sensormeta

Table **sensormeta** stores metadata of sensors on each devices. Its structure is:

**|uuid TEXT NOT NULL | sensor TEXT NOT NULL | vendor TEXT | name TEXT | power REAL | minDelay INTEGER | maxRange REAL | version INTEGER | resolution REAL |**

And the **PRIMARY KEY** is **(uuid, sensor)**.


### 2.3. semloc

Table **semloc** stores semantic locations reported by users. Its structure is:

**|uuid TEXT NOT NULL | epoch INTEGER NOT NULL | country TEXT | state TEXT | city TEXT | street TEXT | building TEXT | floor TEXT | room TEXT |**

And the **PRIMARY KEY** is **(uuid, epoch)**.


### 2.4. wifi

Table **wifi** stores Wi-Fi received signal strengths sampled by the device. Its structure is:

**uuid TEXT NOT NULL | epoch INTEGER NOT NULL | BSSID TEXT NOT NULL | SSID TEST | RSSI REAL NOT NULL | capability TEXT | freq REAL |**

And the **PRIMARY KEY** is **(uuid, epoch, BSSID)**.


### 2.5. audio

Table **audio** stores audio data recorded by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | source TEXT | channel INTEGER NOT NULL | sampwidth INTEGER NOT NULL | framerate INTEGER NOT NULL | nframes INTEGER NOT NULL | raw BLOB NOT NULL |**

And the **PRIMARY KEY** is **(uuid, epoch)**.


### 2.6. geoloc

Table **geoloc** stores geographical location points collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL | longitude REAL NOT NULL | latitude REAL NOT NULL | altitude REAL | bearing REAL | speed REAL | accuracy REAL | provider TEXT |**

And the **PRIMARY KEY** is **(uuid, epoch)**.


### 2.7. acc

Table **acc** stores accelerometer data collected by the device. Its structure is:

**| uuid TEXT NOT NULL | epoch INTEGER NOT NULL |  sysnano INTEGER NOT NULL |  eventnano INTEGER NOT NULL |  x REAL NOT NULL |  y REAL NOT NULL |  z REAL NOT NULL |  accuracy REAL |**

And the **PRIMARY KEY** is **(uuid, epoch, sysnano, eventnano)**.



## 3. Android App Interface
