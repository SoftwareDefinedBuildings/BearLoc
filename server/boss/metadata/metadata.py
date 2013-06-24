#!/usr/bin/env python
# encoding: utf-8

from twisted.web import static
from twisted.internet import defer
from twisted.python import filepath

import json
import os

class Metadata(object):
  """Metadata class"""
  
  def __init__(self, db):
    self._db = db
    self._root = os.path.abspath(os.path.dirname(__file__) + "/data/")
  
  def metadata(self, request):
    """Execute metadata service.
  
    Return metadata"""
    f = open(os.path.abspath(self._root + "/metadata.json"))
    mdtree = json.loads(f.read())
    location = request['location']
    targetsem = request['targetsem']
    
    mdtissue = self._metadata_tissue(mdtree, location, targetsem)
    metadata = self._metadata_expand(mdtissue)
    
    return defer.succeed(metadata)


  def _metadata_tissue(self, mdtree, loc, targetsem):
    """Get tissue of metadata of location for specific semantic.
       The tissue may contain elements whose content is stored in other files.
  
    Return dict"""
    
    if 'child' in mdtree:
      childsems = mdtree['child'].keys()
    else:
      childsems = []
    
    commomsems = [sem for sem in childsems if sem in loc.keys()]
    
    if commomsems: # commomsems is not empty
      if targetsem in commomsems:
        if loc[targetsem] in mdtree['child'][targetsem]:
          mdtissue = mdtree['child'][targetsem][loc[targetsem]]
        else:
          mdtissue = None
      else:
        mdtissue = None
        for sem in commomsems:
          if loc[sem] in mdtree['child'][sem]:
            mdsubtree = mdtree['child'][sem][loc[sem]]
          else:
            break
          
          if 'refer' in mdsubtree.keys():
            f = open(os.path.abspath(self._root + mdsubtree['refer']))
            mdsubtree = json.loads(f.read())
            
          tmp_tissue = self._metadata_tissue(mdsubtree, loc, targetsem)
          if tmp_tissue is not None:
            mdtissue = tmp_tissue
            break
    else:
      mdtissue = None
      
    return mdtissue


  def _metadata_expand(self, mdtissue):
    """Get full content of metadata tissue.
       The full content contains no data in other files.
  
    Return dict"""
    if 'refer' in mdtissue.keys():
      f = open(os.path.abspath(self._root + mdtissue['refer']))
      return json.loads(f.read())
    else:
      for key in mdtissue.keys():
        mdtissue[key] = self._metadata_expand(mdtissue[key])
      return mdtissue