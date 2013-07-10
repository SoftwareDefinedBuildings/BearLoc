#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IBOSSService(Interface):
  """Interface of BOSS service"""
  
  def content():
    """
    Return a list of strings.
    """