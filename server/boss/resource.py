#!/usr/bin/env python
# encoding: utf-8

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
    if 'map' in self._service.content():
      self.putChild('map', MapResource(self._service.map))
    if 'control' in self._service.content():
      self.putChild('control', ControlResource(self._service.control))
  
  
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
    return  self.__doc__ + ": POST JSON to me!"
  
  
  def render_POST(self, request):
    """POST localization request"""
    log.msg("Received localization request from " + request.getHost().host)
    
    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._localize(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail)
    
    # cancel localize deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)
    
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
      request.write("Server error: No Localize module")
      request.finish()
      log.msg(request.getHost().host + " localization failed")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")


class MapResource(resource.Resource):
  """BOSS Map web-accessible resource"""

  def __init__(self, map):
    resource.Resource.__init__(self)
    self._map = map


  def getChild(self, path, request):
    if name == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)


  def render_GET(self, request):
    return self.__doc__ + ": POST JSON to me!"


  def render_POST(self, request):
    """POST map request"""
    log.msg("Received map request from " + request.getHost().host)

    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._map(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail)

    # cancel map query deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)

    return server.NOT_DONE_YET


  def _succeed(self, map):
    request.setResponseCode(httplib.OK)
    request.write(map)
    request.finish()
    log.msg(request.getHost().host + " map returned")


  def _fail(self, err):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " map query canceled")
    elif err.check(boss_service.NoMapError):
      request.setResponseCode(httplib.BAD_REQUEST)
      request.write("Server error: No Map module")
      request.finish()
      log.msg(request.getHost().host + " map query failed")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")


class ControlResource(resource.Resource):
  """BOSS Control web-accessible resource"""

  def __init__(self, control):
    resource.Resource.__init__(self)
    self._control = control


  def getChild(self, path, request):
    if name == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)


  def render_GET(self, request):
    return self.__doc__ + ": POST JSON to me!"


  def render_POST(self, request):
    """POST control request"""
    log.msg("Received control request from " + request.getHost().host)

    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._control(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail)

    # cancel control deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)

    return server.NOT_DONE_YET


  def _succeed(self, map):
    request.setResponseCode(httplib.OK)
    request.write(map)
    request.finish()
    log.msg(request.getHost().host + " map returned")


  def _fail(self, err):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " control canceled")
    elif err.check(boss_service.NoControlError):
      request.setResponseCode(httplib.BAD_REQUEST)
      request.write("Server error: No Control module")
      request.finish()
      log.msg(request.getHost().host + " map query failed")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")