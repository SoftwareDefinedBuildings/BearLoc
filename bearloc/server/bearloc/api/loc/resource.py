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

from .interface import ILoc

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer
from zope.interface import implementer
import simplejson as json
import httplib


@implementer(resource.IResource)
class LocResource(resource.Resource):
    """BearLoc Localize web-accessible resource"""

    def __init__(self, loc):
        resource.Resource.__init__(self)
        self._loc = loc


    def getChild(self, path, request):
        if path == '':
            return self
        else:
            return resource.Resource.getChild(self, path, request)


    def render_GET(self, request):
        return  self.__doc__


    def render_POST(self, request):
        """POST localization request"""
        log.msg("Received localization request from " + request.getHost().host)

        request.setHeader('Content-type', 'application/json')
        try:
            content = json.load(request.content)
        except:
            # TODO: handle bad request
            return ""

        d = self._loc.localize(content)
        d.addCallback(self._succeed, request)
        d.addErrback(self._fail, request)

        # cancel localize deferred if the connection is lost before it fires
        request.notifyFinish().addErrback(self._cancel, d, request)

        return server.NOT_DONE_YET


    def _succeed(self, semlocinfo, request):
        request.setResponseCode(httplib.OK)
        request.write(json.dumps(semlocinfo))
        request.finish()
        log.msg(request.getHost().host + " is localized")


    def _fail(self, err, request):
        if err.check(defer.CancelledError):
            log.msg(request.getHost().host + " localization canceled")
        else:
            pass


    def _cancel(self, err, deferred, request):
        deferred.cancel()
        log.msg(request.getHost().host + " lost connection")


components.registerAdapter(LocResource,
                           ILoc,
                           resource.IResource)
