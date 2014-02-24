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

from .interface import ILocation

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
import pymongo


@implementer(ILocation)
class Location(object):
    """Location class"""

    def __init__(self, db):
        self._db = db
        self._data = self._db.data

        self._train_itvl = 60*60 # in second
        reactor.callLater(10, self._train)

        self._clf = None
        self._bssids = None
        self._wifi_minrssi = -150
        self._wifi_min_itvl = 500
        self._wifi_max_itvl = 2000
        self._wifi_train_epoch_thld = 5000 # ms
        self._wifi_predict_epoch_thld = 5000 # ms

        # hardcoded
        self._sems = ("country", "state", "city", "street", "building", "locale")


    def get(self, query):
        """Execute localization service.

        Return {semantic: location}
        """
        d = defer.Deferred()
        reactor.callLater(0, self._get, query, d)

        return d


    # Maybe it is good to make this block to ensure fast response
    def _get(self, query, d):
        if not query or len(query) >= 3:
            # TODO: more error info can be passed here
            d.errback(Exception())

        query_id = query[0]
        query_epoch = int(query[1]) if len(query) == 2 else time.time() * 1000

        est_loc = self._predict(query_id, query_epoch)
        if est_loc:
            est_loc['type'] = "estimated semloc"
            est_loc['id'] = "0" # TODO add uuid for server
        response = [est_loc,]

        d.callback(response)


    def _predict(self, query_id, query_epoch):
        est_loc = self._predict_wifi(query_id, query_epoch)

        return est_loc


    def _predict_wifi(self, query_id, query_epoch):
        if self._clf == None or self._bssids == None:
            return {}

        query = {'id': query_id}
        query['epoch'] = {'$exists': True, '$type': 18, '$lte': query_epoch + self._wifi_predict_epoch_thld, '$gte': query_epoch - self._wifi_predict_epoch_thld} # type 18: 64-bit integer
        query['BSSID'] = {'$exists': True, '$type': 2} # type 2: String
        query['RSSI'] = {'$exists': True, '$type': 16, '$lt': 0} # type 16: 32-bit integer
        query['type'] = 'wifi'

        wifis = [doc for doc in self._data.find(query).sort('epoch', pymongo.ASCENDING)] # 1: sort ascending

        if len(wifis) == 0:
            query['epoch'] = {'$exists': True, '$type': 18} # type 18: 64-bit integer
            wifis = [doc for doc in self._data.find(query).sort('epoch', pymongo.DESCENDING).limit(50)] # 1: sort ascending

            if len(wifis) == 0:
                return {}

            query_epoch = wifis[0]['epoch']

        self._wifi_calibrate_epoch(wifis)

        epochs = list(set(map(lambda x: x['epoch'], wifis)))
        # sigs: {BSSID: RSSI}
        # TODO use itertools gourpby
        sigs = [{w['BSSID']: w['RSSI'] for w in wifis if w['epoch']==epoch} for epoch in epochs]
        data = [[sig.get(bssid, self._wifi_minrssi) for bssid in self._bssids] for sig in sigs]
        data = np.array(data)

        locations = self._clf.predict(data)
        count = collections.Counter(locations)
        location = count.most_common(1)[0][0]
        location = eval(location) # should be evaluated as a dictionary
        location['epoch'] = query_epoch
        location['target id'] = query_id
        return location


    def _train(self):
        """Train model."""
        # TODO online/incremental training
        log.msg("Started Loc training")

        self._train_wifi()

        # Only schedule next training task when this one finishes
        reactor.callLater(self._train_itvl, self._train)

        log.msg("Stopped Loc training")
        print self._clf


    def _train_wifi(self):
        # only interested in location detailed in locale
        query = {'id': {'$exists': True}}
        query = {sem:{'$exists': True, '$type': 2} for sem in self._sems} # type 2: String
        query['epoch'] = {'$exists': True, '$type': 18} # type 18: 64-bit integer
        query['type'] = 'reported semloc'
        all_locations = [doc for doc in self._data.find(query)]
        device_ids = tuple(set([doc['id'] for doc in all_locations]))

        query = {'id': {'$exists': True, '$in': device_ids}}
        query['epoch'] = {'$exists': True, '$type': 18} # type 18: 64-bit integer
        query['BSSID'] = {'$exists': True, '$type': 2} # type 2: String
        query['RSSI'] = {'$exists': True, '$type': 16, '$lt': 0} # type 16: 32-bit integer
        query['type'] = 'wifi'
        all_wifis = [doc for doc in self._data.find(query)]

        sigs = [] # wifi signatures
        classes = [] # locations corresponding to sigs
        bssids = [] # BSSIDs of wifi data
        for device_id in device_ids:
            print device_id

            locations = [doc for doc in all_locations if doc['id'] == device_id]
            wifis = [doc for doc in all_wifis if doc['id'] == device_id]

            if len(wifis) == 0 or len(locations) == 0:
                continue

            # sort locations and wifi based on epoch
            locations.sort(key=lambda x: x['epoch'])
            wifis.sort(key=lambda x: x['epoch'])

            self._wifi_calibrate_epoch(wifis)

            # group wifi {BSSID:RSSI, ...} by epoch
            wifisigs = [(epoch, {doc['BSSID']:doc['RSSI'] for doc in group}) \
                        for epoch, group in itertools.groupby(wifis, key=lambda x: x['epoch'])]

            loc_epochs = [doc['epoch'] for doc in locations]
            for epoch, sig in wifisigs:
                bisect_idx = bisect.bisect_left(loc_epochs, epoch)
                low_idx = bisect_idx-1 if bisect_idx > 0 else 0
                closest_location = min([location for location in locations[low_idx:bisect_idx+1]], \
                                        key=lambda x: x['epoch'])
                epochdiff = abs(epoch - closest_location['epoch'])
                cls = str({sem:closest_location[sem] for sem in self._sems})

                if epochdiff <= self._wifi_train_epoch_thld:
                    sigs.append(sig)
                    classes.append(cls)
                    bssids.extend(sig.keys())

        if len(sigs) == 0 or len(classes) == 0:
            return

        self._bssids = tuple(set(bssids))
        data = [[sig.get(bssid, self._wifi_minrssi) for bssid in self._bssids] for sig in sigs]
        data = np.array(data)
        classes = np.array(classes)

        self._clf = tree.DecisionTreeClassifier().fit(data, classes)


    def _wifi_calibrate_epoch(self, wifis):
        """Fix that some devices return result of same scan with slightly different timestamps"""
        wifis.sort(key=lambda x: x['epoch'])
        last_epoch = 0
        adjust_wnd = self._wifi_min_itvl
        for wifi in wifis:
            epoch = wifi['epoch']
            if 0 < epoch - last_epoch <= adjust_wnd:
                wifi['epoch'] = last_epoch
            else:
                last_epoch = epoch


