#!/usr/bin/env python
# encoding: utf-8
"""
resource.py

BOSS http site resource.

Created by Kaifei Chen on 2013-03-15.
Copyright (c) 2013 UC Berkeley. All rights reserved.
"""


from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer

from boss import interface as boss_iface
from boss import service as boss_service

import json
import httplib


class BOSSResource(resource.Resource):
  """BOSS web-accessible resource"""
  
  def __init__(self, service):
    resource.Resource.__init__(self)
    self._service = service
    if 'localize' in self._service.content():
      self.putChild('localize', LocResource(self._service.localize))
  
  
  def getChild(self, path, request):
    if path == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)
  
  
  def render_GET(self, request):
    request.setHeader('Content-type', 'application/json')
    return json.dumps(self._service.content())

components.registerAdapter(BOSSResource, 
                           boss_iface.IBOSSService, 
                           resource.IResource)


class LocResource(resource.Resource):
  """BOSS Localize web-accessible resource"""
  
  def __init__(self, localize):
    resource.Resource.__init__(self)
    self._localize = localize
  
  
  def getChild(self, path, request):
    if name == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)
  
  
  def render_GET(self, request):
    return "POST JSON to me!"
  
  
  def render_POST(self, request):
    """POST localization request"""
    log.msg("Received request from " + request.getHost().host)
    
    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._localize(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail)
    
    # cancel localize deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel_localize, d, request)
    
    return server.NOT_DONE_YET
  
  
  def _succeed(self, (loc, confidence), request):
    request.setResponseCode(httplib.OK)
    request.write(json.dumps((loc, confidence)))
    request.finish()
    log.msg(request.getHost().host + " is localizaed as " + str(loc) 
            + " with confidence " + str(confidence))


  def _fail(self, err):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " localization canceled")
    elif err.check(boss_service.NoLocalizerError):
      request.setResponseCode(httplib.BAD_REQUEST)
      request.write("Server error: No Localizer")
      request.finish()
      log.msg(request.getHost().host + " localization failed")
    else:
      pass


  def _cancel_localize(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")