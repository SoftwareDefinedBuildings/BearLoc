#!/usr/bin/env python

# A dummy localization algorithm, the server of Cap'n Proto

import sys
import socket
import capnp

import algorithm_capnp # read algorithm.capnp

import random

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
        return localize_impl()


def restore(ref):
    assert ref.as_text() == 'algorithm'
    return DummyAlgorithm()


def main():
    address = sys.argv[1]
    server = capnp.TwoPartyServer(address, restore)
    server.run_forever()

if __name__ == '__main__':
    main()
