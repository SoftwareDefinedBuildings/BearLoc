#!/usr/bin/env python
# encoding: utf-8

from twisted.web import static
from twisted.internet import defer
from twisted.python import filepath

from zope.interface import implements

from boss.metadata.interface import IMetadata

import simplejson as json
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
    self._transform_points(metadata, target[-1])
    
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
      
  
  def _transform_points(self, metadata, targetsem):
    inverse_mtx = self._inverse(metadata['views']['floorplan']['mtx'])
    metadata['views']['floorplan'].pop('mtx', None)
    mdchildlist = metadata['child'][targetsem]
    for key in mdchildlist.keys():
      for region in mdchildlist[key]['regions']:
        for point in region:
          self._apply_transform_to_point(inverse_mtx, point)
  
  
  # copied from BAS codes /web/smapgeo/inkscape/geoutil.py
  def _inverse(self, ((m00, m01, m02), (m10, m11, m12))):
    m20 = 0
    m21 = 0
    m22 = 1
    det = m00*m11*m22 + m01*m12*m20 + m02*m10*m21 - m00*m12*m21 - m01*m10*m22 - m02*m11*m20;
    return [[float(m11*m22 - m12*m21)/det,
             float(m02*m21 - m01*m22)/det,
             float(m01*m12 - m02*m11)/det],
            [float(m12*m20 - m10*m22)/det,
             float(m00*m22 - m02*m20)/det,
             float(m02*m10 - m00*m12)/det
             ]]


  # copied from BAS codes /web/smapgeo/inkscape/simpletransform.py
  def _apply_transform_to_point(self, mat, pt):
     x = mat[0][0]*pt[0] + mat[0][1]*pt[1] + mat[0][2]
     y = mat[1][0]*pt[0] + mat[1][1]*pt[1] + mat[1][2]
     pt[0]=x
     pt[1]=y