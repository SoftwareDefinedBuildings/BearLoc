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
@author Beidi Chen <beidichen1993@berkeley.edu>
@author Kaifei Chen <kaifei@eecs.berkeley.edu>
"""

from buildsense.report.interface import IReport

from twisted.internet import defer, reactor
from zope.interface import implementer
import os
import glob
import shutil
import simplejson as json
import wave
import array


@implementer(IReport)
class Report(object):
  """Report class"""

  def __init__(self, db):
    self._db = db
    self._create_tables()


  def report(self, report):
    """Store reported location and temperature data.
    """
    reactor.callLater(0, self._insert, report)

    response = {'result': True}
    return defer.succeed(response)


  def fetch(self):
    """Fetch reported location and temperature data.
    """
    return self._get()


  def _create_tables(self):
    """Blocking call of creating loc and notes table"""
    #combination of semloc and temp
    operation = "CREATE TABLE IF NOT EXISTS " + "notes" + \
                " (uuid TEXT NOT NULL, \
                  epoch INTEGER NOT NULL, \
                  country TEXT, \
                  state TEXT, \
                  city TEXT, \
                  street TEXT, \
                  building TEXT, \
                  floor TEXT, \
                  room TEXT, \
                  note TEXT, \
                  PRIMARY KEY (uuid, epoch));"
    cur = self._db.cursor()
    cur.execute(operation)

    self._db.commit()


  def _insert(self, report):
    """Insert the report to database"""
    self._insert_note(report) if "report" in report else None


  def _insert_note(self, report):
    cur = self._db.cursor()

    events = report.get("report")
    for event in events:
      semloc = event.get("semloc")
      data = (event.get("uuid"),
              event.get("epoch"),
              semloc.get("country", None),
              semloc.get("state", None),
              semloc.get("city", None),
              semloc.get("street", None),
              semloc.get("building", None),
              semloc.get("floor", None),
              semloc.get("room", None),
              event.get("note", None)) # temperature or others 
  
      operation = "INSERT OR REPLACE INTO " + "notes" + \
                  " VALUES (?,?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _get(self):
    cur = self._db.cursor()

    operation = "SELECT * FROM " + "notes;"
    cur.execute(operation)
    
    result = cur.fetchall()
    string = ["<p>" + "[" + str(map(str, elem)[0]) + "]" + "/".join(map(str, elem)[1:-1]) + ":" + str(map(str, elem)[-1]) + "</p>" for elem in result]
    
    return "<html>" + "\n".join(string) + "</html>"
