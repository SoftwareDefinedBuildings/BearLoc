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
    
    # TODO put the table names in settings file
    self._wifi_clf = {} # {semantic: estimator}
    self._wifi_minrssi = -150


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return {semloc:{label: loc} dict, confidence: value of confidence, sem: tree of semantic} 
    """
    # TODO handle the case there is trained model
    dl = self._predict(request)

    return dl

  
  def _predict(self, request):
    d1 = defer.Deferred()
    reactor.callLater(0, self._predict_wifi, request, d1)

    dl = defer.DeferredList([d1])
    dl.addCallback(self._aggr)
    
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
    loc = {"country":"US", "state":"CA", "city":"Berkeley", "street":"Leroy Ave", "building":"Soda Hall", "floor":"Floor 4"}
    room = results[0][1][0]
    loc["room"] = room

    confidence = results[0][1][1]
    
    sem = self._sem()

    meta = self._meta(loc)

    locinfo = {"semloc": loc, "confidence": confidence, "sem": sem}

    return locinfo


  def _sem(self):
    # Kepp semantic tree linear, it is too much hassles to deal with different branches
    # hardcoded here
    sem = self._tree()
    sem["country"]["state"]["city"]["street"]["building"]["floor"]["room"]

    return sem


  def _meta(self, loc):
    # semantics sequence should be compatible with semantic tree
    # hardcoded here
    semseq = ['country', 'state', 'city', 'street', 'building', 'floor', 'room']

    country = self._siblings(loc, semseq, 'country')
    state = self._siblings(loc, semseq, 'state')
    city = self._siblings(loc, semseq, 'city')
    street = self._siblings(loc, semseq, 'street')
    building = self._siblings(loc, semseq, 'building')
    floor = self._siblings(loc, semseq, 'floor')
    room = self._siblings(loc, semseq, 'room')

    meta = {"country":country, "state":state, "city":city, "street":street, "building":building, \
            "floor":floor, "room":room}

    return meta


  def _siblings(self, loc, semseq, targetsem):
    cur = self._db.cursor()
    
    condsems = semseq[0:semseq.index(targetsem)]
    operation = "SELECT DISTINCT " + targetsem + " FROM " + "semloc"
    conds = [sem+"='"+loc[sem]+"'" for sem in condsems]
    if conds:
      operation += " WHERE " + " AND ".join(conds)
    cur.execute(operation)
    siblings = [x[0] for x in cur.fetchall()]

    return siblings


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
                " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history*1000)
    cur.execute(operation)
    wifi = cur.fetchall()
  
    # TODO create estimator for all semantics
    operation = "SELECT DISTINCT epoch, room FROM " + "semloc" + \
                " WHERE " + str(curepoch) + "-epoch<=" + str(self._train_history*1000) + \
                " AND room IS NOT NULL"
    cur.execute(operation)
    rooms = cur.fetchall()

    if len(wifi) == 0 or len(rooms) == 0:
      return 
      
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
      return
    
    self._wifi_clf["room"] = tree.DecisionTreeClassifier().fit(data, rooms)
    self._wifi_bssids = bssids
