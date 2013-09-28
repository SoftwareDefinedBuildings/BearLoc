#!/usr/bin/env python
# encoding: utf-8

from bearloc.interface import IBearLocService
from bearloc.loc.resource import LocResource
from bearloc.report.resource import ReportResource

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer
import simplejson as json
import httplib


class BearLocResource(resource.Resource):
  """BearLoc web-accessible resource"""
  
  def __init__(self, service):
    resource.Resource.__init__(self)
    self._service = service
    if 'report' in self._service.content():
      self.putChild('report', ReportResource(self._service.report))
    if 'localize' in self._service.content():
      self.putChild('localize', LocResource(self._service.loc))
  
  
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
