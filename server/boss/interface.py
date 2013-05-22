#!/usr/bin/env python
# encoding: utf-8
"""
interface.py

Interface of BOSS service.

Created by Kaifei Chen on 2013-04-08.
Copyright (c) 2013 UC Berkeley. All rights reserved.
"""


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