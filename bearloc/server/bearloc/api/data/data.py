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

from .interface import IData

from twisted.internet import defer, reactor
from zope.interface import implementer
import os
import glob
import shutil
import simplejson as json
import array
import sqlite3


@implementer(IData)
class Data(object):
    """Data class"""

    def __init__(self, db):
        self._db = db
        self._data = self._db.data


    def add(self, data):
        """Store reported location and corresponding data.
        """
        d = defer.Deferred()
        reactor.callLater(0, self._add, data, d)
        return d


    def _add(self, data, d):
        """Insert data to database"""
        if not isinstance(data, list):
            # TODO: more error info can be passed here
            d.errback(Exception())

        accept_cnt = 0
        for event in data:
            if isinstance(event, dict) \
               and 'type' in event and 'id' in event:
                self._data.insert(event)
                accept_cnt += 1

        response = {'reported': len(data), 'accepted': accept_cnt}
        d.callback(response)