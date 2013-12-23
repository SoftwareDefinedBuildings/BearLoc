Installation
============

To setup a BearLoc system, you need to have a running BearLoc server, and user needs an mobile application that uses BearLoc mobile library to talk to BearLoc server. You can also build your own mobile BearLoc client using our server RESTful interface. This part talks about how to setup a server program on your own server, and how to setup Android programming environment to use our BearLoc library. Note that we have one server running 24/7 already, so you only need to know how to develop application with our mobile library in most cases.


Set Up Server
-------------

Dependencies
^^^^^^^^^^^^

To run our server codes on your machine, you need to install some prerequisite packages in your system.

* `python 2.7 <http://www.python.org>`__
* `pip <http://www.pip-installer.org/>`__
* `twisted <http://www.twistedmatrix.com>`__
* `zope.interface <http://pypi.python.org/pypi/zope.interface>`__
* `numpy <http://www.numpy.org/>`__
* `scipy <http://www.scipy.org/>`__
* `scikit-learn <http://scikit-learn.org/>`__

On debian, you can install these with apt and pip:

.. code-block:: bash

   apt-get install build-essential python-dev python-scipy python-pip
   pip install twisted numpy scikit-learn zope.interface simplejson


On Mac OS X, you can install these with using `Homebrew <http://brew.sh/>`__ and pip. 


Run Server
^^^^^^^^^^

After getting all prerequisite packages, in terminal, go to /bearloc/server, and execute

.. code-block:: bash

   make run

. To kill the server, execute

.. code-block:: bash

   make stop


The server listens to port 10080. We will make the server an plug-in of twisted in later releases. We already have a server running at 54.242.57.128.


Set Up Android Programming Environment
--------------------------------------

To use BearLoc Android library, you need to import BeaLoc library project to your Android IDE. It should work in both `Eclipse <http://www.eclipse.org/>`__ and `Android Studio <http://developer.android.com/sdk/installing/studio.html>`__ (not tested). The details of setting up Android programming environment in Eclipse can be found `here <http://developer.android.com/sdk/installing/installing-adt.html>`__ and `here <http://developer.android.com/tools/projects/index.html>`__.
