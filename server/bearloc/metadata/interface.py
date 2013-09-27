#!/usr/bin/env python
# encoding: utf-8

from zope.interface import Interface


class IMetadata(Interface):
  """Interface of BOSS Metadata service"""

  def metadata(request):
    """
    Return a deferred returning a metadata.
    """