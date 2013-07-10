#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class ILoc(Interface):
  """Interface of BOSS Localization service"""

  def localize(request):
    """
    Return a deferred returning a tuple.
    """