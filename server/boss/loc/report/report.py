#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer

from zope.interface import implements

from boss.loc.report.interface import IReport


class Report(object):
  """Localization Report class"""
  
  implements(IReport)
  
  def __init__(self, db):
    self._db = db
  
  def report(self, report):
    """Store reported location and corresponding data.
    """
    logfile = open('reports.log', 'a')
    logfile.write(str(report));
    logfile.close()
    return defer.succeed(True)
    