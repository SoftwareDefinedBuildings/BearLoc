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
    loc1 = {'location': {'country': 'US', 'state': 'CA', 'city': 'Berkeley', 
    'district': 'University of California Berkeley', 'street': 'Hearst Ave', 
    'building': 'Sutardja Dai Hall', 'floor': 'Floor 4', 
    'thermostat': 'Thermostat 2', 'hvac': 'VAV 4', 'lighting': 'Light 1'}, 
    'confidence': 0}
    loc2 = {'location': {'country': 'US', 'state': 'CA', 'city': 'Berkeley', 
    'district': 'University of California Berkeley', 'street': 'Hearst Ave', 
    'building': 'Sutardja Dai Hall', 'floor': 'Floor 6', 
    'lighting': 'Light 1'}, 
    'confidence': 0}
    loc3 = {'location': {'country': 'US', 'state': 'CA', 'city': 'Berkeley', 
    'district': 'University of California Berkeley', 'street': 'Hearst Ave', 
    'building': 'Sutardja Dai Hall', 'floor': 'Floor 7', 
    'lighting': 'Light 2'}, 
    'confidence': 0}
    locs = [loc1, loc2, loc3]
    
    from random import randint
    hard_loc = locs[randint(0,2)]
    
    return defer.succeed(hard_loc)
    