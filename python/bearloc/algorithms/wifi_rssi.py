#!/usr/bin/env python

import sys
import socket
import capnp

import algorithm_capnp  # read algorithm.capnp

import random
import sqlite3
import bisect
import itertools
from sklearn import tree
import numpy as np
import simplejson as json
import threading
import collections


#db_path = "/root/workspace/BearLoc/python/bearloc/algorithms/bearloc.RADLab.wifi.db"
db_path = "/root/workspace/BearLoc/python/bearloc/algorithms/bearloc.db"
db = None 

clf_infos = {}  # classifiers {(semloc, semantic): {"clf": clf, ...}}

geoloc_train_epoch_thld = 60 * 60 * 1000  # in ms, 1 hour
geoloc_predict_epoch_thld = 60 * 60 * 1000  # in ms, 1 hour
wifi_minrssi = -150
wifi_train_epoch_thld = 2000  # ms
wifi_predict_epoch_thld = 5000  # ms


# semantics list should be compatible with semantic tree in loc.py
# hardcoded here
sems = ("country", "state", "city", "street", "building", "floor", "room")

training_thread = None
lock = threading.RLock()
training_done = False

def localize_impl(data):
    """Execute localization service, which fuses results of multiple
    localization services.

    Return {semloc: {semantic: loc} dict, alter: {semantic: {alternative location: confidence}}, sem: tree of semantic}
    """
    location = algorithm_capnp.Algorithm.Location.new_message()

    #print data

    lock.acquire()
    if training_done:
        data_json = json.loads(data)
        location.country = "US"
        location.state = "CA"
        location.city = "Berkeley"
        location.street = "Leroy Ave"
        location.building = 'Soda Hall'
        # location.locale = str(random.randrange(10000))
        semloc = {"country":"US","state":"CA","city":"Berkeley","street":"Leroy Ave","building":"Soda Hall","floor":"Floor 4"}
        alter = {}
        predict(data_json, (semloc, alter), "room")  # so geoloc won't be called
        location.locale = semloc["room"]
    else:
        location.country = "Unknown"
        location.state = "Unknown"
        location.city = "Unknown"
        location.street = "Unknown"
        location.building = 'Unknown'
        # location.locale = str(random.randrange(10000))
        location.locale = "Unknown"
    lock.release()

    #print location
    return location


# def aggr(semloc, alter):
#     sem = self._sem()
#     semlocinfo = {"semloc": semloc, "alter": alter, "sem": sem}
#     return semlocinfo
#
#
# def sem(self):
#     # Kepp semantic tree linear, it is too much hassles to deal with different branches
#     # hardcoded here
#     sem = tree()
#     sem["country"]["state"]["city"]["street"]["building"]["floor"]["room"]
#     return sem
#
#
# def tree(self):
#     """Autovivification of tree.
#     ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
#     """
#     return defaultdict(tree)


def predict(data, (semloc, alter), targetsem):
    clf_info = clf_infos.get((frozenset(semloc.items()), targetsem))
    if clf_info != None:
        if targetsem not in ("floor", "room"):
            (loc, alter_locs) = predict_geoloc(
                uuid, locepoch, clf_info)
        else:
            (loc, alter_locs) = predict_wifi(
                data, clf_info)

        if loc != None and alter_locs != None:
            semloc[targetsem] = loc
            alter[targetsem] = alter_locs
            return True

    return False


def predict_geoloc(uuid, locepoch, clf_info):
    print("Shouldn't reach here!")
#     clf = clf_info["clf"]
#
#     cur = db.cursor()
#     # extract attributes and store in db
#     operation = "SELECT longitude, latitude FROM " + "geoloc" + \
#                 " WHERE uuid='" + str(uuid) + "'" + \
#                 " AND ABS(epoch-" + str(locepoch) + ") <= " + \
#                 str(geoloc_predict_epoch_thld) + \
#                 " ORDER BY ABS(epoch-" + str(locepoch) + ")" + \
#                 " LIMIT 1"
#     cur.execute(operation)
#     geoloc = cur.fetchall()
#
#     if len(geoloc) == 0:
#         return (None, None)
#
#     data = np.array(geoloc)
#
#     locations = clf.predict(data)
#     count = collections.Counter(locations)
#     location = count.most_common(1)[0][0]
#     alter = {loc: float(cnt) / len(locations)
#              for loc, cnt in count.items()}
#     return (location, alter)


def predict_wifi(data, clf_info):
    if len(data) == 0:
        return (None, None)

    bssids = clf_info["bssids"]
    clf = clf_info["clf"]

    # sigs: {BSSID: RSSI}
    # TODO use itertools gourpby
    sigs = [{d["BSSID"]:d["RSSI"] for d in data}]
    X = [[sig.get(bssid, wifi_minrssi)
             for bssid in bssids] for sig in sigs]
    X = np.array(X)

    locations = clf.predict(X)
    count = collections.Counter(locations)
    location = count.most_common(1)[0][0]
    alter = {loc: float(cnt) / len(locations) for loc, cnt in count.items()}
    return (location, alter)


