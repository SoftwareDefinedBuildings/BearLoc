#!/usr/bin/env python
# encoding: utf-8
"""
service.py

BOSS service.

Created by Kaifei Chen on 2013-02-23.
Copyright (c) 2013 UC Berkeley. All rights reserved.
"""


from twisted.application import service
from twisted.enterprise import adbapi
from twisted.internet import defer

from zope.interface import implements

from boss.interface import IBOSSService
from boss.loc import loc


class BOSSService(service.Service):
  """BOSS service"""
  
  implements(IBOSSService)
  
  def __init__(self, dbname = "boss.db", content = ['localize']):
    # TODO: add other services
    self._db = adbapi.ConnectionPool('sqlite3', database = dbname, 
                                     check_same_thread=False)
    self._content = content
    
    if 'localize' in self._content:
      self._loc = loc.Loc(self._db)
  
  
  def content(self):
    return self._content
  
  
  def localize(self, request):
    # TODO: maintain an instance of _localizer for each user
    if 'localize' in self._content:
      d = self._loc.localize(request)
      return d
    else:
      return defer.fail(NoLocalizerError())


class NoLocalizerError(Exception):
  pass
  