#!/usr/bin/env python
# encoding: utf-8

from twisted.web import resource, server, static
from twisted.python import log, components
from twisted.internet import defer

from boss.metadata.interface import IMetadata

import simplejson as json
import httplib
import os


class MetadataResource(resource.Resource):
  """BOSS Metadata web-accessible resource"""

  def __init__(self, metadata):
    resource.Resource.__init__(self)
    self._metadata = metadata
    
    datapath = os.path.dirname(__file__) + '/data/'
    self.putChild('data', static.File(datapath))


  def getChild(self, path, request):
    if path == '':
      return self
    else:
      return resource.Resource.getChild(self, path, request)


  def render_GET(self, request):
    return self.__doc__ + ": POST JSON to me!"


  def render_POST(self, request):
    """POST metadata request"""
    log.msg("Received metadata request from " + request.getHost().host)
    
    request.setHeader('Content-type', 'application/json')
    # TODO: handle bad request
    content = json.load(request.content)
    d = self._metadata.metadata(content)
    d.addCallback(self._succeed, request)
    d.addErrback(self._fail, request)
    
    # cancel metadata query deferred if the connection is lost before it fires
    request.notifyFinish().addErrback(self._cancel, d, request)
    
    return server.NOT_DONE_YET


  def _succeed(self, metadata, request):
    request.setResponseCode(httplib.OK)
    request.write(json.dumps(metadata))
    request.finish()
    log.msg(request.getHost().host + " metadata returned")


  def _fail(self, err, request):
    if err.check(defer.CancelledError):
      log.msg(request.getHost().host + " metadata query canceled")
    else:
      pass


  def _cancel(self, err, deferred, request):
    deferred.cancel()
    log.msg(request.getHost().host + " lost connection")

components.registerAdapter(MetadataResource, 
                           IMetadata, 
                           resource.IResource)