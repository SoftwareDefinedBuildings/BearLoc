#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer


class Map(object):
  """Map class"""
  
  def __init__(self, db):
    self._db = db
  
  
  def map(self, request):
    """Execute map service.
  
    Return map"""
    # TODO use DeferredList after adding new loc service
    hard_map = null
    return defer.succeed(hard_map)