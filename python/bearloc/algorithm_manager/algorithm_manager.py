#!/usr/bin/env python

# Algorithm Manager, Client of Cap'n Proto

import socket
import capnp

import algorithm_capnp # read algorithm.capnp


def print_location(location):
    print location

def main():
    address = 'localhost:60000'
    client = capnp.TwoPartyClient(address)
    algorithm = client.ez_restore('algorithm').cast_as(algorithm_capnp.Algorithm)
    location_promise = algorithm.localize()
    location_promise.then(print_location).wait()

if __name__ == '__main__':
    main()
