#!/usr/bin/env python
# encoding: utf-8

from twisted.application import service
from twisted.enterprise import adbapi
from twisted.internet import defer

from zope.interface import implements

from bearloc.interface import IBearLocService
from bearloc.loc.loc import Loc
from bearloc.metadata.metadata import Metadata
from bearloc.report.report import Report


class BearLocService(service.Service):
  """BearLoc service"""
  
  implements(IBearLocService)
  
  def __init__(self, db, content):
    self._db = adbapi.ConnectionPool('sqlite3', database = db, check_same_thread=False)
    self._content = content
    
    if 'localize' in self._content:
      self.loc = Loc(self._db)
    if 'metadata' in self._content:
      self.metadata = Metadata(self._db)
    if 'report' in self._content:
      self.report = Report(self._db)


  def content(self):
    return self._content
