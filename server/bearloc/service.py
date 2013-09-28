#!/usr/bin/env python
# encoding: utf-8

from bearloc.interface import IBearLocService
from bearloc.loc.loc import Loc
from bearloc.report.report import Report

from twisted.application import service
from twisted.internet import defer
from zope.interface import implements
import sqlite3

class BearLocService(service.Service):
  """BearLoc service"""
  
  implements(IBearLocService)
  
  def __init__(self, db, content):
    self._db = sqlite3.connect(database = db)  # No need to use aync DB for now
    self._content = content
    
    if 'report' in self._content:
      self.report = Report(self._db) # Report.__init__() create all data tables
    if 'localize' in self._content:
      self.loc = Loc(self._db)


  def content(self):
    return self._content
