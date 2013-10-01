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
                " (uuid TEXT NOT NULL PRIMARY KEY, \
                  make TEXT, \
                  model TEXT);"
    
    # meta
    operation += "CREATE TABLE IF NOT EXISTS " + "meta" + \
                 " (uuid TEXT NOT NULL, \
                    sensor TEXT NOT NULL, \
                    vendor TEXT, \
                    name TEXT, \
                    power INTEGER, \
                    minDelay INTEGER, \
                    maxRange INTEGER, \
                    version INTEGER, \
                    resolution INTEGER, \
                    PRIMARY KEY (uuid, sensor));"

    # wifi
    operation += "CREATE TABLE IF NOT EXISTS " + "wifi" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    BSSID TEXT NOT NULL, \
                    SSID TEST, \
                    RSSI REAL NOT NULL, \
                    capability TEXT, \
                    freq REAL, \
                    PRIMARY KEY (uuid, epoch, BSSID));"

    # audio
    operation += "CREATE TABLE IF NOT EXISTS " + "audio" + \
                 " (uuid TEXT NOT NULL, \
                   epoch INTEGER NOT NULL, \
                   channel INTEGER NOT NULL, \
                   sampwidth INTEGER NOT NULL, \
                   framerate INTEGER NOT NULL, \
                   nframes INTEGER NOT NULL, \
                   path TEXT NOT NULL, \
                   PRIMARY KEY (uuid, epoch));"
    
    # geoloc
    operation += "CREATE TABLE IF NOT EXISTS " + "geoloc" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    longtitude REAL NOT NULL, \
                    latitude REAL NOT NULL, \
                    altitude REAL, \
                    bearing REAL, \
                    speed REAL, \
                    accuracy REAL, \
                    provider TEXT, \
                    PRIMARY KEY (uuid, epoch));"

    # linear acc 
    operation += "CREATE TABLE IF NOT EXISTS " + "lacc" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"

    # acc                                                                                                                       
    operation += "CREATE TABLE IF NOT EXISTS " + "acc" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # temp                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "temp" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    temp REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"

    # rotation                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "rotation" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"

    # gravity                                                                                                                   
    operation += "CREATE TABLE IF NOT EXISTS " + "gravity" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # gyro                                                                                                                      
    operation += "CREATE TABLE IF NOT EXISTS " + "gyro" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # light                                                                                                                     
    operation += "CREATE TABLE IF NOT EXISTS " + "light" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    light REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # magnetic                                                                                                                  
    operation += "CREATE TABLE IF NOT EXISTS " + "magnetic" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    x REAL NOT NULL, \
                    y REAL NOT NULL, \
                    z REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # pressure                                                                                                                  
    operation += "CREATE TABLE IF NOT EXISTS " + "pressure" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    pressure REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # proximity                                                                                                                 
    operation += "CREATE TABLE IF NOT EXISTS " + "proximity" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    proximity REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
    
    # humidity
    operation += "CREATE TABLE IF NOT EXISTS " + "humidity" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    humidity REAL NOT NULL, \
                    accuracy REAL, \
                    PRIMARY KEY (uuid, epoch));"
   
    # semloc
    operation += "CREATE TABLE IF NOT EXISTS " + "semloc" + \
                 " (uuid TEXT NOT NULL, \
                    epoch INTEGER NOT NULL, \
                    semantic TEXT NOT NULL, \
                    location TEXT NOT NULL, \
                    PRIMARY KEY (uuid, epoch, semantic));"
  
    cur = self._db.cursor()    
    cur.executescript(operation)
   
    self._db.commit()


  def _insert(self, report):
    """Insert the report to database"""
    self._insert_device(report) if "device" in report else None
    self._insert_meta(report) if "meta" in report else None
    self._insert_wifi(report) if "wifi" in report else None
    self._insert_audio(report) if "audio" in report else None
    self._insert_bluetooth(report) if "bluetooth" in report else None
    self._insert_geoloc(report) if "geoloc" in report else None
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
    data = (device.get("uuid"),
            device.get("make", None),
            device.get("model", None))
    
    operation = "INSERT OR REPLACE INTO " + "device" + \
                " VALUES (?,?,?);"
    cur = self._db.cursor()
    cur.execute(operation, data)

    self._db.commit()

 
  def _insert_meta(self, report):
    cur = self._db.cursor()
    meta = report.get("meta") # dict of meta, which is also dict
    for sensortype, sensormeta in meta.iteritems():
      data = (report.get("device").get("uuid"),
              sensortype,
              meta.get("vendor", None),
              meta.get("name", None),
              meta.get("power", None),
              meta.get("minDelay", None),
              meta.get("maxRange",None ),
              meta.get("version", None),
              meta.get("resolution", None))
  
      operation = "INSERT OR REPLACE INTO " + "meta" + \
                " VALUES (?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_wifi(self, report):
    cur = self._db.cursor()
    events = report.get("wifi") # list of wifi events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("BSSID"),
              event.get("SSID", None),
              event.get("RSSI"),
              event.get("capability", None),
              event.get("freq", None))
  
      operation = "INSERT OR REPLACE INTO " + "wifi" + \
                " VALUES (?,?,?,?,?,?,?);"
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
      data = (report.get("device").get("uuid"),
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
    pass

  def _insert_geoloc(self, report):
    cur = self._db.cursor()
    events = report.get("geoloc") # list of geoloc events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("longtitude"),
              event.get("latitude"),
              event.get("altitude", None),
              event.get("bearing", None),
              event.get("speed", None),
              event.get("accuracy", None),
              event.get("provider", None))
  
      operation = "INSERT OR REPLACE INTO " + "geoloc" + \
                  " VALUES (?,?,?,?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
  
  def _insert_lacc(self, report):
    cur = self._db.cursor()
    events = report.get("lacc") # list of linear acc events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "lacc" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_acc(self, report):
    cur = self._db.cursor()
    events = report.get("acc") # list of acc events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "acc" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_temp(self, report):
    cur = self._db.cursor()
    events = report.get("temp") # list of temp events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "temp" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_rotation(self, report):
    cur = self._db.cursor()
    events = report.get("rotation") # list of rotation events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "rotation" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_gravity(self, report):
    cur = self._db.cursor()
    events = report.get("gravity") # list of gravity events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "gravity" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_gyro(self, report):
    cur = self._db.cursor()
    events = report.get("gyro") # list of gyro events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "gyro" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_light(self, report):
    cur = self._db.cursor()
    events = report.get("light") # list of light events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "light" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_magnetic(self, report):
    cur = self._db.cursor()
    events = report.get("magnetic") # list of magnetic events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("values")[1],
              event.get("values")[2],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "magnetic" + \
                " VALUES (?,?,?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_pressure(self, report):
    cur = self._db.cursor()
    events = report.get("pressure") # list of pressure events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "pressure" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_proximity(self, report):
    cur = self._db.cursor()
    events = report.get("proximity") # list of proximity events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "proximity" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()

  
  def _insert_humidity(self, report):
    cur = self._db.cursor()
    events = report.get("humidity") # list of humidity events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("values")[0],
              event.get("accuracy", None))
  
      operation = "INSERT OR REPLACE INTO " + "humidity" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()


  def _insert_semloc(self, report):
    cur = self._db.cursor()
    events = report.get("semloc") # list of semloc events
    for event in events:
      data = (report.get("device").get("uuid"),
              event.get("epoch"),
              event.get("semantic"),
              event.get("location"))
  
      operation = "INSERT OR REPLACE INTO " + "semloc" + \
                " VALUES (?,?,?,?);"
      cur.execute(operation, data)

    self._db.commit()
