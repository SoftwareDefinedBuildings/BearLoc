"""
Copyright (c) 2013, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions 
are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
"""
"""
@author 
"""

from buildsense.report.interface import IReport

from twisted.internet import defer, reactor
from zope.interface import implements
import os
import glob
import shutil
import simplejson as json
import wave
import array

class Report(object):
  """Report class"""

  implements(IReport)

  def __init__(self, db):
    self._db = db
    self._create_tables()


  def report(self, report):
    """Store reported location and temperature data.
    """
    if "semloc" in report and len(report["semloc"]) > 0:
      if "note" in report and len(report["note"]) >0:
        return defer.fail(Exception("Old Version"))

    reactor.callLater(0, self._insert, report)

    response = {'result': True}
    return defer.succeed(response)


  def _create_tables(self):
    """Blocking call of creating loc and temp table"""
    #combination of semloc and temp
    operation = "CREATE TABLE IF NOT EXISTS " + "temp_semloc" + \
                " (uuid TEXT NOT NULL, \
                  epoch INTEGER NOT NULL, \
                  country TEXT, \
                  state TEXT, \
                  city TEXT, \
                  street TEXT, \
                  building TEXT, \
                  floor TEXT, \
                  room TEXT, \
                  tem TEXT, \
                  PRIMARY KEY (uuid, epoch));"
    cur = self._db.cursor()
    cur.executescript(operation)

    self._db.commit()


  def _insert(self, report):
    """Insert the report to database"""
    self._insert_temp_semloc(report) if "semloc" and "note" in report else None


  def _insert_temp_semloc(self, report):
    cur = self._db.cursor()
    events = report.get("semloc") # list of semloc events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("country", None),
              event.get("state", None),
              event.get("city", None),
              event.get("street", None),
              event.get("building", None),
              event.get("floor", None),
              event.get("room", None),
              report.get("note")) # temperature or others 
  
      operation = "INSERT OR REPLACE INTO " + "temp_semloc" + \
                " VALUES (?,?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
