Server REST Resources
========================

BearLoc server provides an RESTful interface for data post and get. 


Location
--------

Location is the **estimated** semantic location of a device at a time. 

========================================================= ====================================================================================================
Resource                                                  Description
========================================================= ====================================================================================================
:ref:`GET location/:id <get-location-id>`                 Returns the most recent semantic location (and its epoch time) of device with specified id parameter.
:ref:`GET location/:id/:epoch <get-location-id-epoch>`    Returns the semantic location (and its epoch time) of device with specified id parameter at the closest available time with the specified epoch time.
========================================================= ====================================================================================================


.. _get-location-id:

GET location/:id
^^^^^^^^^^^^^^^^

Returns the most recent semantic location (and its epoch time) of device with specified id parameter.


**Resource URL**

http://bearloc.cal-sdb.org:20080/api/location/:id


**Parameters**

========================== ====================================================================================================
**id** *(required)*        The string of UUID of the device. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
========================== ====================================================================================================


**Return Data**

Return data is an JSON object of an event with type "estimated semloc".


**Example Request**

========================== ====================================================================================================
GET                        http://bearloc.cal-sdb.org:20080/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66
Return Data                *(See Below)*
========================== ====================================================================================================

.. code-block:: json

   {
      "type": "estimated semloc",
      "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
      "epoch": 1387670483532,
      "country": "US",
      "state": "CA",
      "city": "Berkeley",
      "street": "Leroy Ave",
      "building": "Soda Hall",
      "locale": "494"
   }


.. _get-location-id-epoch:

GET location/:id/:epoch
^^^^^^^^^^^^^^^^^^^^^^^

Returns the semantic location (and its epoch time) of device with specified id parameter at the closest available time with the specified epoch time.


**Resource URL**

http://bearloc.cal-sdb.org:20080/api/location/:id/:epoch


**Parameters**

========================== ====================================================================================================
**id** *(required)*        The string of UUID of the device. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
**epoch** *(required)*     The numerical value of epoch time in millisecond. (Example Value: 1384125523390)
========================== ====================================================================================================


**Return Data**

Return data is an JSON object of an event with type "estimated semloc".


**Example Request**

========================== ====================================================================================================
GET                        http://bearloc.cal-sdb.org:20080/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66/1384125523390
Return Data                *(See Below)*
========================== ====================================================================================================

.. code-block:: json

   {
      "type": "estimated semloc",
      "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
      "epoch": 1384125523375,
      "country": "US",
      "state": "CA",
      "city": "Berkeley",
      "street": "Leroy Ave",
      "building": "Soda Hall",
      "locale": "494"
   }


Data
----

Data is the collections of data from all sensors, including the locations reported by users. Clients can report any data type, but only those specified in :ref:`Sensor Schema <sensor-schema>` will be useful for localization.

========================================================= ====================================================================================================
Resource                                                  Description
========================================================= ====================================================================================================
:ref:`POST data/:id <post-data-id>`                       Add new sensor data of device with specified id parameter.
========================================================= ====================================================================================================


.. _post-data-id:

POST data/:id
^^^^^^^^^^^^^

Add new sensor data of device with specified id parameter.


**Resource URL**

http://bearloc.cal-sdb.org:20080/api/data/:id


**Parameters**

========================== ====================================================================================================
**id** *(required)*        The string of UUID of the device. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
========================== ====================================================================================================


**POST Data**

POST data is an JSON array of JSON objects that represent events. The event JSON objects are required to have **"type"** and **"id"** keys, otherwise the event will not be accepted by server. There is no specification on other keys and values, but we have an :doc:`schema </schema>` of event types, keys, and values. Only those data conform to the schema will be correctly parsed by our localization service.


**Return Data**

Return data is an JSON object with keys **"reported"** and **"accepted"**. "reported" has a number value indicating the number of events reported, and "accepted" has a number value indicating the number of events accepted.


**Example Request**

========================== ====================================================================================================
POST                       http://bearloc.cal-sdb.org:20080/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66
POST Data                  *(See Below)*
========================== ====================================================================================================

.. code-block:: json
 
   [
     {
        "type": "sensor meta",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "sensor": "accelerometer",
        "vendor": "st micro",
        "model": "kr3dh",
        "version": "1",
        "unit": "m/s^2",
        "power": 20,
        "min delay": 0,
        "max range": 1,
        "resolution": 1
     },
     {
        "type": "device meta",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "make": "LGE",
        "model": "VS910 4G"
     },
     {
        "type": "accelerometer",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "epoch": 1384128767709,
        "y": 0.054481390863657,
        "x": 0,
        "z": 9.779409408569336,
        "accuracy": 3
     },
     {
        "type": "wifi",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "epoch": 1384128767808,
        "BSSID": "00:1a:df:a7:33:12",
        "SSID": "EECS-Open",
        "RSSI": -67,
        "capability": "[WPA2-EAP-CCMP]",
        "frequency": 2462
     },
     {
        "type": "wifi",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "epoch": 1384128767809,
        "BSSID": "00:13:5f:51:d8:b0",
        "SSID": "AirBears2",
        "RSSI": -92,
        "capability": "",
        "frequency": 2462
     },
     {
        "type": "reported semloc",
        "id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
        "epoch": 1384128515251,
        "country": "US",
        "state": "CA",
        "city": "Berkeley",
        "street": "Leroy Ave",
        "building": "Soda Hall",
        "locale": "494"
     }
   ]

========================== ====================================================================================================
Return Data                *(See Below)*
========================== ====================================================================================================

.. code-block:: json


  {
    "reported": 6,
    "accepted": 6
  }



Metadata
--------

Metadata is an interface for client to query metadata of locations. It is also done with HTTP POST with JSON object. In HTTP POST request, the JSON obejct should be a semantic location, with an example as below:

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
       "locale": "494"
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