#!/usr/bin/env python
# encoding: utf-8

from bearloc.loc.interface import ILoc

from twisted.internet import defer, reactor
from zope.interface import implements
from collections import defaultdict
import numpy as np
from sklearn import tree


class Loc(object):
  """Loc class"""
  
  implements(ILoc)
  
  def __init__(self, db):
    self._db = db
    self._train_interval = 43201
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

    locinfo = self._predict_wifi(request)
    
    return defer.succeed(locinfo)


  def _predict_wifi(self, request):
    events = request.get('wifi') # list of wifi events
    events = sorted(events, key=lambda x: x['timestamp'])
    sig = {event['BSSID']:event['level'] for event in events}
    data = np.array([sig.get(bssid, -150) for bssid in self._wifi_bssids])
   
    # hardcoded
    loc = {'country':'US', 'state':'CA', 'city':'Berkeley', 'district':'UC Berkeley', 'building':'Soda Hall', 'floor':'Floor 4'}
    room = self._wifi_clf['room'].predict(data) if self._wifi_clf['room'] else None
    loc['room'] = room
    
    sem = self._tree()
    sem['country']['state']['city']['district']['building']['floor']['room']
    sem['country']['state']['city']['street']

    confidence = 1
    
    # hardcoded
    meta = ["489", "487", "485", "483", "481", "479", "477", "475", "465H", "465HA", "465G", "465E", "465C", "465A", \
        "465B", "465D", "465F", "RADLab Kitchen", "465K", "405", "492", "494", "493", "495", "413", "415", "417", "419", \
        "421", "Wozniak Lounge", "Wozniak Lounge Kitchen", "420", "410", "420A", "442", "440", "449", "447", "445", "443", "441"]

    locinfo = {'loc': loc, 'metadata': metadata, 'confidence': confidence, 'meta': meta}

    return locinfo


  def _tree(self):
    """Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    """
    return defaultdict(self._tree)


  def _train(self):
    """Train model for all data."""
    # TODO online/incremental training

    self._train_wifi()

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train)
  

  def _train_wifi(self):
    """Train model for wifi data."""
    cur = self._db.cursor()

    # TODO handle the case there is no data to train

    # extract attributes and store in db
    operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM " + "wifi"
    cur.execute(operation)
    wifi = cur.fetchall()
   
    # TODO create estimator for all semantics
    operation = "SELECT DISTINCT epoch, location FROM " + "semloc" + \
                " WHERE semantic=" + "'room'"
    cur.execute(operation)
    roomloc = cur.fetchall()
   
    # filter epochs that do not have semloc logged
    epochs = list(set(map(lambda x: x[0], wifi)))
    thld = 3000 # ms
    timediff = [min(roomloc, key=lambda x: abs(x[0] - epoch))[0] for epoch in epochs]
    epochs = [epochs[i] for i in range(0, len(timediff)) if timediff[i] < thld]
    
    # sigs: {BSSID: RSSI}
    sigs = [{w[1]: w[2] for w in wifi if w[0]==epoch} for epoch in epochs]
    bssids = tuple(set(map(lambda x: x[1], wifi)))
    data = [[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs]
    
    rooms = [min(roomloc, key=lambda x: abs(x[0] - epoch))[1] for epoch in epochs]

    data = np.array(data)
    rooms = np.array(rooms)
    
    # TODO only update when there are enough new data
    if len(data) > 0 and len(rooms) > 0:
      self._wifi_clf['room'] = tree.DecisionTreeClassifier().fit(data, rooms)
    
      self._wifi_bssids = bssids
