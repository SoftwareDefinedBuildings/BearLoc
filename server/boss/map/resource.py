#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server
from twisted.python import log
from twisted.internet import defer

from boss import service as boss_service

import json
import httplib


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