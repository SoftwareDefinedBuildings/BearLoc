#!/usr/bin/env python

# Algorithm Manager, Client of Cap'n Proto

import socket
import capnp

import algorithm_capnp # read algorithm.capnp


def print_location(localize_response):
    location = localize_response.location.to_dict()
    print location

def main():
    address = 'localhost:60000'
    client = capnp.TwoPartyClient(address)
    algorithm = client.ez_restore('algorithm').cast_as(algorithm_capnp.Algorithm)
    localize_promise = algorithm.localize()
    localize_promise.then(print_location).wait()

if __name__ == '__main__':
    main()
