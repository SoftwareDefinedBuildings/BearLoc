#!/usr/bin/env python

# Algorithm Manager, Client of Cap'n Proto

import socket
import capnp

import algorithm_capnp # read algorithm.capnp

import paho.mqtt.client as mqtt
import simplejson as json

algorithm_addr = 'localhost'
algorithm_port = 60000

mqtt_broker_addr = "bearloc.cal-sdb.org"
mqtt_broker_port = 52411

algorithm_topic = "bearloc/algorithm/dummy"
algorithm_map = {} # topic to algorithm isntance mapping
# TODO add heartbeat suppport

mqtt_client = None
capnp_client = None

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
    localize_promise = capnp_client.localize()
    publish_location_once = lambda response: publish_location(response, back_topic)
    localize_promise.then(publish_location_once).wait()

def publish_location(localize_response, back_topic):
    location = localize_response.location.to_dict()
    print location
    response = {"msgtype":"locResult", "uuid":"somerandomnumberhaha", "epoch": 142500000, "result": location}
    response_str = json.dumps(response)
    mqtt_client.publish(back_topic, payload=response_str, qos=1, retain=True)

def init_mqtt():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(mqtt_broker_addr, mqtt_broker_port, 60)
    return client

def init_capnp(address):
    client = capnp.TwoPartyClient(address)
    algorithm = client.ez_restore('algorithm').cast_as(algorithm_capnp.Algorithm)
    return algorithm

def main():
    global mqtt_client
    global capnp_client
    mqtt_client = init_mqtt()
    capnp_client = init_capnp(algorithm_addr+":"+str(algorithm_port))
    mqtt_client.loop_forever()
    

if __name__ == '__main__':
    main()
