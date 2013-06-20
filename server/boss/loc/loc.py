#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer


class Loc(object):
  """Loc class"""
  
  def __init__(self, db):
    self._db = db
  
  
  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return tuple 
    ((country, state, city, district, street, building, 
    floor, ((semantic, zone), ...)), confidence)"""
    # TODO use DeferredList after adding new loc service
    hard_loc = (('US', 'CA', 'Berkeley', 'University of California Berkeley', 
    'Hearst Ave', 'Sutardja Dai Hall', 'Floor 4', 
    (('Room', '410'), ('HVAC', '1'), ('Light', '2'))), 0)
    
    return defer.succeed(hard_loc)