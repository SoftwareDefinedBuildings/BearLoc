Android App Interface
=====================

BearLoc has an Android library that takes care of all communication and data collection, providing simple interfaces for Android applications using an Android service [`ref <http://developer.android.com/guide/components/services.html>`__]. This section describes how to build your application using BearLoc Android library.


Bind BearLocService
-------------------

Your application needs to bind to BearLocService in order to use it. The details of service binding is introduced `here <http://developer.android.com/guide/components/bound-services.html#Binding>`__. Below is an example of binding to BearLocService.

.. code-block:: java

   BearLocService mBearLocService;
   private ServiceConnection mBearLocConn = new ServiceConnection() {
       // Called when the connection with the service is established
       public void onServiceConnected(ComponentName className, IBinder service) {
           // Because we have bound to an explicit
           // service that is running in our own process, we can
           // cast its IBinder to a concrete class and directly access it.
           BearLocBinder binder = (BearLocBinder) service;
           mBearLocService = binder.getService();
           mBound = true;
       }

       // Called when the connection with the service disconnects unexpectedly
       public void onServiceDisconnected(ComponentName className) {
           mBound = false;
       }
   };


After doing this in your application's Activity or Service, you can access the functionalities that BearLocService provides indicated in interface **SemLocService**, which is introduced in following part. Quick examples of calling BealocService are as follows.

.. code-block:: java

   // ask for localization with an instance of SemLocListener
   mBearLocService.localize(this);
   // report current location with an instance of semantic location
   mBearLocService.report(semloc);
   // Ask for metadata with an instance of MetaListener
   mBearLocService.meta(semloc, this);


Details about the listeners and the interface are in next part.


Interfaces
----------

Interfaces are the only thing you need to know after binding to BearLocService.

SemLocListener Interface
^^^^^^^^^^^^^^^^^^^^^^^^

**SemLocListener** is the interface to listen to returned estimated location back from BearLoc server. It is defined as follows.

.. code-block:: java

   public interface SemLocListener {
     public abstract void onSemLocInfoReturned(JSONObject semLocInfo);
   }


When your application asks for localization, it should pass an object that implements this interface, as we will discuss soon. 

There is one method 

.. code-block:: java

   public abstract void onSemLocInfoReturned(JSONObject semLocInfo);

in this interface, which will be called by BearLocService when location is returned by localization service, and the parameter semLocInfo contains all the information of returned semantic location. 

**semLocInfo** is a **JSONObject** [`ref1 <http://www.json.org/>`__, `ref2 <http://developer.android.com/reference/org/json/JSONObject.html>`__], which has following structure (as an example). 

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


MetaListener Interface
^^^^^^^^^^^^^^^^^^^^^^

**MetaListener** is the listener to listen to returned metadata back from BearLoc server. It is defined as follows.

.. code-block:: java

   public interface MetaListener {
     public abstract void onMetaReturned(JSONObject meta);
   }

Similar to SemLocListener, your application should pass an object that implements this interface when asking for metadata of a semantic location. In addition, your application also needs to provide an semantic location to query the metadata. We will discuss the details in next part.

There is also one method 

.. code-block:: java

   public abstract void onMetaReturned(JSONObject meta);

in this interface, which will be called by BearLocService when metadata is returned from server.

**meta** is also an **JSONObject**, with an example of it as follows.

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


In each semantic, it is a list of all known locations on server that are siblings of the give semantic location from your application. 



SemLocService Interface
^^^^^^^^^^^^^^^^^^^^^^^

SemLocService is the interface that should be implemented by any semantic localization service, such as BearLocService. It is defined as follows.

.. code-block:: java

   public interface SemLocService {
     public abstract boolean localize(SemLocListener listener);
     public abstract boolean report(JSONObject semloc);
     public abstract boolean meta(JSONObject semloc, MetaListener listener);
   }


localize
""""""""

The first method is for localization request.

.. code-block:: java

   public abstract boolean localize(SemLocListener listener);


Your application can call it after binding to BearLocServie as we described above. When calling this method, you should pass an instance of **SemLocListener** to it. The method **onSemLocInfoReturned** in SemLocListener will be called when the location is returned from server.

This ``localize`` method returns a boolean indicating whether it successfully scheduled a localization request for caller. If it returns **true**, then **onSemLocInfoReturned** is guaranteed to be called later for this localization request. If it returns **false**, then **onSemLocInfoReturned** is guaranteed NOT to be called later for this localization request.


report
""""""

The second method is used for reporting your current location.

.. code-block:: java

   public abstract boolean report(JSONObject semloc);


Your application must try best to ensure this **semloc** is correct location, such as taking the semantic location from user input.

**semloc** is an **JSONObject**. As an exmaple, the structure of **semloc** should be

.. code-block:: json

   {
     "country": "US",
     "state": "CA",
     "city": "Berkeley",
     "street": "Leroy Ave",
     "building": "Soda Hall",
     "floor": "Floor 4",
     "room": "494"
   }


It is not required to include all semantics in the **semloc**, but semantics in **semloc** has to start from top-level semantic all the way down to the lowest-level semantic in your **semloc**. In other words, **semloc** cannot have hole in its semantics. For example, if your application is sure the device is in building *Soda Hall*, but not sure which floor and room it is, then it can just call ``report`` with **semloc**

.. code-block:: json

   {
     "country": "US",
     "state": "CA",
     "city": "Berkeley",
     "street": "Leroy Ave",
     "building": "Soda Hall",
   }


But it cannot report **semloc** 

.. code-block:: json

   {
     "building": "Soda Hall",
   }


Intuitively this requirement is valid, because the user should know the locations in the higher semantics. Like she/he must know *Soda Hall* is in *Berkeley* and on *Leroy Ave*. Otherwise it is not possible for our system to distinguish another possible *Soda Hall* at some other place in the world.


meta
""""
The third method is to get metadata of a semantic location.

.. code-block:: java

   public abstract boolean meta(JSONObject semloc, MetaListener listener);


The **semloc** you should pass to ``meta`` has the same restrictions as the ``report``. And the returned **meta** in MetaListener is described before in MetaListener section.


Utilities
---------

BearLoc library has some utilities, such as **DeviceUUID** and **JSONHttpPostTask**.