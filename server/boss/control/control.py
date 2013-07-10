#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer

from zope.interface import implements

from boss.control.interface import IControl


class Control(object):
  """Control class"""
  
  implements(IControl)
  
  def __init__(self, db):
    self._db = db
  
  
  def control(self, request):
    """Execute control service.
  
    Return control status"""
    hard_failure = Exception()
    
    return defer.succeed(hard_failure)