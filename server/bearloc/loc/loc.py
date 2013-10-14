#!/usr/bin/env python
# encoding: utf-8

from __future__ import division

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
    self._train_interval = 600
    self._train_history = 2592000000 # 30 days
    reactor.callLater(0, self._train)
    
    # TODO put the table names in settings file
    self._wifi_clf = {} # {semantic: estimator}
    self._wifi_minrssi = -150


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return {loc:{label: loc} dict, sem: tree of semantic, confidence: value of confidence, meta: list of candidates of deapest semantic} 
    """
    # TODO handle the case there is trained model
    dl = self._predict(request)
    dl.addCallback(self._aggr)

    return dl

  
  def _predict(self, request):
    d1 = defer.Deferred()
    reactor.callLater(0, self._predict_wifi, request, d1)

    dl = defer.DeferredList([d1])
    return dl


  def _predict_wifi(self, request, d):
    if 'room' not in self._wifi_clf:
      d.callback((None, 1))
      return

    locepoch = request['epoch']
    thld = 5000 # ms

    cur = self._db.cursor()
    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                " WHERE ABS(epoch-" + str(locepoch) + ") <= " + str(thld)
    cur.execute(operation)
    wifi = cur.fetchall()

    if len(wifi) == 0:
      d.callback((None, 1))
      return 

    epochs = list(set(map(lambda x: x[0], wifi)))
    # sigs: {BSSID: RSSI}
    sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
    bssids = self._wifi_bssids 
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    data = np.array(data)

    if len(data) == 0:
      d.callback((None, 1))
      return

    rooms = self._wifi_clf["room"].predict(data)
    count = Counter(rooms)
    d.callback((count.most_common(1)[0][0], count.most_common(1)[0][1]/len(rooms)))


  def _aggr(self, results):
    # wifi only now, hard code to get reuslt
    loc = {"country":"US", "state":"CA", "city":"Berkeley", "street":"Leroy Ave", "district":"UC Berkeley", \
           "building":"Soda Hall", "floor":"Floor 4"}
    room = results[0][1][0]
    loc["room"] = room

    confidence = results[0][1][1]
    
    sem = self._sem()

    meta = self._meta(loc)

    locinfo = {"loc": loc, "confidence": confidence, "sem": sem, "meta": meta}

    return locinfo


  def _sem(self):
    sem = self._tree()
    sem["country"]["state"]["city"]["street"]
    sem["country"]["state"]["city"]["district"]["building"]["floor"]["room"]

    return sem


  def _meta(self, loc):
    cur = self._db.cursor()

    operation = "SELECT DISTINCT country FROM " + "semloc"
    cur.execute(operation)
    country = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT state FROM " + "semloc" + " WHERE country='" + loc["country"] + "'"
    cur.execute(operation)
    state = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT city FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'"
    cur.execute(operation)
    city = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT street FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'" + " AND city='" + loc["city"] + "'"
    cur.execute(operation)
    street = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT district FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'" + " AND city='" + loc["city"] + "'"
    cur.execute(operation)
    district = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT building FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'" + " AND city='" + loc["city"] + "'" + \
                " AND district='" + loc["district"] + "'"
    cur.execute(operation)
    building = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT floor FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'" + " AND city='" + loc["city"] + "'" + \
                " AND district='" + loc["district"] + "'" + " AND building='" + loc["building"] + "'"
    cur.execute(operation)
    floor = [x[0] for x in cur.fetchall()]

    operation = "SELECT DISTINCT room FROM " + "semloc" + " WHERE country='" + loc["country"] + "'" + \
                " AND state='" + loc["state"] + "'" + " AND city='" + loc["city"] + "'" + \
                " AND district='" + loc["district"] + "'" + " AND building='" + loc["building"] + "'" + \
                " AND floor='" + loc["floor"] + "'"
    cur.execute(operation)
    room = [x[0] for x in cur.fetchall()]

    meta = {"country":country, "state":state, "city":city, "street":street, "district":district, \
            "building":building, "floor":floor, "room":room}

    return meta


  def _tree(self):
    """Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    """
    return defaultdict(self._tree)


  def _train(self):
    """Train model for all data."""
    # TODO online/incremental training
    log.msg("Started Loc training")

    self._train_wifi()

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train)
    
    log.msg("Stopped Loc training")
  

  def _train_wifi(self):
    """Train model for wifi data."""
    cur = self._db.cursor()

    curepoch = int(round(time.time() * 1000))
    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi" + \
                " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history)
    cur.execute(operation)
    wifi = cur.fetchall()
  
    # TODO create estimator for all semantics
    operation = "SELECT DISTINCT epoch, room FROM " + "semloc" + \
                " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history)
    cur.execute(operation)
    roomloc = cur.fetchall()

    if len(wifi) == 0 or len(roomloc) == 0:
      return 
      
    # filter epochs that do not have semloc logged
    epochs = list(set(map(lambda x: x[0], wifi)))
    thld = 1500 # ms
    timediffs = [abs(epoch - min(roomloc, key=lambda x: abs(x[0] - epoch))[0]) for epoch in epochs]
    epochs = [epochs[i] for i in range(0, len(timediffs)) if timediffs[i] <= thld]
   
    # sigs: {BSSID: RSSI}
    sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
    bssids = tuple(set(map(lambda x: x[1], wifi)))
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    
    rooms = [min(roomloc, key=lambda x: abs(x[0] - epoch))[1] for epoch in epochs]

    data = np.array(data)
    rooms = np.array(rooms)

    # TODO only update when there are enough new data
    if len(data) == 0 or len(rooms)== 0:
      return
    
    self._wifi_clf["room"] = tree.DecisionTreeClassifier().fit(data, rooms)
    self._wifi_bssids = bssids
