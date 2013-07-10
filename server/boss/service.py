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
      self.loc = loc.Loc(self._db)
    if 'metadata' in self._content:
      self.metadata = metadata.Metadata(self._db)
    if 'control' in self._content:
      self.control = control.Control(self._db)


  def content(self):
    return self._content