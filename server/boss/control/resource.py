#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer

from boss.control.interface import IControl

import simplejson as json
import httplib


class ControlResource(resource.Resource):
  """BOSS Control web-accessible resource"""

  def __init__(self, control):
    resource.Resource.__init__(self)
    self._control = control


  def getChild(self, path, request):
    if path == '':
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
    d = self._control.control(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail)

    # cancel control deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)

    return server.NOT_DONE_YET


  def _succeed(self, state, request):
    request.setResponseCode(httplib.OK)
    request.write(state)
    request.finish()
    log.msg(request.getHost().host + " map returned")


  def _fail(self, err, request):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " control canceled")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")

components.registerAdapter(ControlResource, 
                           IControl, 
                           resource.IResource)