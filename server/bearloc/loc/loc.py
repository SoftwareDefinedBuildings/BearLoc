from __future__ import division

"""
Copyright (c) 2013, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions 
are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
"""
"""
@author Kaifei Chen <kaifei@eecs.berkeley.edu>
"""

from bearloc.loc.interface import ILoc

from twisted.internet import defer, reactor
from twisted.python import log
from zope.interface import implements
from collections import defaultdict
import numpy as np
from sklearn import tree
from collections import Counter
import time


class Loc(object):
  """Loc class"""
  
  implements(ILoc)
  
  def __init__(self, db):
    self._db = db
    self._train_interval = 10*60 # in second
    self._train_history = 30*24*60*60 # in second = 30 days
    reactor.callLater(0, self._train)
    

    self._clfs = {} # classifiers {(semloc, semantic): classifier}
    self._wifi_bssids = {}
    self._wifi_minrssi = -150


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return {semloc:{label: loc} dict, confidence: value of confidence, sem: tree of semantic} 
    """
    d = self._localize(request)

    return d


  @defer.inlineCallbacks
  def _localize(self, request):
    (semloc, confidence) = ({}, 1)
    (semloc, confidence) = yield self._predict_country((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_state((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_city((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_street((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_building((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_floor((semloc, confidence), request)
    (semloc, confidence) = yield self._predict_room((semloc, confidence), request)

    semlocinfo = yield self._aggr((semloc, confidence))
    
    defer.returnValue(semlocinfo)


  def _predict_country(self, (semloc, confidence), request):
    clf = self._clfs.get((frozenset(semloc.items()), "country"))
    if clf == None:
      return defer.succeed((semloc, confidence))

    locepoch = request['epoch']
    thld =  60*60 # in second, 1 hour

    cur = self._db.cursor()
    # extract attributes and store in db
    operation = "SELECT longitude, latitude FROM " + "geoloc" + \
                " WHERE ABS(epoch-" + str(locepoch) + ") <= " + str(thld*1000) + \
                " ORDER BY ABS(epoch-" + str(locepoch) + ")" + \
                " LIMIT 1"
    cur.execute(operation)
    geoloc = cur.fetchall()

    if len(geoloc) == 0:
      return defer.succeed((semloc, confidence))

    data = np.array(geoloc)

    country = clf.predict(data)
    semloc["country"] = country[0]
    confidence = 1

    return defer.succeed((semloc, confidence))


  def _predict_state(self, (semloc, confidence), request):
    semloc["state"] = "CA"
    return defer.succeed((semloc, confidence))


  def _predict_city(self, (semloc, confidence), request):
    semloc["city"] = "Berkeley"
    return defer.succeed((semloc, confidence))


  def _predict_street(self, (semloc, confidence), request):
    semloc["street"] = "Leroy Ave"
    return defer.succeed((semloc, confidence))


  def _predict_building(self, (semloc, confidence), request):
    semloc["building"] = "Soda Hall"
    return defer.succeed((semloc, confidence))


  def _predict_floor(self, (semloc, confidence), request):
    semloc["floor"] = "Floor 4"
    return defer.succeed((semloc, confidence))


  def _predict_room(self, (semloc, confidence), request):
    clf = self._clfs.get((frozenset(semloc.items()), "room"))
    if clf == None:
      return defer.succeed((semloc, confidence))

    locepoch = request['epoch']
    thld = 5000 # ms

    cur = self._db.cursor()
    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                " WHERE ABS(epoch-" + str(locepoch) + ") <= " + str(thld)
    cur.execute(operation)
    wifi = cur.fetchall()

    if len(wifi) == 0:
      return defer.succeed((semloc, confidence))

    epochs = list(set(map(lambda x: x[0], wifi)))
    # sigs: {BSSID: RSSI}
    sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
    bssids = self._wifi_bssids[frozenset(semloc.items())]
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    data = np.array(data)

    if len(data) == 0:
      return defer.succeed((semloc, confidence))

    rooms = clf.predict(data)
    count = Counter(rooms)
    semloc["room"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _aggr(self, (semloc, confidence)):
    sem = self._sem()

    semlocinfo = {"semloc": semloc, "confidence": confidence, "sem": sem}

    return semlocinfo


  def _sem(self):
    # Kepp semantic tree linear, it is too much hassles to deal with different branches
    # hardcoded here
    sem = self._tree()
    sem["country"]["state"]["city"]["street"]["building"]["floor"]["room"]

    return sem


  def _tree(self):
    """Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    """
    return defaultdict(self._tree)


  @defer.inlineCallbacks
  def _train(self):
    """Train model for all semantic."""
    # TODO online/incremental training
    log.msg("Started Loc training")

    semlocs = [{}]
    semlocs = yield self._train_country(semlocs)
    semlocs = yield self._train_state(semlocs)
    semlocs = yield self._train_city(semlocs)
    semlocs = yield self._train_street(semlocs)
    semlocs = yield self._train_building(semlocs)
    semlocs = yield self._train_floor(semlocs)
    self._train_room(semlocs)

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train)
    
    log.msg("Stopped Loc training")
  

  def _train_country(self, semlocs):
    new_semlocs = []
    for semloc in semlocs:
      if len(semloc) != 0:
        continue

      cur = self._db.cursor()

      curepoch = int(round(time.time() * 1000))
      # extract attributes stored in db
      operation = "SELECT DISTINCT epoch, longitude, latitude FROM " + "geoloc"
      cur.execute(operation)
      geoloc = cur.fetchall()
    
      # extract locations stored in db
      operation = "SELECT DISTINCT epoch, country FROM " + "semloc" + \
                  " WHERE country IS NOT NULL"
      cur.execute(operation)
      countrys = cur.fetchall()

      if len(geoloc) == 0 or len(countrys) == 0:
        continue 
        
      # filter epochs that do not have semloc logged
      epochs = list(set(map(lambda x: x[0], geoloc)))
      thld = 0.5*24*60*60*1000 # in ms, 0.5 day
      timediffs = [abs(epoch - min(countrys, key=lambda x: abs(x[0] - epoch))[0]) for epoch in epochs]
      epochs = [epochs[i] for i in range(0, len(timediffs)) if timediffs[i] <= thld]
     
      data = [(g[1], g[2]) for epoch in epochs for g in geoloc if g[0]==epoch]
      
      countrys = [min(countrys, key=lambda x: abs(x[0] - epoch))[1] for epoch in epochs]

      data = np.array(data)
      countrys = np.array(countrys)

      # TODO only update when there are enough new data
      if len(data) == 0 or len(countrys)== 0:
        continue
      
      self._clfs[(frozenset(semloc.items()), "country")] = tree.DecisionTreeClassifier().fit(data, countrys)
      
      for country in set(countrys):
        semloc["country"] = country
        new_semlocs.append(semloc)

    return defer.succeed(new_semlocs)


  def _train_state(self, semlocs):
    return defer.succeed([{"country": "US", "state": "CA"}])


  def _train_city(self, semlocs):
    return defer.succeed([{"country": "US", "state": "CA", "city": "Berkeley"}])


  def _train_street(self, semlocs):
    return defer.succeed([{"country": "US", "state": "CA", "city": "Berkeley", "street": "Leroy Ave"}])


  def _train_building(self, semlocs):
    return defer.succeed([{"country": "US", "state": "CA", "city": "Berkeley", "street": "Leroy Ave", "building": "Soda Hall"}])


  def _train_floor(self, semlocs):
    return defer.succeed([{"country": "US", "state": "CA", "city": "Berkeley", "street": "Leroy Ave", "building": "Soda Hall", "floor": "Floor 4"}])


  def _train_room(self, semlocs):
    new_semlocs = []
    condsems = ("country", "state", "city", "street", "building", "floor")

    for semloc in semlocs:
      if not all(k in semloc for k in condsems):
        continue

      cur = self._db.cursor()

      curepoch = int(round(time.time() * 1000))
      # extract attributes and store in db
      operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                  " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history*1000)
      cur.execute(operation)
      wifi = cur.fetchall()
    
      operation = "SELECT DISTINCT epoch, room FROM " + "semloc"
      conds = [sem+"='"+semloc[sem]+"'" for sem in condsems]
      operation += " WHERE " + " AND ".join(conds) + " AND room IS NOT NULL"
      cur.execute(operation)
      rooms = cur.fetchall()

      if len(wifi) == 0 or len(rooms) == 0:
        continue 
        
      # filter epochs that do not have semloc logged
      epochs = list(set(map(lambda x: x[0], wifi)))
      thld = 1500 # ms
      timediffs = [abs(epoch - min(rooms, key=lambda x: abs(x[0] - epoch))[0]) for epoch in epochs]
      epochs = [epochs[i] for i in range(0, len(timediffs)) if timediffs[i] <= thld]
     
      # sigs: {BSSID: RSSI}
      sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
      bssids = tuple(set(map(lambda x: x[1], wifi)))
      data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
      
      rooms = [min(rooms, key=lambda x: abs(x[0] - epoch))[1] for epoch in epochs]

      data = np.array(data)
      rooms = np.array(rooms)

      # TODO only update when there are enough new data
      if len(data) == 0 or len(rooms)== 0:
        continue
      
      self._clfs[(frozenset(semloc.items()), "room")] = tree.DecisionTreeClassifier().fit(data, rooms)
      self._wifi_bssids[frozenset(semloc.items())] = bssids
