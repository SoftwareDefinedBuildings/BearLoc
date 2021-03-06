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

from bearloc.interface import IBearLocService
from bearloc.report.resource import ReportResource
from bearloc.loc.resource import LocResource
from bearloc.meta.resource import MetaResource

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer
from zope.interface import implementer
import simplejson as json
import httplib


@implementer(resource.IResource)
class BearLocResource(resource.Resource):
  """BearLoc web-accessible resource"""
  
  def __init__(self, service):
    resource.Resource.__init__(self)
    self._service = service
    if 'report' in self._service.content():
      self.putChild('report', ReportResource(self._service.report))
    if 'localize' in self._service.content():
      self.putChild('localize', LocResource(self._service.loc))
    if 'meta' in self._service.content():
      self.putChild('meta', MetaResource(self._service.meta))
  
  
  def getChild(self, path, request):
    if path == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)
  
  
  def render_GET(self, request):
    request.setHeader('Content-type', 'application/json')
    return json.dumps(self._service.content())


components.registerAdapter(BearLocResource, 
                           IBearLocService, 
                           resource.IResource)
