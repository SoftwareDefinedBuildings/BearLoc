#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer


class Control(object):
  """Control class"""
  
  def __init__(self, db):
    self._db = db
  
  
  def control(self, request):
    """Execute control service.
  
    Return control status"""
    hard_failure = Exception()
    
    return defer.succeed(hard_failure)