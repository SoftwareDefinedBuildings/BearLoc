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
from zope.interface import implementer
from collections import defaultdict
import numpy as np
from sklearn import tree
import collections
import time
import bisect
import itertools


@implementer(ILoc)
class Loc(object):
  """Loc class"""
  
  def __init__(self, db):
    self._db = db
    self._train_interval = 30*60 # in second
    reactor.callLater(0, self._train_all)
    
    self._clf_infos = {} # classifiers {(semloc, semantic): {"clf": clf, ...}}

    self._geoloc_train_epoch_thld = 60*60*1000 # in ms, 1 hour
    self._geoloc_predict_epoch_thld = 60*60*1000 # in ms, 1 hour
    self._wifi_minrssi = -150
    self._wifi_train_epoch_thld = 2000 # ms
    self._wifi_predict_epoch_thld = 5000 # ms

    # semantics list should be compatible with semantic tree in loc.py
    # hardcoded here
    self._sems = ("country", "state", "city", "street", "building", "floor", "room")


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return {semloc: {semantic: loc} dict, alter: {semantic: {alternative location: confidence}}, sem: tree of semantic} 
    """
    d = defer.Deferred()
    reactor.callLater(0, self._localize, request, d)

    return d


  # Maybe it is good to make this block to ensure fast response
  def _localize(self, request, d):
    device = request.get('device')
    uuid = device.get('uuid') if device != None else None
    locepoch = request.get('epoch')
    
    semloc = {}
    alter = {}
    if uuid != None and locepoch != None :
      for sem in self._sems:
        if self._predict(uuid, locepoch, (semloc, alter), sem) == False:
          break

    semlocinfo = self._aggr(semloc, alter)
    
    d.callback(semlocinfo)


  def _predict(self, uuid, locepoch, (semloc, alter), targetsem):
    clf_info = self._clf_infos.get((frozenset(semloc.items()), targetsem))
    if clf_info != None:
      if targetsem not in ("floor", "room"):
        (loc, alter_locs) = self._predict_geoloc(uuid, locepoch, clf_info)
      else:
        (loc, alter_locs) = self._predict_wifi(uuid, locepoch, clf_info)

      if loc != None and alter_locs != None:
        semloc[targetsem] = loc
        alter[targetsem] = alter_locs
        return True

    return False


  def _predict_geoloc(self, uuid, locepoch, clf_info):
    clf = clf_info["clf"]

    cur = self._db.cursor()
    # extract attributes and store in db
    operation = "SELECT longitude, latitude FROM " + "geoloc" + \
                " WHERE uuid='" + str(uuid) + "'" + \
                " AND ABS(epoch-" + str(locepoch) + ") <= " + \
                str(self._geoloc_predict_epoch_thld) + \
                " ORDER BY ABS(epoch-" + str(locepoch) + ")" + \
                " LIMIT 1"
    cur.execute(operation)
    geoloc = cur.fetchall()

    if len(geoloc) == 0:
      return (None, None)

    data = np.array(geoloc)

    locations = clf.predict(data)
    count = collections.Counter(locations)
    location = count.most_common(1)[0][0]
    alter = {loc: float(cnt)/len(locations) for loc, cnt in count.items()}
    return (location, alter)


  def _predict_wifi(self, uuid, locepoch, clf_info):
    bssids = clf_info["bssids"]
    clf = clf_info["clf"]

    cur = self._db.cursor()
    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                " WHERE uuid='" + str(uuid) + "'" + \
                " AND ABS(epoch-" + str(locepoch) + ") <= " + \
                str(self._wifi_predict_epoch_thld)
    cur.execute(operation)
    wifi = cur.fetchall()

    if len(wifi) == 0:
      return (None, None)

    epochs = list(set(map(lambda x: x[0], wifi)))
    # sigs: {BSSID: RSSI}
    # TODO use itertools gourpby
    sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    data = np.array(data)

    locations = clf.predict(data)
    count = collections.Counter(locations)
    location = count.most_common(1)[0][0]
    alter = {loc: float(cnt)/len(locations) for loc, cnt in count.items()}
    return (location, alter)


  def _aggr(self, semloc, alter):
    sem = self._sem()
    semlocinfo = {"semloc": semloc, "alter": alter, "sem": sem}
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
  def _train_all(self):
    """Train model for all semantic."""
    # TODO online/incremental training
    log.msg("Started Loc training")

    condsemlocs = [{}]
    for sem in self._sems:
      condsemlocs = yield self._train(condsemlocs, sem)
      if len(condsemlocs) == 0:
        break

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train_all)
    
    log.msg("Stopped Loc training")


  def _train(self, condsemlocs, targetsem):
    """Build model for sem under conditional sems and semlocs.

    return new conditional semlocs for lower level."""
    cur = self._db.cursor()
    operation = "SELECT DISTINCT uuid FROM device;"
    cur.execute(operation)
    uuids = cur.fetchall()

    condsems = self._sems[0:self._sems.index(targetsem)]

    new_condsemlocs = []

    for condsemloc in condsemlocs:
      if not all(condsem in condsemloc for condsem in condsems):
        continue

      uuidlocations = {}
      for uuid, in uuids:
        cur = self._db.cursor()
        # extract locations stored in db
        operation = "SELECT DISTINCT epoch, " + targetsem + " FROM " + "semloc" + \
                    " WHERE uuid='" + uuid + "'"
        conds = [condsem+"='"+condsemloc[condsem]+"'" for condsem in condsems]
        if conds:
          operation += " AND " + " AND ".join(conds)
        operation += " AND " + targetsem + " IS NOT NULL;"
        cur.execute(operation)
        locations = cur.fetchall()
        uuidlocations[uuid] = locations
        
      clf = None
      if targetsem not in ("floor", "room"):
        (clf, locations) = self._train_geoloc(uuidlocations)
        if clf != None:
          self._clf_infos[(frozenset(condsemloc.items()), targetsem)] = {"clf": clf}
      else:
        (clf, locations, bssids) = self._train_wifi(uuidlocations)
        if clf != None:
          self._clf_infos[(frozenset(condsemloc.items()), targetsem)] = {"clf": clf, "bssids": bssids}

      if clf != None:
        for location in set(locations):
          new_condsemloc = condsemloc.copy()
          new_condsemloc[targetsem] = location
          new_condsemlocs.append(new_condsemloc)

    return new_condsemlocs


  # TODO make this defer.inlineCallbacks
  def _train_geoloc(self, uuidlocations):
    cur = self._db.cursor()

    uuidgeolocs = {}
    for uuid in uuidlocations.keys():
      # extract attributes stored in db
      operation = "SELECT DISTINCT epoch, longitude, latitude FROM geoloc" + \
                  " WHERE uuid='" + uuid + "'"
      cur.execute(operation)
      geolocs = cur.fetchall()
      uuidgeolocs[uuid] = geolocs
    
    data = []  # geoloc data
    classes = []  # locations corresponding to data
    getepoch_f = lambda x: x[0]
    for uuid in uuidlocations.keys():
      geolocs = uuidgeolocs[uuid]
      locations = uuidlocations[uuid]

      if len(geolocs) == 0 or len(locations) == 0:
        continue

      # sort geolocs and locations based on epoch
      geolocs.sort(key=getepoch_f)
      locations.sort(key=getepoch_f)

      locepochs = [location[0] for location in locations]
      for epoch, lon, lat in geolocs:
        bisect_idx = bisect.bisect_left(locepochs, epoch)
        low_idx = bisect_idx-1 if bisect_idx > 0 else 0
        (epochdiff, cls) = min( \
                            [(abs(epoch - location[0]), location[1]) \
                             for location in locations[low_idx:bisect_idx+1]], \
                           key=getepoch_f)

        if epochdiff <= self._geoloc_train_epoch_thld:
          data.append((lon, lat))
          classes.append(cls)

    if len(data) == 0 or len(classes) == 0:
      return (None, None) 

    data = np.array(data)
    classes = np.array(classes)
    
    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes)


  def _train_wifi(self, uuidlocations):
    cur = self._db.cursor()

    uuidwifis = {}
    for uuid in uuidlocations.keys():
      # extract attributes and store in db
      operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM wifi" + \
                  " WHERE uuid='" + uuid + "'"
      cur.execute(operation)
      wifis = cur.fetchall()
      uuidwifis[uuid] = wifis
    
    sigs = [] # wifi signatures 
    classes = []  # locations corresponding to sigs
    bssids = [] # BSSIDs of wifi data
    getepoch_f = lambda x: x[0]
    for uuid in uuidlocations.keys():
      wifis = uuidwifis[uuid]
      locations = uuidlocations[uuid]

      if len(wifis) == 0 or len(locations) == 0:
        continue

      # sort wifi and locations based on epoch
      wifis.sort(key=getepoch_f)
      locations.sort(key=getepoch_f)

      # group wifi {BSSID:RSSI} by epoch
      wifisigs = [(epoch, {bssid:rssi for (epoch, bssid, rssi) in group}) \
                  for epoch, group in itertools.groupby(wifis, key=getepoch_f)]

      locepochs = [location[0] for location in locations]
      for epoch, sig in wifisigs:
        bisect_idx = bisect.bisect_left(locepochs, epoch)
        low_idx = bisect_idx-1 if bisect_idx > 0 else 0
        (epochdiff, cls) = min( \
                            [(abs(epoch - location[0]), location[1]) \
                             for location in locations[low_idx:bisect_idx+1]], \
                           key=getepoch_f)

        if epochdiff <= self._wifi_train_epoch_thld:
          sigs.append(sig)
          classes.append(cls)
          bssids.extend(sig.keys())

    if len(sigs) == 0 or len(classes) == 0:
      return (None, None, None)

    bssids = tuple(set(bssids))
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    data = np.array(data)
    classes = np.array(classes)
    
    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes, bssids)