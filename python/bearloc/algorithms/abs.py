#!/usr/bin/env python

# ABS localization algorithm, the server of Cap'n Proto


import sys
import socket
import wave
import atexit
import os
import random
import simplejson as json
import struct

import matlab.engine
import capnp

import algorithm_capnp # read algorithm.capnp

eng = None

def convert2wave(raw, filename, channel, sampwidth, framerate, nframes):
    wavf = wave.open(filename, 'wb')
    wavf.setnchannels(channel)
    wavf.setsampwidth(sampwidth)
    wavf.setframerate(framerate)
    wavf.setnframes(nframes)
    print "here!!!"
    wavf.writeframesraw(struct.pack(len(raw)*'i', *raw))
    print "done!!!"
    wavf.close()

def localize_impl(rv=None):
    location = algorithm_capnp.Algorithm.Location.new_message()
    location.country = "US"
    location.state = "CA"
    location.city = "Berkeley"
    location.street = "Leroy Ave"
    location.building = rv[0] if rv else 'Soda Hall'
    location.locale = rv[1] + "/" + rv[2] if rv else str(random.randrange(10000))
    return location


class DummyAlgorithm(algorithm_capnp.Algorithm.Server):
    def localize(self, data, **kwargs):
        global wavename
        data_dict = json.loads(data)
        print(type(data_dict))
        print(type(data_dict["raw"]))
        print(type(data_dict["raw"][0]))
        convert2wave(data_dict["raw"], "../algorithms/temp.wav", 1, 2, 44100, len(data))
        # eng.workspace['wavename'] = wavename
        eng.cd("../algorithms/")
        rv = eng.ABS_Localize("temp.wav")
        # rv, rv2, rv3 = eng.eval("ABS_Localize(wavename)",nargout=3)
        print(rv)
        print(type(rv))
        print(type(rv[0]))
        os.remove("../algorithms/temp.wav")
        return localize_impl(rv)

def restore(ref):
    assert ref.as_text() == 'algorithm'
    return DummyAlgorithm()


def main():
    global eng
    address = sys.argv[1]
    eng = matlab.engine.start_matlab()
    server = capnp.TwoPartyServer(address, restore)
    print("started")
    server.run_forever()

@atexit.register
def shutdown():
    print("shutting down algorithm engine")
    if eng:
        eng.quit()

if __name__ == '__main__':
    main()
