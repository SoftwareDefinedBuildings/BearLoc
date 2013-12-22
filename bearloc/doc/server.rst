Server REST Resources
========================

BearLoc server provides an RESTful interface for data post and get. 


Location
--------

*Location* is the **estimated** semantic location of a device at a time. 

========================================================= ===================
Resource                                                  Description
========================================================= ===================
:ref:`GET location/:id <get-location-id>`                 Returns the most recent semantic location (and its epoch time) of device with specified id parameter.
:ref:`GET location/:id/:epoch <get-location-id-epoch>`    Returns the semantic location (and its epoch time) of device with specified id parameter at the closest available time with the specified epoch time.
========================================================= ===================


.. _get-location-id:

GET location/:id
^^^^^^^^^^^^^^^^

Returns the most recent semantic location (and its epoch time) of device with specified id parameter.


**Resource URL**

http://bearloc.cal-sdb.org:20080/api/location/:id


**Parameters**

========================== ===================
**id** *(required)*        The string of UUID of the device. (Example Value: 1d352410-4a5e-11e3-8f96-0800200c9a66)
========================== ===================


**Example Request**

========================== ===================
GET                        http://bearloc.cal-sdb.org/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66
========================== ===================

.. code-block:: json

   {
     "semloc": {
       "country": "US",
       "state": "CA",
       "city": "Berkeley",
       "street": "Leroy Ave",
       "building": "Soda Hall",
       "floor": "Floor 4",
       "room": "494"
     },
     "epoch": 1387670483532,
     "id": "1d352410-4a5e-11e3-8f96-0800200c9a66"
   }


.. _get-location-id-epoch:

GET location/:id/:epoch
^^^^^^^^^^^^^^^^^^^^^^^

Returns the semantic location (and its epoch time) of device with specified id parameter at the closest available time with the specified epoch time.


**Resource URL**

http://bearloc.cal-sdb.org:20080/api/location/:id/:epoch


**Parameters**

========================== ===================
**id** *(required)*        The string of UUID of the device. (Example Value: 1d352410-4a5e-11e3-8f96-0800200c9a66)
**epoch** *(required)*     The numerical value of epoch time in millisecond. (Example Value: 1384125523390)
========================== ===================


**Example Request**

========================== ===================
GET                        http://bearloc.cal-sdb.org/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66/1384125523390
========================== ===================

.. code-block:: json

   {
     "semloc": {
       "country": "US",
       "state": "CA",
       "city": "Berkeley",
       "street": "Leroy Ave",
       "building": "Soda Hall",
       "floor": "Floor 4",
       "room": "494"
     },
     "epoch": 1384125523375,
     "id": "1d352410-4a5e-11e3-8f96-0800200c9a66"
   }


Data
----

**Data** is the collections of data from all sensors, including the locations reported by users. Clients can report any data type, but only those specified in :ref:`Sensor Schema <sensor-schema>` will be useful for localization.

========================================================= ===================
Resource                                                  Description
========================================================= ===================
:ref:`GET data/:id <post-data-id>`                        Add new data of sensor of device with specified id parameter at the closest available time with the specified epoch time.
:ref:`POST data/:id <post-data-id>`                       Add new data of sensor of device with specified id parameter at the closest available time with the specified epoch time.
========================================================= ===================



.. code-block:: http

   POST /report
   Host: 54.242.57.128
   Authorization: Basic xxxxxxxxxxxxxxxxxxx
   Content-Length: nnn
   Content-Type: application/json
 
   {
     'sensormeta': {
       'acc': {
         'm axRange': 1,
         'vendor': 'st micro',
         'name': 'kr3dh',
         'power': 20,
         'minDelay': 0,
         'version': 1,
         'resolution': 1
       },
       ...
     },
     'device': {
       'make': 'LGE',
       'model': 'VS910 4G',
       'uuid': '5036b270-b584-3248-9322-93ce70a32f62'
     },
     'acc': [
       {
         'eventnano': 22325627610000,
         'sysnano': 22325532395689,
         'epoch': 1384128767709,
         'y': 0.054481390863657,
         'x': 0,
         'z': 9.779409408569336,
         'accuracy': 3
       }
     ],
     'wifi': [
       {
         'SSID': 'EECS-Secure ',
         'BSSID': '00:17:df:a7:33:12',
         'capability': '[WPA2-EAP-CCMP]',
         'epoch': 1384128767808,
         'frequency': 2462,
         'RSSI': -67
       },
       ...
       {
         'SSID': 'AirBears',
         'BSSID': '00:13:5f:55:d8:b0',
         'cap ability': '',
         'epoch': 1384128767809,
         'frequency': 2462,
         'RSSI': -92
       }
     ],
     ...
   }

The response of report HTTP POST will be a simple JSON object indicating whether the report is correctly received. One example is here

.. code-block:: json


  {
    'result': True
  }



meta
----

**meta** is an interface for client to query metadata of locations. It is also done with HTTP POST with JSON object. In HTTP POST request, the JSON obejct should be a semantic location, with an example as below:

.. code-block:: http

   POST /meta
   Host: 54.242.57.128
   Authorization: Basic xxxxxxxxxxxxxxxxxxx
   Content-Length: nnn
   Content-Type: application/json
 
   {
     "semloc": {
       "country": "US",
       "state": "CA",
       "city": "Berkeley",
       "street": "Leroy Ave",
       "building": "Soda Hall",
       "floor": "Floor 4",
       "room": "494"
     }
  }

The response is also a JSON object containing a dictionary of list of all known locations on server that are siblings of the give semantic location from your application. One example is as below:

.. code-block:: json

   {
     "country": ["US", "Canada"], 
     "state": ["CA", "MA"],
     "city": ["Berkeley", "San Francisco", "Mountain View"], 
     "street": ["Leroy Ave", "Telegraph Ave"], 
     "building": ["Soda Hall"],
     "floor": ["Floor 3", "Floor 4"],
     "room": ["410", "494", "RADLab Kitchen", "417", "415", "Wozniak Lounge"]
   }



**NOTE: we may add a meta request type field (in URL or request JSON object) in later version.**