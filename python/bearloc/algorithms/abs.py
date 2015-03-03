#!/usr/bin/env python

# ABS localization algorithm, the server of Cap'n Proto


import sys
import socket
import wave
import atexit
import os
import random
import ast

import matlab.engine
import capnp

import algorithm_capnp # read algorithm.capnp

def convert2wave(raw, filename, channel, sampwidth, framerate, nframes):
    wavf = wave.open(filename, 'wb')
    wavf.setnchannels(channel)
    wavf.setsampwidth(sampwidth)
    wavf.setframerate(framerate)
    wavf.setnframes(nframes)
    wavf.writeframesraw(raw)
    wavf.close()

def localize_impl():
    location = algorithm_capnp.Algorithm.Location.new_message()
    location.country = "US"
    location.state = "CA"
    location.city = "Berkeley"
    location.street = "Leroy Ave"
    location.building = 'Soda Hall'
    location.locale = str(random.randrange(10000))
    return location


class DummyAlgorithm(algorithm_capnp.Algorithm.Server):
    def localize(self, data, **kwargs):
        data_dict = ast.literal_eval(data)
        print(type(data))
        print(type(data["raw"]))
        print(type(data["raw"][0]))
        convert2wave(data[raw], "temp.wav"，1, 2, 44100, len(data))
        rv = eng.ABS_Localize('temp.wav')
        os.remove("temp.wav")
        return rv

    def shutdown()
        eng.quit()


def restore(ref):
    assert ref.as_text() == 'algorithm'
    return DummyAlgorithm()


def main():
    address = sys.argv[1]
    global eng = matlab.engine.start_matlab()
    server = capnp.TwoPartyServer(address, restore)
    server.run_forever()

@atexit.register
def shutdown():
    print("shutting down algorithm engine")
    eng.quit()

if __name__ == '__main__':
    main()
