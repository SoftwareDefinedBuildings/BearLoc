#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer, reactor

from zope.interface import implements

from boss.loc.interface import ILoc
from boss.loc.report import report

from collections import defaultdict


class Loc(object):
  """Loc class"""
  
  implements(ILoc)
  
  def __init__(self, db):
    self._db = db
    self.report = report.Report(self._db)
    self._estimators = self._tree()
    reactor.callLater(0, self._train)
    self._train_interval = 20
    self._report_num = 0
    
    # TODO put the table names in settings file
    self._sensors_table = 'sensors'
    self._locations_table = 'locations'
    self._mldata_table = 'mldata'


  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return tuple 
    ((country, state, city, district, street, building, 
    floor, ((semantic, zone), ...)), confidence)"""
    # TODO use DeferredList after adding new loc service
    loc1 = self._tree()
    loc1['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["district", "University of California Berkeley"]']['["building", "Sutardja Dai Hall"]']['["floor", "Floor 4"]']['["thermostat", "Thermostat 2"]']
    loc1['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["district", "University of California Berkeley"]']['["building", "Sutardja Dai Hall"]']['["floor", "Floor 4"]']['["hvac", "VAV 4"]']
    loc1['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["district", "University of California Berkeley"]']['["building", "Sutardja Dai Hall"]']['["floor", "Floor 4"]']['["lighting", "Light 1"]']
    loc1['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["street", "Hearst Ave"]']
    loc_info1 = {'location': loc1, 'confidence': 0}
    
    loc2 = self._tree()
    loc2['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["district", "University of California Berkeley"]']['["building", "Sutardja Dai Hall"]']['["floor", "Floor 6"]']['["lighting", "Light 1"]']
    loc2['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["street", "Hearst Ave"]']
    loc_info2 = {'location': loc2, 'confidence': 0}
    
    loc3 = self._tree()
    loc3['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["district", "University of California Berkeley"]']['["building", "Sutardja Dai Hall"]']['["floor", "Floor 7"]']['["lighting", "Light 2"]']
    loc3['["planet", "Earth"]']['["country", "US"]']['["state", "CA"]']['["city", "Berkeley"]']['["street", "Hearst Ave"]']
    loc_info3 = {'location': loc3, 'confidence': 0}
    
    loc_infos = [loc_info1, loc_info2, loc_info3]
    
    from random import randint
    hard_loc_info = loc_infos[0] #loc_infos[randint(0,2)]
    
    return defer.succeed(hard_loc_info)


  def _tree(self):
    """Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    """
    return defaultdict(self._tree)


  @defer.inlineCallbacks
  def _train(self):
    """Train a SVM model for all data."""
    # TODO online/incremental training
    
    operation = "CREATE TABLE IF NOT EXISTS " + self._mldata_table + \
                " (timestamp INTEGER NOT NULL PRIMARY KEY, \
                  wifi_rssi TEXT, \
                  abs TEXT, \
                  longtitude REAL, \
                  latitude REAL,\
                  location TEXT)"
    yield self._db.runQuery(operation)
    
    # check if locations table exists
    operation = "SELECT * FROM sqlite_master WHERE name ='" + \
                self._locations_table + "' and type='table'";
    d = self._db.runQuery(operation)
    loctable = yield d
    
    if len(loctable) > 0:
      # extract attributes and store in db
      operation = "SELECT * FROM " + self._locations_table
      d = self._db.runQuery(operation)
      locations = yield d
    
      if len(locations) > self._report_num:
        self._report_num = len(locations)
        for timestamp, location in locations:
          
          wifi_rssi = self._get_wifi_rssi(timestamp, 10000)
    
      # TODO retrain model if there is enough data
      self._estimators['TODO']
    
      # Only schedule next training task when this one finishes
      reactor.callLater(self._train_interval, self._train)


  @defer.inlineCallbacks
  def _get_wifi_rssi(self, timestamp, history_len):
    """get wifi RSSIs """
    # check if sensors table exists
    operation = "SELECT * FROM sqlite_master WHERE name ='" + \
                self._sensors_table + "' and type='table'";
    d = self._db.runQuery(operation)
    sensros_table = yield d
    
    if len(_sensors_table) > 0:
      typename = ('wifi',)
      operation = "SELECT name FROM " + self._sensors_table + " WHERE type=?"
      d = self._db.runQuery(operation, typename)
      name = yield d
      
      table = name.replace(" ", "_")
      operation = "SELECT MAX(timestamp), BSSID, RSSI FROM " + table + \
                  " WHERE " + str(timestamp) + " - timestamp > " + \
                  str(history_len) + " ORDER by timestamp"
      d = self._db.runQuery(operation, typename)
      wifievents = yield d
      
      wifi_rssi_dict = {event[1]: event[2] for event in wifievents}
    else:
      defer.returnValue(None)
    
    