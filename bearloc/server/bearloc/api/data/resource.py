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

from twisted.web import resource, server, http
from twisted.python import log, components
from twisted.internet import defer
from zope.interface import implementer
import simplejson as json


@implementer(resource.IResource)
class DataResource(resource.Resource):
    """BearLoc Data web-accessible resource"""

    def __init__(self, data):
        resource.Resource.__init__(self)
        self._data = data


    def getChild(self, path, request):
        if path == '':
            return self
        
        log.msg("Received data from " + request.getHost().host)
        query = [path,]
        return self._DataPage(query, self._data)


    def render_GET(self, request):
        return  self.__doc__


    class _DataPage(resource.Resource):

        def __init__(self, query, data):
            resource.Resource.__init__(self)
            self._query = query
            self._data = data


        def render_GET(self, request):
            return  self.__doc__


        def render_POST(self, request):
            """POST data"""

            request.setHeader('Content-type', 'application/json')
            try:
                content = json.load(request.content)
            except:
                # handle bad request
                self._client_error(request, http.BAD_REQUEST, "400 Bad Request")
                return server.NOT_DONE_YET

            d = self._data.add(self._query, content)
            d.addCallback(self._succeed, request)
            d.addErrback(self._fail, request)

            # cancel report deferred if the connection is lost before it fires
            request.notifyFinish().addErrback(self._cancel, d, request)

            return server.NOT_DONE_YET


        def _succeed(self, response, request):
            request.setResponseCode(http.OK)
            request.write(json.dumps(response))
            request.finish()
            log.msg(request.getHost().host + " reported")


        def _fail(self, err, request):
            if err.check(defer.CancelledError):
                log.msg(request.getHost().host + " report canceled")
            else:
                self._client_error(request, http.BAD_REQUEST, "400 Bad Request")


        def _cancel(self, err, deferred, request):
            deferred.cancel()
            log.msg(request.getHost().host + " lost connection")


        def _client_error(self, request, status, content, mimetype='text/plain'):
            request.setResponseCode(status)
            request.setHeader("Content-Type", mimetype)
            request.setHeader("Content-Length", str(len(content)))
            request.write(content)
            request.finish()


components.registerAdapter(DataResource,
                           IData,
                           resource.IResource)
