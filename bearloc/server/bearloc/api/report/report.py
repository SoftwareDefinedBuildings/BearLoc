"""
Copyright (c) 2013, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
"""
"""
@author Kaifei Chen <kaifei@eecs.berkeley.edu>
"""

from .interface import IReport

from twisted.internet import defer, reactor
from zope.interface import implementer
import os
import glob
import shutil
import simplejson as json
import array
import sqlite3


@implementer(IReport)
class Report(object):
    """Report class"""

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

        # sensormeta
        operation += "CREATE TABLE IF NOT EXISTS " + "sensormeta" + \
                     " (uuid TEXT NOT NULL, \
                        sensor TEXT NOT NULL, \
                        vendor TEXT, \
                        name TEXT, \
                        power REAL, \
                        minDelay INTEGER, \
                        maxRange REAL, \
                        version INTEGER, \
                        resolution REAL, \
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
                       source TEXT, \
                       channel INTEGER NOT NULL, \
                       sampwidth INTEGER NOT NULL, \
                       framerate INTEGER NOT NULL, \
                       nframes INTEGER NOT NULL, \
                       raw BLOB NOT NULL, \
                       PRIMARY KEY (uuid, epoch));"

        # geoloc
        operation += "CREATE TABLE IF NOT EXISTS " + "geoloc" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        longitude REAL NOT NULL, \
                        latitude REAL NOT NULL, \
                        altitude REAL, \
                        bearing REAL, \
                        speed REAL, \
                        accuracy REAL, \
                        provider TEXT, \
                        PRIMARY KEY (uuid, epoch));"

        # acc
        operation += "CREATE TABLE IF NOT EXISTS " + "acc" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        x REAL NOT NULL, \
                        y REAL NOT NULL, \
                        z REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # linear acc
        operation += "CREATE TABLE IF NOT EXISTS " + "lacc" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        x REAL NOT NULL, \
                        y REAL NOT NULL, \
                        z REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # gravity
        operation += "CREATE TABLE IF NOT EXISTS " + "gravity" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        x REAL NOT NULL, \
                        y REAL NOT NULL, \
                        z REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # gyro
        operation += "CREATE TABLE IF NOT EXISTS " + "gyro" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        x REAL NOT NULL, \
                        y REAL NOT NULL, \
                        z REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # rotation
        operation += "CREATE TABLE IF NOT EXISTS " + "rotation" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        xr REAL NOT NULL, \
                        yr REAL NOT NULL, \
                        zr REAL NOT NULL, \
                        cos REAL, \
                        head_accuracy REAL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # magnetic
        operation += "CREATE TABLE IF NOT EXISTS " + "magnetic" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        x REAL NOT NULL, \
                        y REAL NOT NULL, \
                        z REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # light
        operation += "CREATE TABLE IF NOT EXISTS " + "light" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        light REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # temp
        operation += "CREATE TABLE IF NOT EXISTS " + "temp" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        temp REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # pressure
        operation += "CREATE TABLE IF NOT EXISTS " + "pressure" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        pressure REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # proximity
        operation += "CREATE TABLE IF NOT EXISTS " + "proximity" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        proximity REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # humidity
        operation += "CREATE TABLE IF NOT EXISTS " + "humidity" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        sysnano INTEGER NOT NULL, \
                        eventnano INTEGER NOT NULL, \
                        humidity REAL NOT NULL, \
                        accuracy REAL, \
                        PRIMARY KEY (uuid, epoch, sysnano, eventnano));"

        # semloc
        operation += "CREATE TABLE IF NOT EXISTS " + "semloc" + \
                     " (uuid TEXT NOT NULL, \
                        epoch INTEGER NOT NULL, \
                        country TEXT, \
                        state TEXT, \
                        city TEXT, \
                        street TEXT, \
                        building TEXT, \
                        floor TEXT, \
                        room TEXT, \
                        PRIMARY KEY (uuid, epoch));"

        cur = self._db.cursor()
        cur.executescript(operation)

        self._db.commit()


    def _insert(self, report):
        """Insert the report to database"""
        self._insert_device(report) if "device" in report else None
        self._insert_sensormeta(report) if "sensormeta" in report else None
        self._insert_wifi(report) if "wifi" in report else None
        self._insert_audio(report) if "audio" in report else None
        self._insert_bluetooth(report) if "bluetooth" in report else None
        self._insert_geoloc(report) if "geoloc" in report else None
        self._insert_acc(report) if "acc" in report else None
        self._insert_lacc(report) if "lacc" in report else None
        self._insert_gravity(report) if "gravity" in report else None
        self._insert_gyro(report) if "gyro" in report else None
        self._insert_rotation(report) if "rotation" in report else None
        self._insert_magnetic(report) if "magnetic" in report else None
        self._insert_light(report) if "light" in report else None
        self._insert_temp(report) if "temp" in report else None
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


    def _insert_sensormeta(self, report):
        cur = self._db.cursor()
        sensormeta = report.get("sensormeta") # dict of sensormeta, which is also dict
        for sensortype, meta in sensormeta.iteritems():
            data = (report.get("device").get("uuid"),
                    sensortype,
                    meta.get("vendor", None),
                    meta.get("name", None),
                    meta.get("power", None),
                    meta.get("minDelay", None),
                    meta.get("maxRange",None ),
                    meta.get("version", None),
                    meta.get("resolution", None))

            operation = "INSERT OR REPLACE INTO " + "sensormeta" + \
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
        cur = self._db.cursor()
        events = report.get("audio") # list of audio events
        for event in events:
            epoch = event.get("epoch")
            source = event.get("source", None)
            channel = event.get("channel")
            sampwidth = event.get("sampwidth")
            framerate = event.get("framerate")
            nframes = event.get("nframes")
            raw = array.array('b', event.get("raw")).tostring()

            # insert auido file info to db
            data = (report.get("device").get("uuid"),
                    epoch,
                    source,
                    channel,
                    sampwidth,
                    framerate,
                    nframes,
                    sqlite3.Binary(raw))

            operation = "INSERT OR REPLACE INTO " + "audio" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_geoloc(self, report):
        cur = self._db.cursor()
        events = report.get("geoloc") # list of geoloc events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("longitude"),
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

    def _insert_acc(self, report):
        cur = self._db.cursor()
        events = report.get("acc") # list of acc events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("x"),
                    event.get("y"),
                    event.get("z"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "acc" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_lacc(self, report):
        cur = self._db.cursor()
        events = report.get("lacc") # list of linear acc events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("x"),
                    event.get("y"),
                    event.get("z"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "lacc" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_gravity(self, report):
        cur = self._db.cursor()
        events = report.get("gravity") # list of gravity events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("x"),
                    event.get("y"),
                    event.get("z"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "gravity" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_gyro(self, report):
        cur = self._db.cursor()
        events = report.get("gyro") # list of gyro events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("x"),
                    event.get("y"),
                    event.get("z"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "gyro" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_rotation(self, report):
        cur = self._db.cursor()
        events = report.get("rotation") # list of rotation events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("xr"),
                    event.get("yr"),
                    event.get("zr"),
                    event.get("cos", None),
                    event.get("head_accuracy", None),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "rotation" + \
                        " VALUES (?,?,?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_magnetic(self, report):
        cur = self._db.cursor()
        events = report.get("magnetic") # list of magnetic events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("x"),
                    event.get("y"),
                    event.get("z"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "magnetic" + \
                        " VALUES (?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_light(self, report):
        cur = self._db.cursor()
        events = report.get("light") # list of light events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("light"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "light" + \
                        " VALUES (?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_temp(self, report):
        cur = self._db.cursor()
        events = report.get("temp") # list of temp events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("temp"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "temp" + \
                        " VALUES (?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_pressure(self, report):
        cur = self._db.cursor()
        events = report.get("pressure") # list of pressure events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("pressure"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "pressure" + \
                        " VALUES (?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_proximity(self, report):
        cur = self._db.cursor()
        events = report.get("proximity") # list of proximity events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("proximity"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "proximity" + \
                        " VALUES (?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_humidity(self, report):
        cur = self._db.cursor()
        events = report.get("humidity") # list of humidity events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("sysnano"),
                    event.get("eventnano"),
                    event.get("humidity"),
                    event.get("accuracy", None))

            operation = "INSERT OR REPLACE INTO " + "humidity" + \
                        " VALUES (?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()


    def _insert_semloc(self, report):
        cur = self._db.cursor()
        events = report.get("semloc") # list of semloc events
        for event in events:
            data = (report.get("device").get("uuid"),
                    event.get("epoch"),
                    event.get("country", None),
                    event.get("state", None),
                    event.get("city", None),
                    event.get("street", None),
                    event.get("building", None),
                    event.get("floor", None),
                    event.get("room", None))

            operation = "INSERT OR REPLACE INTO " + "semloc" + \
                        " VALUES (?,?,?,?,?,?,?,?,?);"
            cur.execute(operation, data)

        self._db.commit()
