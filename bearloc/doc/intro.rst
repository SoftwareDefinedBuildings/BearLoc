BearLoc Introduction
====================

BearLoc is an semantic indoor localization service.


System Architecture
-------------------

BearLoc consists of a cloud service that performs all localization intelligence and mobile end library for application to use.




Concepts
--------

There are some basic concepts that infiltrate everywhere in the design and implementation of BearLoc.

Semantic
^^^^^^^^

**Semantic** is the type of a location in people's mind when talking about it. For example, when saying "United States", we know is a **country**. And when saying "room 404", we know it is a **room**. Semantics conforms to hierarchy relationships in people's mind. For example, a **state** must be inside a **country**, and a **floor** must be in a **building**. We also call this hierarchy a **semantic tree**.

**semantic** enables BearLoc "understand" what people are talking about and manage different levels of informations appropriately. For example, when a user indicates she is in a room, the system can provide only room level services to her, such as temperature control and lighting, which is least overhead for a building controlling system. Other services include navigation in floors, and overview of information of a building.


Name
^^^^

A **name** is what people use to refer to a place, such as "United States", "room 404", "Soda Hall", etc. Every name people describe has a type, which is the *semantic* we described above. BearLoc never store a name individually, because there is no way for it to understand what place a name refers to without other information. As an example, for BearLoc, "Soda Hall" can refer to numerous buildings (or even rooms) in the world, but it can only refer to this "`Soda Hall <http://www.berkeley.edu/map/3dmap/3dmap.shtml?soda>`__" when we say its semantic is **building**, and its **street** is "Leroy Ave", its **city** is "Berkeley", its **state** is "CA", and its **country** is "US". This is why we need to introduce **location** below in BearLoc.



Location
^^^^^^^^

A **location** is how BearLoc refers to a place, versus **name** used by human. A location consists of a set of pairs of semantics and names. The semantics in a location must be a branch in semantic tree, i.e. they have to be connected in the semantic tree. And the semantic of the location has to be the lowest level of the branch. One example of location is

.. code-block:: json

   {
     "country": "US",
     "state": "CA",
     "city": "Berkeley",
     "street": "Leroy Ave",
     "building": "Soda Hall",
     "floor": "Floor 4",
     "room": "404"
   }