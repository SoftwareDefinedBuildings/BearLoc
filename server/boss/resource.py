#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer

from boss.interface import IBOSSService
from boss.loc import resource as loc_resource
from boss.metadata import resource as metadata_resource
from boss.control import resource as control_resource

import json
import httplib


class BOSSResource(resource.Resource):
  """BOSS web-accessible resource"""
  
  def __init__(self, service):
    resource.Resource.__init__(self)
    self._service = service
    if 'localize' in self._service.content():
      self.putChild('localize', loc_resource.LocResource(self._service.loc))
    if 'metadata' in self._service.content():
      self.putChild('metadata', metadata_resource.MetadataResource(self._service.metadata))
    if 'control' in self._service.content():
      self.putChild('control', control_resource.ControlResource(self._service.control))
  
  
  def getChild(self, path, request):
    if path == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)
  
  
  def render_GET(self, request):
    request.setHeader('Content-type', 'application/json')
    return json.dumps(self._service.content())

components.registerAdapter(BOSSResource, 
                           IBOSSService, 
                           resource.IResource)