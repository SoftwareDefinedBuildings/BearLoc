#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IReport(Interface):
  """Interface of BOSS Localization Report service"""

  def report(report):
    """
    Return a deferred returning a boolean.
    """