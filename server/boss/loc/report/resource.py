#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer

from boss.loc.report.interface import IReport

import json
import httplib


class ReportResource(resource.Resource):
  """BOSS Localize Report web-accessible resource"""
  
  def __init__(self, report):
    resource.Resource.__init__(self)
    self._report = report
  
  
  def getChild(self, path, request):
    if path == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)
  
  
  def render_GET(self, request):
    return  self.__doc__ + ": POST JSON to me!"
  
  
  def render_POST(self, request):
    """POST localization report"""
    log.msg("Received localization report from " + request.getHost().host)
    
    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._report.report(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail, request)
    
    # cancel localize deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)
    
    return server.NOT_DONE_YET
  
  
  def _succeed(self, state, request):
    request.setResponseCode(httplib.OK)
    request.write(state)
    request.finish()
    log.msg(request.getHost().host + " reported a location")


  def _fail(self, err, request):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " localization report canceled")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")

components.registerAdapter(ReportResource, 
                           IReport, 
                           resource.IResource)