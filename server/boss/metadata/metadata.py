#!/usr/bin/env python
# encoding: utf-8

from twisted.web import static
from twisted.internet import defer
from twisted.python import filepath

import os

class Metadata(object):
  """Metadata class"""
  
  def __init__(self, db):
    self._db = db
  
  def metadata(self, request):
    """Execute metadata service.
  
    Return metadata"""
    location = request['location']
    path = os.path.abspath(os.path.dirname(__file__) + '/data/' + '/'.join(location[0:-1]) + '/metadata.json')
    f = open(path)
    metadata = f.read()
    
    return defer.succeed(metadata)