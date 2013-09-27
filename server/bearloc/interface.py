#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IBearLocService(Interface):
  """Interface of BearLoc service"""
  
  def content():
    """
    Return a list of strings.
    """
