#!/usr/bin/env python
# encoding: utf-8

from twisted.application import service
from twisted.enterprise import adbapi
from twisted.internet import defer

from zope.interface import implements

from boss.interface import IBOSSService
from boss.loc import loc
from boss.map import map
from boss.control import control


class BOSSService(service.Service):
  """BOSS service"""
  
  implements(IBOSSService)
  
  def __init__(self, dbname = "boss.db", content = ['localize', 'map', 'control']):
    self._db = adbapi.ConnectionPool('sqlite3', database = dbname, 
                                     check_same_thread=False)
    self._content = content
    
    if 'localize' in self._content:
      self._loc = loc.Loc(self._db)
    if 'map' in self._content:
      self._map = map.Map(self._db)
    if 'control' in self._content:
      self._control = control.Control(self._db)


  def content(self):
    return self._content
  
  
  def localize(self, request):
    if 'localize' in self._content:
      d = self._loc.localize(request)
      return d
    else:
      return defer.fail(NoLocalizerError())
  
  
  def map(self, request):
    if 'map' in self._content:
      d = self._map.map(request)
      return d
    else:
      return defer.fail(NoMapError())


  def control(self, request):
    if 'control' in self._content:
      d = self._control.control(request)
      return d
    else:
      return defer.fail(NoControlError())


class NoLocalizerError(Exception):
  pass


class NoMapError(Exception):
  pass


class NoControlError(Exception):
  pass