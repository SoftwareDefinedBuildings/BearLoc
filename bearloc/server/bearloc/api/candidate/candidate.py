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

from .interface import ICandidate

from twisted.internet import defer, reactor
from twisted.python import log
from zope.interface import implementer
from collections import defaultdict
import numpy as np
from sklearn import tree
from collections import Counter
import time


@implementer(ICandidate)
class Candidate(object):
    """Candidate class"""

    def __init__(self, db):
        self._db = db
        self._data = self._db.data

        # hardcoded
        self._sems = ("country", "state", "city", "street", "building", "locale")


    def get(self, query):
        """Handle candidate query, which is a list of locations

        Return deferred that returns [locations]
        """
        d = defer.Deferred()
        reactor.callLater(0, self._get, query, d)

        return d


    def _get(self, query, d):
        query_loc = zip(self._sems, query)
        if len(query_loc) == len(self._sems):
            # query is down to locale or longer
            candidate = []
        else:
            target_sem = self._sems[len(query_loc)]
            query = {sem: loc for sem, loc in query_loc}
            query['type'] = 'reported semloc'
            result = self._data.find(query)
            candidate = tuple(set([doc[target_sem] for doc in result if target_sem in doc]))

        d.callback(candidate)