def train_all():
    """Train model for all semantic."""
    global training_done
    global db

    db = sqlite3.connect(database=db_path) 

    # TODO online/incremental training
    print("Started Loc training")

    condsemlocs = [{}]
    for sem in sems:
        condsemlocs = train(condsemlocs, sem)
        if len(condsemlocs) == 0:
            break

    print("Stopped Loc training")
    lock.acquire()
    training_done = True
    lock.release()


def train(condsemlocs, targetsem):
    """Build model for sem under conditional sems and semlocs.
    return new conditional semlocs for lower level."""
    global clf_infos

    cur = db.cursor()
    operation = "SELECT DISTINCT uuid FROM device;"
    cur.execute(operation)
    uuids = cur.fetchall()

    condsems = sems[0:sems.index(targetsem)]

    new_condsemlocs = []

    for condsemloc in condsemlocs:
        if not all(condsem in condsemloc for condsem in condsems):
            continue

        uuidlocations = {}
        for uuid, in uuids:
            cur = db.cursor()
            # extract locations stored in db
            operation = "SELECT DISTINCT epoch, " + targetsem + " FROM " + "semloc" + \
                        " WHERE uuid='" + uuid + "'"
            conds = [
                condsem + "='" + condsemloc[condsem] + "'" for condsem in condsems]
            if conds:
                operation += " AND " + " AND ".join(conds)
            operation += " AND " + targetsem + " IS NOT NULL;"
            cur.execute(operation)
            locations = cur.fetchall()
            uuidlocations[uuid] = locations

        clf = None
        if targetsem not in ("floor", "room"):
            (clf, locations) = train_geoloc(uuidlocations)
            if clf != None:
                clf_infos[
                    (frozenset(condsemloc.items()), targetsem)] = {"clf": clf}
        else:
            (clf, locations, bssids) = train_wifi(uuidlocations)
            if clf != None:
                clf_infos[(frozenset(condsemloc.items()), targetsem)] = {
                    "clf": clf, "bssids": bssids}

        if clf != None:
            for location in set(locations):
                new_condsemloc = condsemloc.copy()
                new_condsemloc[targetsem] = location
                new_condsemlocs.append(new_condsemloc)

    return new_condsemlocs

  # TODO make this defer.inlineCallbacks


def train_geoloc(uuidlocations):
    cur = db.cursor()

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
            low_idx = bisect_idx - 1 if bisect_idx > 0 else 0
            (epochdiff, cls) = min(
                [(abs(epoch - location[0]), location[1])
                 for location in locations[low_idx:bisect_idx + 1]],
                key=getepoch_f)

            if epochdiff <= geoloc_train_epoch_thld:
                data.append((lon, lat))
                classes.append(cls)

    if len(data) == 0 or len(classes) == 0:
        return (None, None)

    data = np.array(data)
    classes = np.array(classes)

    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes)


def train_wifi(uuidlocations):
    cur = db.cursor()

    uuidwifis = {}
    for uuid in uuidlocations.keys():
            # extract attributes and store in db
        operation = "SELECT DISTINCT epoch, BSSID, RSSI FROM wifi" + \
                    " WHERE uuid='" + uuid + "'"
        cur.execute(operation)
        wifis = cur.fetchall()
        uuidwifis[uuid] = wifis

    sigs = []  # wifi signatures
    classes = []  # locations corresponding to sigs
    bssids = []  # BSSIDs of wifi data
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
        wifisigs = [(epoch, {bssid: rssi for (epoch, bssid, rssi) in group})
                    for epoch, group in itertools.groupby(wifis, key=getepoch_f)]

        locepochs = [location[0] for location in locations]
        for epoch, sig in wifisigs:
            bisect_idx = bisect.bisect_left(locepochs, epoch)
            low_idx = bisect_idx - 1 if bisect_idx > 0 else 0
            (epochdiff, cls) = min(
                [(abs(epoch - location[0]), location[1])
                 for location in locations[low_idx:bisect_idx + 1]],
                key=getepoch_f)

        if epochdiff <= wifi_train_epoch_thld:
            sigs.append(sig)
            classes.append(cls)
            bssids.extend(sig.keys())

    if len(sigs) == 0 or len(classes) == 0:
        return (None, None, None)

    bssids = tuple(set(bssids))
    data = [[sig.get(bssid, wifi_minrssi)
             for bssid in bssids] for sig in sigs]
    data = np.array(data)
    classes = np.array(classes)

    clf = tree.DecisionTreeClassifier().fit(data, classes)

    return (clf, classes, bssids)


# def localize_impl(data):
#     print data
#     location = algorithm_capnp.Algorithm.Location.new_message()
#     location.country = "US"
#     location.state = "CA"
#     location.city = "Berkeley"
#     location.street = "Leroy Ave"
#     location.building = 'Soda Hall'
#     location.locale = str(random.randrange(10000))
#     print location
#     return location


class DummyAlgorithm(algorithm_capnp.Algorithm.Server):

    def localize(self, data, **kwargs):
        return localize_impl(data)


def restore(ref):
    assert ref.as_text() == 'algorithm'
    return DummyAlgorithm()


def main():
    global training_thread
    address = sys.argv[1]
    training_thread = threading.Thread(target=train_all)
    training_thread.start()
    server = capnp.TwoPartyServer(address, restore)
    server.run_forever()

if __name__ == '__main__':
    main()
