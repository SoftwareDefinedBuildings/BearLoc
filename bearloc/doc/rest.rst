Server REST Resources
========================

BearLoc server provides an RESTful interface for data post and get. All data between client and server are JSON array of JSON objects. A JSON object represents an event happened somewhere, mostly in client or server. Examples of event include a Wi-Fi access point signal strength aquisition, or user-reported location, or server-generated estiamted location of a device. We have some predefined :doc:`schema </schema>` of the JSON Obejct we are using for localization.


Location
--------

Location is the **estimated** semantic location of a device at a time. 

====================================================== ====================================================================================================
Resource                                               Description
====================================================== ====================================================================================================
:ref:`GET location/:id[/:epoch] <get-location-id>`     Returns the semantic location (and its epoch time) of device with specified id parameter. The time of the location is the most recent time (if epoch parameter is not specified), or at the closest available time with the specified epoch time (if epoch parameter is specified).
====================================================== ====================================================================================================


.. _get-location-id:

GET location/:id[/:epoch]
^^^^^^^^^^^^^^^^^^^^^^^^^

Returns the semantic location (and its epoch time) of device with specified id parameter. The time of the location is the most recent time (if epoch parameter is not specified), or at the closest available time with the specified epoch time (if epoch parameter is specified).


**Resource URL**

http://bearloc.cal-sdb.org:10080/api/location/:id[/:epoch]


**Parameters**

============================ ====================================================================================================
**id** *(required)*          The string of UUID of the device. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
**epoch** *(optional)*       The numerical value of epoch time in millisecond. (Example Value: 1384125523390)
============================ ====================================================================================================


**Return Data**

Return data is a one-element JSON array of :ref:`an event JSON object with type "estimated semloc" <estimated-semantic-location>`.


**Example Request**

========================== ====================================================================================================
GET                        http://bearloc.cal-sdb.org:10080/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66
Return Data                *(See Below)*
========================== ====================================================================================================

.. code-block:: json

  [
    {
      "type": "estimated semloc",
      "id": "3f2cd8d0-9831-11e3-a5e2-0800200c9a66",
      "target id": "1d352410-4a5e-11e3-8f96-0800200c9a66",
      "epoch": 1387670483532,
      "country": "US",
      "state": "CA",
      "city": "Berkeley",
      "street": "Leroy Ave",
      "building": "Soda Hall",
      "locale": "494"
    }
  ]


Data
----

Data is the collections of event from all sensors, including the locations reported by users. One event is conceptually represented as a JSON object. We provide some recommended :doc:`schema </schema>` of events.

========================================= ====================================================================================================
Resource                                  Description
========================================= ====================================================================================================
:ref:`POST data/:id <post-data-id>`       Add new sensor data of device with specified id parameter.
========================================= ====================================================================================================


.. _post-data-id:

POST data/:id
^^^^^^^^^^^^^

Add new sensor data of device with specified id parameter.


**Resource URL**

http://bearloc.cal-sdb.org:10080/api/data/:id


**Parameters**

========================== ====================================================================================================
**id** *(required)*        The string of UUID of the device. (Example Value: "1d352410-4a5e-11e3-8f96-0800200c9a66")
========================== ====================================================================================================


**POST Data**

POST data is an JSON array of JSON objects that represent events. The event JSON objects are required to have **"type"** and **"id"** keys, otherwise the event will not be accepted by server. There is no specification on other keys and values, but we have an :doc:`schema </schema>` of event types, keys, and values. Only those data conform to the schema will be correctly parsed by our localization service.


**Return Data**

Return data is an one-element JSON Array of :ref:`an event JSON object with type "data reception" <data-reception>`.


**Example Request**

========================== ====================================================================================================
POST                       http://bearloc.cal-sdb.org:10080/api/location/1d352410-4a5e-11e3-8f96-0800200c9a66
POST Data                  *(See Below)*
========================== ====================================================================================================

.. code-block:: json
 
   [
     {
        "type": "sensor info",
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
        "type": "device info",
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


  [
    {
      "type": "data reception",
      "id": "3f2cd8d0-9831-11e3-a5e2-0800200c9a66",
      "posted": 6,
      "accepted": 6
    }
  ]



Candidate
---------

Candidate is the list of candidate locations given all upper level locations.

======================================================================================================= ====================================================================================================
Resource                                                                                                Description
======================================================================================================= ====================================================================================================
:ref:`GET candidate/:country[/:state[/:city[/:street[/:building[/:locale]]]]] <get-candidate>`          Returns the list of candidate locations at the lowest level of specified parameter.
======================================================================================================= ====================================================================================================


.. _get-candidate:

GET candidate/[:country/[:state/[:city/[:street/[:building]]]]]
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Returns the list of candidate locations that exists in the lowest level of semantic in specified parameters.


**Resource URL**

http://bearloc.cal-sdb.org:10080/api/candidate/[:country/[:state/[:city/[:street/[:building]]]]]


**Parameters**

=============================== ===================================================================================================================================
**country** *(optional)*        The string of name of the country of the query location. (Example Value: "US")
**state** *(optional)*          The string of name of the state of the query location. (Example Value: "CA")
**city** *(optional)*           The string of name of the state of the query location. (Example Value: "Berkeley")
**street** *(optional)*         The string of name of the state of the query location. (Example Value: "Leroy Ave")
**building** *(optional)*       The string of name of the state of the query location. (Example Value: "Soda Hall")
=============================== ===================================================================================================================================


**Return Data**

Return data is an one-element JSON array of :ref:`an event JSON object with type "location candidate" <location-candidate>`.


**Example Request**

========================== ===================================================================================================================================
GET                        http://bearloc.cal-sdb.org:10080/api/candidate/US/CA/Berkeley/Leroy%20Ave/Soda%20Hall
Return Data                *(See Below)*
========================== ===================================================================================================================================

.. code-block:: json
 
  [
    {
      "type": "location candidate",
      "id": "3f2cd8d0-9831-11e3-a5e2-0800200c9a66",
      "country": "US",
      "state": "CA",
      "city": "Berkeley",
      "street": "Leroy Ave",
      "building": "Soda Hall",
      "target semantic": "locale",
      "location candidate": ["410", "494", "RADLab Kitchen", "417", "415", "Wozniak Lounge"]
    }
  ]


**Example Request**

========================== ===================================================================================================================================
GET                        http://bearloc.cal-sdb.org:10080/api/candidates/US/CA
Return Data                *(See Below)*
========================== ===================================================================================================================================

.. code-block:: json

  [
    {
      "type": "location candidate",
      "id": "3f2cd8d0-9831-11e3-a5e2-0800200c9a66",
      "country": "US",
      "state": "CA",
      "target semantic": "city",
      "location candidate": ["Berkeley", "San Francisco", "Mountain View"]
    }
  ]
