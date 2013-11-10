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
@author Kaifei Chen <kaifei@eecs.berkeley.edu>
"""

from bearloc.meta.interface import IMeta

from twisted.internet import defer, reactor
from twisted.python import log
from zope.interface import implementer
from collections import defaultdict
import numpy as np
from sklearn import tree
from collections import Counter
import time


@implementer(IMeta)
class Meta(object):
  """Meta class"""
  
  def __init__(self, db):
    self._db = db

    # semantics list should be compatible with semantic tree in loc.py
    # hardcoded here
    self._sems = ("country", "state", "city", "street", "building", "floor", "room")


  def meta(self, request):
    """Handle metadata request

    Return deferred that returns {semantic: [locations]} 
    """
    d = defer.Deferred()
    reactor.callLater(0, self._meta, request, d)

    return d


  def _meta(self, request, d):
    semloc = request.get("semloc")

    country = self._locs(semloc, "country")
    state = self._locs(semloc, "state")
    city = self._locs(semloc, "city")
    street = self._locs(semloc, "street")
    building = self._locs(semloc, "building")
    floor = self._locs(semloc, "floor")
    room = self._locs(semloc, "room")

    meta = {"country":country, "state":state, "city":city, "street":street, "building":building, \
            "floor":floor, "room":room}

    d.callback(meta)


  def _locs(self, semloc, targetsem):
    """Get list of locations under targetsem and semloc"""
    cur = self._db.cursor()
    
    condsems = self._sems[0:self._sems.index(targetsem)]
    # if semloc miss some consems, we return empty list
    if all([sem in semloc for sem in condsems]):
      operation = "SELECT DISTINCT " + targetsem + " FROM " + "semloc"
      conds = [sem+"='"+semloc[sem]+"'" for sem in condsems]
      if conds:
        operation += " WHERE " + " AND ".join(conds) + " AND " + targetsem + " IS NOT NULL"
      else:
        operation += " WHERE " + targetsem + " IS NOT NULL"
      cur.execute(operation)
      siblings = [x[0] for x in cur.fetchall()]

      return siblings
    else:
      return []