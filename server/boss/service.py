#!/usr/bin/env python
# encoding: utf-8

from twisted.application import service
from twisted.enterprise import adbapi
from twisted.internet import defer

from zope.interface import implements

from boss.interface import IBOSSService
from boss.loc import loc
from boss.metadata import metadata
from boss.control import control


class BOSSService(service.Service):
  """BOSS service"""
  
  implements(IBOSSService)
  
  def __init__(self, dbname = "boss.db", content = ['localize', 'metadata', 'control']):
    self._db = adbapi.ConnectionPool('sqlite3', database = dbname, check_same_thread=False)
    self._content = content
    
    if 'localize' in self._content:
      self._loc = loc.Loc(self._db)
    if 'metadata' in self._content:
      self._metadata = metadata.Metadata(self._db)
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
  
  
  def metadata(self, request):
    if 'metadata' in self._content:
      d = self._metadata.metadata(request)
      return d
    else:
      return defer.fail(NoMetadataError())


  def control(self, request):
    if 'control' in self._content:
      d = self._control.control(request)
      return d
    else:
      return defer.fail(NoControlError())


class NoLocalizerError(Exception):
  pass


class NoMetadataError(Exception):
  pass


class NoControlError(Exception):
  pass