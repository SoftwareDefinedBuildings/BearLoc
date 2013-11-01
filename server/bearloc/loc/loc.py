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
import bisect
import itertools


class Loc(object):
  """Loc class"""
  
  implements(ILoc)
  
  def __init__(self, db):
    self._db = db
    self._train_interval = 10*60 # in second
    self._train_history = 30*24*60*60 # in second = 30 days
    reactor.callLater(0, self._train)
    
    self._clf_infos = {} # classifiers {(semloc, semantic): {"clf": clf, ...}}

    self._geoloc_epoch_thld = 60*60*1000 # in ms, 1 hour
    self._wifi_minrssi = -150
    self._wifi_epoch_thld = 1500 # ms


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
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "country"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))

    (countrys, confidence) = self._predict_geoloc(clf_info, request)
    count = Counter(countrys)
    semloc["country"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_state(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "state"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))

    (states, confidence) = self._predict_geoloc(clf_info, request)
    count = Counter(states)
    semloc["state"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_city(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "city"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))

    (citys, confidence) = self._predict_geoloc(clf_info, request)
    count = Counter(citys)
    semloc["city"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_street(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "street"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))

    (streets, confidence) = self._predict_geoloc(clf_info, request)
    count = Counter(streets)
    semloc["street"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_building(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "building"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))
    
    (buildings, confidence) = self._predict_geoloc(clf_info, request)
    count = Counter(buildings)
    semloc["building"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_floor(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "floor"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))

    (floors, confidence) = self._predict_wifi(clf_info, request)
    count = Counter(floors)
    semloc["floor"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_room(self, (semloc, confidence), request):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), "room"))
    if clf_info == None:
      return defer.succeed((semloc, confidence))
    
    (rooms, confidence) = self._predict_wifi(clf_info, request)
    count = Counter(rooms)
    semloc["room"] = count.most_common(1)[0][0]
    return defer.succeed((semloc, confidence))


  def _predict_geoloc(self, clf_info, request):
    clf = clf_info["clf"]

    locepoch = request['epoch']
    thld = 60*60 # in second, 1 hour

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

    locations = clf.predict(data)
    confidence = 1

    return (locations, confidence)


  def _predict_wifi(self, clf_info, request):
    bssids = clf_info["bssids"]
    clf = clf_info["clf"]

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
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    data = np.array(data)

    if len(data) == 0:
      return defer.succeed((semloc, confidence))

    locations = clf.predict(data)
    confidence = 1

    return (locations, confidence)


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

    condsemlocs = [{}]
    condsemlocs = yield self._train_country(condsemlocs)
    condsemlocs = yield self._train_state(condsemlocs)
    condsemlocs = yield self._train_city(condsemlocs)
    condsemlocs = yield self._train_street(condsemlocs)
    condsemlocs = yield self._train_building(condsemlocs)
    condsemlocs = yield self._train_floor(condsemlocs)
    yield self._train_room(condsemlocs)

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train)
    
    log.msg("Stopped Loc training")
  

  def _train_country(self, condsemlocs):
    condsems = ()
    new_condsemlocs = self._model(condsems, condsemlocs, "country", "geoloc")

    return defer.succeed(new_condsemlocs)


  def _train_state(self, condsemlocs):
    condsems = ("country", )
    new_condsemlocs = self._model(condsems, condsemlocs, "state", "geoloc")

    return defer.succeed(new_condsemlocs)


  def _train_city(self, condsemlocs):
    condsems = ("country", "state")
    new_condsemlocs = self._model(condsems, condsemlocs, "city", "geoloc")

    return defer.succeed(new_condsemlocs)


  def _train_street(self, condsemlocs):
    condsems = ("country", "state", "city")
    new_condsemlocs = self._model(condsems, condsemlocs, "street", "geoloc")

    return defer.succeed(new_condsemlocs)


  def _train_building(self, condsemlocs):
    condsems = ("country", "state", "city", "street")
    new_condsemlocs = self._model(condsems, condsemlocs, "building", "geoloc")

    return defer.succeed(new_condsemlocs)


  def _train_floor(self, condsemlocs):
    condsems = ("country", "state", "city", "street", "building")
    new_condsemlocs = self._model(condsems, condsemlocs, "floor", "wifi")

    return defer.succeed(new_condsemlocs)


  def _train_room(self, condsemlocs):
    condsems = ("country", "state", "city", "street", "building", "floor")
    new_condsemlocs = self._model(condsems, condsemlocs, "room", "wifi")

    return defer.succeed(new_condsemlocs)


  def _model(self, condsems, condsemlocs, sem, type):
    """Build model for sem under conditional sems and semlocs.

    return new conditional semlocs for lower level."""
    new_condsemlocs = []

    for condsemloc in condsemlocs:
      if not all(condsem in condsemloc for condsem in condsems):
        continue

      cur = self._db.cursor()
    
      # extract locations stored in db
      operation = "SELECT DISTINCT epoch, " + sem + " FROM " + "semloc"
      conds = [condsem+"='"+condsemloc[condsem]+"'" for condsem in condsems]
      if conds:
        operation += " WHERE " + " AND ".join(conds) + " AND " + sem + " IS NOT NULL"
      else:
        operation += " WHERE " + sem + " IS NOT NULL"
      cur.execute(operation)
      locations = cur.fetchall()

      if len(locations) == 0:
        continue 
        
      clf = None
      if type == "geoloc":
        (clf, locations) = self._train_geoloc(locations)
        if clf != None:
          self._clf_infos[(frozenset(condsemloc.items()), sem)] = {"clf": clf}
      elif type == "wifi":
        (clf, locations, bssids) = self._train_wifi(locations)
        if clf != None:
          self._clf_infos[(frozenset(condsemloc.items()), sem)] = {"clf": clf, "bssids": bssids}

      if clf:
        for location in set(locations):
          new_condsemloc = condsemloc.copy()
          new_condsemloc[sem] = location
          new_condsemlocs.append(new_condsemloc)

    return new_condsemlocs


  # TODO make this defer.inlineCallbacks
  def _train_geoloc(self, locations):
    cur = self._db.cursor()

    # extract attributes stored in db
    operation = "SELECT DISTINCT epoch, longitude, latitude FROM " + "geoloc"
    cur.execute(operation)
    geoloc = cur.fetchall()

    if len(geoloc) == 0 or len(locations) == 0:
      return (None, None) 
      
    # filter epochs that do not have condsemloc logged
    epochs = list(set(map(lambda x: x[0], geoloc)))
    timediffs = [abs(epoch - min(locations, key=lambda x: abs(x[0] - epoch))[0]) for epoch in epochs]
    epochs = [epochs[i] for i in range(0, len(timediffs)) if timediffs[i] <=  self._geoloc_epoch_thld]
   
    # TODO this is not optimal
    data = [(g[1], g[2]) for epoch in epochs for g in geoloc if g[0]==epoch]
    
    classes = [min(locations, key=lambda x: abs(x[0] - epoch))[1] for epoch in epochs]

    data = np.array(data)
    classes = np.array(classes)

    # TODO only update when there are enough new data
    if len(data) == 0 or len(classes) == 0:
      return (None, None) 
    
    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes)


  def _train_wifi(self, locations):
    cur = self._db.cursor()

    curepoch = int(round(time.time() * 1000))
    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history*1000)
    cur.execute(operation)
    wifi = cur.fetchall()

    if len(wifi) == 0 or len(locations) == 0:
      return (None, None, None)

    # sort wifi and locations based on epoch
    getepoch_f = lambda x: x[0]
    wifi.sort(key=getepoch_f)
    locations.sort(key=getepoch_f)
    # group wifi {BSSID:RSSI} by epoch
    wifisigs = [(epoch, {bssid:rssi for (epoch, bssid, rssi) in group}) \
                for epoch, group in itertools.groupby(wifi, key=getepoch_f)]
    
    sigs = []
    epochs = []
    classes = []  # locations corresponding to sigs
    locepochs = [location[0] for location in locations]
    for epoch, sig in wifisigs:
      bisect_idx = bisect.bisect_left(locepochs, epoch)
      low_idx = bisect_idx-1 if bisect_idx > 0 else 0
      (epochdiff, epoch, cls) = min( \
                                 [(abs(epoch - location[0]), location[0], location[1]) \
                                  for location in locations[low_idx:bisect_idx+1]], \
                                key=lambda x: x[0])

      if epochdiff <= self._wifi_epoch_thld:
        sigs.append(sig)
        epochs.append(epoch)
        classes.append(cls)

    bssids = tuple(set(map(lambda x: x[1], wifi)))
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]

    data = np.array(data)
    classes = np.array(classes)

    # TODO only update when there are enough new data
    if len(data) == 0 or len(classes) == 0:
      return (None, None, None)
    
    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes, bssids)