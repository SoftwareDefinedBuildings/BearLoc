#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IControl(Interface):
  """Interface of BOSS Control service"""

  def control(request):
    """
    Return a deferred returning a control status.
    """