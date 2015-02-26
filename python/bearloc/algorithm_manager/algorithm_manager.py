#!/usr/bin/env python

# Algorithm Manager, Client of Cap'n Proto

import socket
import capnp

import algorithm_capnp # read algorithm.capnp

import paho.mqtt.client as mqtt
import simplejson as json

algorithm_topic = "bearloc/algorithm/dummy"
algorithm_map = {} # topic to algorithm isntance mapping
# TODO add heartbeat suppport

fake_loc = {"msgtype":"locResult", "uuid":"somerandomnumberhaha", "epoch": 142500000, "result": {"country":"US", "state":"CA", "city":"Berkeley", "street":"Leroy Ave", "building":"Soda Hall", "locale":"492"}}

# The callback for when the client receives a CONNACK response from the broker.
def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(algorithm_topic)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    if msg.topic != algorithm_topic:
        return
        
    data = json.loads(str(msg.payload))
    print(msg.topic+" "+str(data))
    back_topic = data["backtopic"]
    fake_loc_str = json.dumps(fake_loc)
    client.publish(back_topic, payload=fake_loc_str, qos=1, retain=True)
    #localize_promise = algorithm.localize()
    #localize_promise.then(print_location).wait()

def print_location(localize_response):
    location = localize_response.location.to_dict()
    print location

def init_mqtt():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect("bearloc.cal-sdb.org", 52411, 60)
    return client

def init_capnp(address):
    client = capnp.TwoPartyClient(address)
    algorithm = client.ez_restore('algorithm').cast_as(algorithm_capnp.Algorithm)
    return algorithm

def main():
    client = init_mqtt()
    client.loop_forever()
    

if __name__ == '__main__':
    main()
