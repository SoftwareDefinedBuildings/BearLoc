Server RESTful Interface
========================

BearLoc server provides an RESTful interface [`ref1 <http://en.wikipedia.org/wiki/Representational_state_transfer>`__, `ref2 <http://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm>`__] for data post and get. 


**Note: currently the design and implementation of server RESTful interface is not in stable version, expect potential changes in later versions.**


localize
--------

**NOTE: localize HTTP POST may be changed to location HTTP GET later.**

*localize* is the interface of server to request localization service. It is done by HTTP POST now. The client needs to call HTTP POST to server (port 10080) with ``localize`` as path. The POSTed object should be an `JSON object <http://www.json.org/>`__. Here is an example request:

.. code-block:: http

   POST /localize
   Host: 54.242.57.128
   Authorization: Basic xxxxxxxxxxxxxxxxxxx
   Content-Length: nnn
   Content-Type: application/json
 
   {
     "device": {
       "uuid": "1d352410-4a5e-11e3-8f96-0800200c9a66"
     },
     "epoch": 1384125523375
   }


And the HTTPP POST response is also an JSON object. If localization is done successfully, the response will contain estimated semantic location of the device specified by ``"uuid"``, as well as its information. If localization is not able to be done, it will return an empty JSON object. An example response of successful localization is:

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
         "Berkeley": 0.98,
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
   }


In this example, ``"semloc"``, ``"alter"``, ``"sem"`` are fixed top-layer keys, indicating the best estimated semantic location, alternative locations under different semantics, and tree structure of semantics, respectively. And the second-layer keys ``"country"``, ``"state"``, ``"city"``, ``"street"``, ``"building"``, ``"floor"``, and ``"room"`` are all semantics predefined in schema in ``"sem"``. In ``"semloc"``, all values are strings, which are locations reported by users. In ``"alter"``, all values are numbers that are less than 1, which are the confidence about each estimated location that the server has. All confidences under one semantic should sum to 1. in ``"sem"``, it is a tree describing the semantic schema. This represents how the server understands the semantics. Currently every semantic only have child, but presumably, they can have multiple children. For example, besides *room*, we can have *ventilation zone* or *lighting zone* inside one floor. We may remove the ``"sem"`` part in the future, so please don't make your application reply on it.


To make the localization work, client must report its data in the recent 5 seconds, via the **report** interface we described in next part. Our Android library makes sure it will report current sensor data to server right after application requests localization service and before the library really sends out an localization request to server.


** NOTE: we may change the server to return the most recent available location later. **


report
------

**report** is the interface for client to report data to server. This is the crucial functionality for BearLoc to operate. With no reported data, BearLoc cannot train any model, and provide real time localization service. Client and report any data type specified in :ref:`Sensor Schema <sensor-schema>`, and the fields it should report is specified in :doc:`database`. It is also done by HTTP POST. The client needs to call HTTP POST to server (port 10080) with ``report`` as path. The POSTed object should also be an JSON object, and it must contain a "device" value. Here is an example request:

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