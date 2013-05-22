#!/usr/bin/env python
# encoding: utf-8
"""
loc.py

localization.

Created by Kaifei Chen on 2013-02-23.
Copyright (c) 2013 UC Berkeley. All rights reserved.
"""


from twisted.application import service

from boss.loc import wifiloc


class Loc(object):
  """Loc class"""
  
  def __init__(self, db, content = ['wifi']):
    self._db = db
    self._content = content
    
    if 'wifi' in self._content:
      self._wifiloc = wifiloc.WifiLoc(self._db, dbupdate_interval = 3600)
  
  
  def localize(self, request):
    """Execute localization service, which fuses results of multiple 
    localization services.
  
    Return tuple (building id, (x, y, z), confidence)"""
    # TODO use DeferredList after adding new loc service
    wifiloc_request = request['data']['wifi']
    d = self._wifiloc.localize(wifiloc_request)
    return d