#!/usr/bin/env python
# encoding: utf-8

from twisted.web import static
from twisted.internet import defer
from twisted.python import filepath

from zope.interface import implements

from boss.metadata.interface import IMetadata

import json
import os

class Metadata(object):
  """Metadata class"""
  
  implements(IMetadata)
  
  def __init__(self, db):
    self._db = db
    self._root = os.path.abspath(os.path.dirname(__file__) + "/data/")
  
  def metadata(self, request):
    """Execute metadata service.
  
    Return metadata"""
    f = open(os.path.abspath(self._root + "/metadata.json"))
    mdtree = json.loads(f.read())
    # target format: [[Sem1, Zone1], ..., [SemN, ZoneN], TargetSem]
    target = request['target']
    
    metadata = self._metadata_recursive(mdtree, target)
    
    print metadata
    
    return defer.succeed(metadata)


  def _metadata_recursive(self, mdtree, target):
    """Get metadata of location for specific semantic array, 
       including children metadata in target semantic.
       The metadata may contain elements whose content is stored in other files.
  
    Return dict"""
    
    semantic = target[0][0]
    zone = target[0][1]
    
    if mdtree['semantic'] == semantic and mdtree['name'] == zone:
      if isinstance(target[1], basestring):
        targetsem = target[1]
        mdtree['child'] = {targetsem: mdtree['child'][targetsem]}
        mdchildlist = mdtree['child'][targetsem]
        for key in mdchildlist.keys():
          if 'refer' in mdchildlist[key].keys():
            f = open(os.path.abspath(self._root + mdchildlist[key]['refer']))
            mdchildlist[key] = json.loads(f.read())
          mdchildlist[key].pop('child', None)
        
        return mdtree
      else:
        childsemantic = target[1][0]
        childzone = target[1][1]
        childmdtree = mdtree['child'][childsemantic][childzone]
        if 'refer' in childmdtree.keys():
          f = open(os.path.abspath(self._root + childmdtree['refer']))
          childmdtree = json.loads(f.read())
        return self._metadata_recursive(childmdtree, target[1:])
    else:
      return None