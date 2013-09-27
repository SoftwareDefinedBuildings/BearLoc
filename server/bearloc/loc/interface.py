#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class ILoc(Interface):
  """Interface of BearLoc Localization service"""

  def localize(request):
    """
    Return a deferred returning a tuple.
    """
