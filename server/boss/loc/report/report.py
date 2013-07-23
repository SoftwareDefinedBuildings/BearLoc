#!/usr/bin/env python
# encoding: utf-8

from twisted.internet import defer, reactor

from zope.interface import implements

from boss.loc.report.interface import IReport
from boss.loc.android import AndroidSensorType, AndroidAudioFormat, AndroidAudioEncoding

import os
import glob
import shutil
import simplejson as json
import wave
import array


class Report(object):
  """Localization Report class"""
  
  implements(IReport)
  
  def __init__(self, db):
    self._db = db
    self._sensors_table = 'sensors'
    self._locations_table = 'locations'
    self._audio_dir = 'audio/'
  
  def report(self, report):
    """Store reported location and corresponding data.
    """
    
    # cap the size of report log files
    if os.path.exists('report.log') == True:
      statinfo = os.stat('report.log')
      if statinfo.st_size > 5*1024*1024:
        filenamelist = glob.glob('report.log.*')
        nolist = [int(filename.split(".")[2]) for filename in filenamelist]
        nolist.sort(reverse=True)
        for no in nolist:
          shutil.move('report.log.' + str(no), 'report.log.' + str(no+1))
        shutil.move('report.log', 'report.log.1')
    
    logfile = open('report.log', 'a')
    logfile.write(json.dumps(report) + '\n');
    logfile.close()
    
    reactor.callLater(0, self._insert, report)
    
    response = {'result': True}
    return defer.succeed(response)
  
  
  @defer.inlineCallbacks
  def _insert(self, report):
    """Insert the report to database"""
    
    operation = "CREATE TABLE IF NOT EXISTS " + self._sensors_table + \
                " (name TEXT NOT NULL PRIMARY KEY UNIQUE, \
                  vendor TEXT, \
                  type TEXT, \
                  version INTEGER, \
                  sampleRate REAL, \
                  power INTEGER, \
                  minDelay INTEGER, \
                  maxDelay INTEGER, \
                  resolution REAL, \
                  format TEXT, \
                  source TEXT, \
                  channel TEXT)"
    yield self._db.runQuery(operation)
  
    self._insert_sensor_data(report)
    self._insert_location(report)


  @defer.inlineCallbacks
  def _insert_sensor_data(self, report):
    sensordatadict = report['sensor data']
    for sensorname, sensordata in sensordatadict.iteritems():
      # insert sensor info item in sensor table
      sensorinfo = [sensordata['name'], 
                    sensordata.get('vendor'), 
                    str(sensordata.get('type')),
                    sensordata.get('version'),
                    sensordata.get('sample rate'),
                    sensordata.get('power'), 
                    sensordata.get('minimum deday'),
                    sensordata.get('maximum deday'),
                    sensordata.get('resolution'),
                    sensordata.get('format'),
                    sensordata.get('source'),
                    sensordata.get('channel')]
      # TODO log if it encounters conflicts
      operation = "INSERT OR IGNORE INTO " + self._sensors_table + \
                  " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
      yield self._db.runQuery(operation, sensorinfo)
      
      
      # insert sensor events to relted data table or files
      table = sensordata['name'].replace(" ", "_")
      if AndroidSensorType.contains(sensordata.get('type')):
        # create sensor events table
        operation = "CREATE TABLE IF NOT EXISTS " + table + \
                    " (timestamp NOT NULL PRIMARY KEY, \
                      value TEXT, \
                      accuracy REAL)"
        yield self._db.runQuery(operation)
        
        # insert event data
        for event in sensordata.get('events'):
          eventdata = (event['timestamp'],
                       str(event.get('values')),
                       event.get('accuracy'))
          operation = "INSERT OR IGNORE INTO " + table + \
                      " VALUES (?,?,?)"
          yield self._db.runQuery(operation, eventdata)
      elif sensordata.get('type') == 'location':
        # create sensor events table
        operation = "CREATE TABLE IF NOT EXISTS " + table + \
                    " (timestamp NOT NULL PRIMARY KEY, \
                      provider TEXT, \
                      bearing REAL, \
                      altitude REAL, \
                      longtitude REAL, \
                      latitude REAL, \
                      speed REAL, \
                      accuracy REAL)"
        yield self._db.runQuery(operation)

        # insert event data
        for event in sensordata.get('events'):
          eventdata = (event['timestamp'],
                       event.get('provider'),
                       event.get('bearing'),
                       event.get('altitude'),
                       event.get('longtitude'),
                       event.get('latitude'),
                       event.get('speed'),
                       event.get('accuracy'))
          operation = "INSERT OR IGNORE INTO " + table + \
                      " VALUES (?,?,?,?,?,?,?,?)"
          yield self._db.runQuery(operation, eventdata)
      elif sensordata.get('type') == 'wifi':
        # create sensor events table
        operation = "CREATE TABLE IF NOT EXISTS " + table + \
                    " (timestamp NOT NULL PRIMARY KEY, \
                      SSID TEXT, \
                      BSSID TEXT, \
                      level REAL, \
                      capabilities TEXT, \
                      frequency REAL)"
        yield self._db.runQuery(operation)

        # insert event data
        for event in sensordata.get('events'):
          eventdata = (event['timestamp'],
                       event.get('SSID'),
                       event.get('BSSID'),
                       event.get('level'),
                       event.get('capabilities'),
                       event.get('frequency'))
          operation = "INSERT OR IGNORE INTO " + table + \
                      " VALUES (?,?,?,?,?,?)"
          yield self._db.runQuery(operation, eventdata)
      elif sensordata.get('type') == 'audio':
        # create sensor events table
        operation = "CREATE TABLE IF NOT EXISTS " + table + \
                    " (timestamp NOT NULL PRIMARY KEY, \
                      path TEXT)"
        yield self._db.runQuery(operation)

        # generate .wav audio file
        eventdict = {event['timestamp']: event.get('values', []) for event in sensordata.get('events', [])}
        data = [byte for timestamp in sorted(eventdict.iterkeys()) for byte in eventdict[timestamp]]
        
        if not os.path.exists(self._audio_dir):
          os.makedirs(self._audio_dir)
        wavfpath = self._audio_dir + str(event['timestamp']) + '.wav'
        wavf = wave.open(wavfpath, 'wb')
        channel_num = AndroidAudioFormat.channel_num(sensordata.get('channel'))
        wavf.setnchannels(channel_num)
        sample_width = AndroidAudioEncoding.sample_width(sensordata.get('format'))
        wavf.setsampwidth(sample_width)
        wavf.setframerate(sensordata.get('sample rate'))
        wavf.setnframes(len(data)/sample_width)
        
        bindatastr = array.array('b', data).tostring()
        wavf.writeframesraw(bindatastr)
        
        # insert .wav audio file info
        eventdata = (sorted(eventdict.iterkeys())[-1],
                     wavfpath)
        operation = "INSERT OR IGNORE INTO " + table + \
                    " VALUES (?,?)"
        yield self._db.runQuery(operation, eventdata)


  @defer.inlineCallbacks
  def _insert_location(self, report):
    # create location reports table
    operation = "CREATE TABLE IF NOT EXISTS " + self._locations_table + \
                " (timestamp NOT NULL PRIMARY KEY, \
                  location TEXT)"
    yield self._db.runQuery(operation)
    
    locdata = (report['timestamp'],
               str(report.get('location')))
    operation = "INSERT OR IGNORE INTO " + self._locations_table + \
                " VALUES (?,?)"
    yield self._db.runQuery(operation, locdata)