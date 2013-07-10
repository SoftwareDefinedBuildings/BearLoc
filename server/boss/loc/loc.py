#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer

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
    hard_loc_info = loc_infos[randint(0,2)]
    
    return defer.succeed(hard_loc_info)


  def _tree(self):
    '''Autovivification of tree.
    ref http://recursive-labs.com/blog/2012/05/31/one-line-python-tree-explained/
    '''
    return defaultdict(self._tree)