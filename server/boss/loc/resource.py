#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server
from twisted.python import log, components
from twisted.internet import defer

from boss.loc.interface import ILoc
from boss.loc.report import resource as report_resource

import simplejson as json
import httplib


class LocResource(resource.Resource):
  """BOSS Localize web-accessible resource"""
  
  def __init__(self, loc):
    resource.Resource.__init__(self)
    self._loc = loc
    self.putChild('report', report_resource.ReportResource(self._loc.report))
  
  
  def getChild(self, path, request):
    if path == '':
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
    d = self._loc.localize(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail, request)
    
    # cancel localize deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)
    
    return server.NOT_DONE_YET
  
  
  def _succeed(self, locinfo, request):
    request.setResponseCode(httplib.OK)
    request.write(json.dumps(locinfo))
    request.finish()
    log.msg(request.getHost().host + " is localizaed as " + 
           json.dumps(locinfo['location']) + " with confidence " 
           + json.dumps(locinfo['confidence']))


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