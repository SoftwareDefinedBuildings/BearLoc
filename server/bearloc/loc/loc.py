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
    reactor.callLater(0, self._train)
    self._train_interval = 43200
    
    # TODO put the table names in settings file
    self._wifi_table = 'wifi'
    self._wifi_clf = {} # {semantic: estimator}
    self._wifi_minrssi = -150


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return {loc:{label: loc} dict, metadata: tree of label, confidence: value of confidence} 
    """
    events = request.get('sensor data').get('wifi').get('events')
    events = sorted(events, key=lambda x: x['timestamp'])
    sig = {event['BSSID']:event['level'] for event in events}
    data = np.array([sig.get(bssid, -150) for bssid in self._wifi_bssids])
    
    loc = {'country':'US', 'state':'CA', 'city':'Berkeley', 'district':'UC Berkeley', 'building':'Soda Hall', 'floor':'Floor 4'}
    zone = self._wifi_clf['zone'].predict(data)
    loc['zone'] = zone
    
    metadata = self._tree()
    metadata['country']['state']['city']['district']['building']['floor']['zone']
    metadata['country']['state']['city']['street']

    confidence = 1

    locinfo = {'loc': loc, 'metadata': metadata, 'confidence': confidence}
    
    return defer.succeed(locinfo)


  def _tree(self):
    """Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    """
    return defaultdict(self._tree)


  @defer.inlineCallbacks
  def _train(self):
    """Train model for all data."""
    # TODO online/incremental training
    
    # extract attributes and store in db
    operation = "SELECT epoch, BSSID, RSSI, Loc FROM " + self._wifi_table
    d = self._db.runQuery(operation)
    records = yield d
   
    epochs = list(set(map(lambda x: x[0], records)))
    # TODO create estimator for all semantics
    zones = [set([json.loads(record[3]).get('zone', None) for record in records if record[0]==epoch]) for epoch in epochs]

    # remove zone set whose elements are not identical
    map(lambda x: x.pop() if len(x) == 1 else None, zones)
    epochs = [epochs[i] for i in range(0, len(epochs)) if zones[i]]
    zones = [zones[i] for i in range(0, len(zones)) if zones[i]]

    # sigs: {BSSID: RSSI}
    sigs = [{record[1]: record[2] for record in records if record[0]==epoch} for epoch in epochs]
    
    bssids = tuple(set(map(lambda x: x[1], records)))
    data = np.array([[sig.get(bssid, self._wifi_minrssi) for bssid in bssids] for sig in sigs])
    zones = np.array(zones)
    
    # TODO only update when there are enough new data
    self._wifi_clf['zone'] = tree.DecisionTreeClassifier().fit(data, zones)
    
    self._wifi_bssids = bssids

    # Only schedule next training task when this one finishes
    reactor.callLater(self._train_interval, self._train)
