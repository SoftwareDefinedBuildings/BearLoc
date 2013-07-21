#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer

from zope.interface import implements

from boss.loc.report.interface import IReport

import os
import glob
import shutil
import simplejson as json


class Report(object):
  """Localization Report class"""
  
  implements(IReport)
  
  def __init__(self, db):
    self._db = db
  
  def report(self, report):
    """Store reported location and corresponding data.
    """
    
    # cap the size of report log files
    if os.path.exists('report.log') == True:
      statinfo = os.stat('report.log')
      if statinfo.st_size > 5*1024*1024:
        filenamelist = glob.glob('report.log.*')
        nolist = [int(filename.split(".")[2]) for filename in filenamelist]
        nolist.sort(reverse=True)
        for no in nolist:
          shutil.move('report.log.' + str(no), 'report.log.' + str(no+1))
        shutil.move('report.log', 'report.log.1')
    
    logfile = open('report.log', 'a')
    logfile.write(json.dumps(report) + '\n');
    logfile.close()
    
    response = {'result': True}
    return defer.succeed(response)
    