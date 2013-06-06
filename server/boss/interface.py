#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IBOSSService(Interface):
  """Interface of BOSS service"""
  
  def content():
    """
    Return a list of strings.
    """
  
  
  def localize(request):
    """
    Return a deferred returning a tuple.
    """
  
  
  def map(request):
    """
    Return a deferred returning a map.
    """


  def control(request):
    """
    Return a deferred returning a control status.
    """