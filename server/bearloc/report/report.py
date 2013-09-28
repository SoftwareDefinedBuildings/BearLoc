#!/usr/bin/env python
# encoding: utf-8

from bearloc.report.interface import IReport

from twisted.internet import defer, reactor
from zope.interface import implements
import os
import glob
import shutil
import simplejson as json
import wave
import array


class Report(object):
  """Report class"""
  
  implements(IReport)
  
  def __init__(self, db):
    self._db = db
    self._create_tables()
 

  def report(self, report):
    """Store reported location and corresponding data.
    """
    reactor.callLater(0, self._insert, report)
    
    response = {'result': True}
    return defer.succeed(response)
  
  
  def _create_tables(self):
    """Blocking call of creating tables"""
    # device
    operation = "CREATE TABLE IF NOT EXISTS " + "device" + \
                " (imei TEXT NOT NULL PRIMARY KEY, \
                  make TEXT, \
                  model TEXT);"
    
    # sensormeta
    operation += "CREATE TABLE IF NOT EXISTS " + "sensormeta" + \
                 " (imei TEXT NOT NULL, \
                    sensor TEXT, \
                    vendor TEXT, \
                    name TEXT, \
                    power INTEGER, \
                    minDelay INTEGER, \
                    maxDelay INTEGER, \
                    version INTEGER, \
                    resolution INTEGER, \
                    PRIMARY KEY (imei, sensor));"

    # wifi
    operation += "CREATE TABLE IF NOT EXISTS " + "wifi" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    BSSID TEXT NOT NULL, \
                    SSID TEST, \
                    RSSI REAL NOT NULL, \
                    freq REAL, \
                    PRIMARY KEY (imei, epoch, BSSID));"

    # audio
    operation += "CREATE TABLE IF NOT EXISTS " + "audio" + \
                 " (imei TEXT NOT NULL, \
                   epoch INTEGER NOT NULL, \
                   channel INTEGER, \
                   sampwidth INTEGER, \
                   framerate INTEGER, \
                   nframes INTEGER, \
                   path TEXT NOT NULL, \
                   PRIMARY KEY (imei, epoch));"
    
    # geoloc
    operation += "CREATE TABLE IF NOT EXISTS " + "geoloc" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    longtitude REAL NOT NULL, \
                    latitude REAL NOT NULL, \
                    altitude REAL, \
                    bearing REAL, \
                    speed REAL, \
                    accuracy REAL, \
                    provider TEXT, \
                    PRIMARY KEY (imei, epoch));"

    # linear acc 
    operation += "CREATE TABLE IF NOT EXISTS " + "lacc" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"

    # acc                                                                                                                       
    operation += "CREATE TABLE IF NOT EXISTS " + "acc" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # temp                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "temp" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    temp REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"

    # rotation                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "rotation" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"

    # gravity                                                                                                                   
    operation += "CREATE TABLE IF NOT EXISTS " + "gravity" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # gyro                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "gyro" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # light                                                                                                                     
    operation += "CREATE TABLE IF NOT EXISTS " + "light" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    light REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # magnetic                                                                                                                  
    operation += "CREATE TABLE IF NOT EXISTS " + "magnetic" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # pressure                                                                                                                  
    operation += "CREATE TABLE IF NOT EXISTS " + "pressure" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    pressure REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # proximity                                                                                                                 
    operation += "CREATE TABLE IF NOT EXISTS " + "proximity" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    proximity REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
    
    # humidity
    operation += "CREATE TABLE IF NOT EXISTS " + "humidity" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    humidity REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (imei, epoch));"
   
    # semloc
    operation += "CREATE TABLE IF NOT EXISTS " + "semloc" + \
                 " (imei TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    semantic TEXT NOT NULL, \
                    location TEXT NOT NULL, \
                    PRIMARY KEY (imei, epoch));"
  
    cur = self._db.cursor()    
    cur.executescript(operation)
   
    self._db.commit()


  def _insert(self, report):
    """Insert the report to database"""
    self._insert_device(report)
    self._insert_sensormeta(report) if "sensormeta" in report else None
    self._insert_wifi(report) if "wifi" in report else None
    self._insert_audio(report) if "audio" in report else None
    self._insert_geoloc(report) if "audio" in report else None
    self._insert_lacc(report) if "lacc" in report else None
    self._insert_acc(report) if "acc" in report else None
    self._insert_temp(report) if "temp" in report else None
    self._insert_rotation(report) if "rotation" in report else None
    self._insert_gravity(report) if "gravity" in report else None
    self._insert_gyro(report) if "gyro" in report else None
    self._insert_light(report) if "light" in report else None
    self._insert_magnetic(report) if "magnetic" in report else None
    self._insert_pressure(report) if "pressure" in report else None
    self._insert_proximity(report) if "proximity" in report else None
    self._insert_humidity(report) if "humidity" in report else None
    self._insert_semloc(report) if "semloc" in report else None


  def _insert_device(self, report):
    device = report.get("device") # dict of device info
    data = (device.get("imei"),
            device.get("make"),
            device.get("model"))
    
    operation = "INSERT OR REPLACE INTO " + "device" + \
                " VALUES (?,?,?);"
    cur = self._db.cursor()
    cur.execute(operation, data)

    self._db.commit()

 
  def _insert_sensormeta(self, report):
    cur = self._db.cursor()
    sensormeta = report.get("sensormeta") # dict of sensor meta, which is also dict
    for sensor, meta in sensormeta.iteritems():
      data = (report.get("device").get("imei"),
              sensor,
              meta.get("vendor"),
              meta.get("name"),
              meta.get("power"),
              meta.get("minDelay"),
              meta.get("maxDelay"),
              meta.get("version"),
              meta.get("resolution"))
  
      operation = "INSERT OR REPLACE INTO " + "sensormeta" + \
                " VALUES (?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_wifi(self, report):
    cur = self._db.cursor()
    events = report.get("wifi") # list of wifi events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("BSSID"),
              event.get("SSID"),
              event.get("RSSI"),
              event.get("freq"))
  
      operation = "INSERT OR REPLACE INTO " + "wifi" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
    

  def _insert_audio(self, report):
    audiodir = "audio/"
    if not os.path.exists(audiodir):
      os.makedirs(audiodir)
    
    cur = self._db.cursor()
    events = report.get("audio") # list of audio events
    for event in events:
      epoch = event.get("epoch")
      channel = event.get("channel")
      sampwidth = event.get("sampwidth")
      framerate = event.get("framerate")
      nframes = event.get("nframes")
      raw = array.array('b', event.get("raw")).tostring()
   
      # generate .wav audio file
      wavfpath = audiodir + str(epoch) + '.wav'
      with wave.open(wavfpath, 'wb') as wavef:
        wavf.setnchannels(channel)
        wavf.setsampwidth(sampwidth)
        wavf.setframerate(framerate)
        wavf.setnframes(nframes)
        wavf.writeframesraw(raw)
        
      # insert auido file info to db
      data = (report.get("device").get("imei"),
              epoch,
              channel,
              sampwidth,
              framerate,
              nframes,
              wavpath)
  
      operation = "INSERT OR REPLACE INTO " + "audio" + \
                  " VALUES (?,?,?,?,?,?,?);" 
      cur.execute(operation, data)

    self._db.commit()
  

  def _insert_geoloc(self, report):
    cur = self._db.cursor()
    events = report.get("geoloc") # list of geoloc events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("longtitude"),
              event.get("latitude"),
              event.get("altitude"),
              event.get("bearing"),
              event.get("speed"),
              event.get("accuracy"),
              event.get("provider"))
  
      operation = "INSERT OR REPLACE INTO " + "geoloc" + \
                  " VALUES (?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
  
  def _insert_lacc(self, report):
    cur = self._db.cursor()
    events = report.get("lacc") # list of linear acc events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "lacc" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_acc(self, report):
    cur = self._db.cursor()
    events = report.get("acc") # list of acc events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "acc" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_temp(self, report):
    cur = self._db.cursor()
    events = report.get("temp") # list of temp events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "temp" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_rotation(self, report):
    cur = self._db.cursor()
    events = report.get("rotation") # list of rotation events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "rotation" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_gravity(self, report):
    cur = self._db.cursor()
    events = report.get("gravity") # list of gravity events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "gravity" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_gyro(self, report):
    cur = self._db.cursor()
    events = report.get("gyro") # list of gyro events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "gyro" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_light(self, report):
    cur = self._db.cursor()
    events = report.get("light") # list of light events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "light" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_magnetic(self, magnetic):
    cur = self._db.cursor()
    events = report.get("magnetic") # list of magnetic events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "magnetic" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_pressure(self, pressure):
    cur = self._db.cursor()
    events = report.get("pressure") # list of pressure events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "pressure" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_proximity(self, proximity):
    cur = self._db.cursor()
    events = report.get("proximity") # list of proximity events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "proximity" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_humidity(self, humidity):
    cur = self._db.cursor()
    events = report.get("humidity") # list of humidity events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy"))
  
      operation = "INSERT OR REPLACE INTO " + "humidity" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_semloc(self, semloc):
    cur = self._db.cursor()
    events = report.get("semloc") # list of semloc events
    for event in events:
      data = (report.get("device").get("imei"),
              event.get("epoch"),
              event.get("semantic"),
              event.get("location"))
  
      operation = "INSERT OR REPLACE INTO " + "semloc" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
