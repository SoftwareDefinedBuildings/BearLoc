#!/usr/bin/env python

# Algorithm Manager, Client of Cap'n Proto

import socket
import capnp

import algorithm_capnp # read algorithm.capnp

import paho.mqtt.client as mqtt
import simplejson as json
import random
import subprocess
import atexit

algorithm_addr = 'localhost'
algorithm_next_port = 10000

mqtt_broker_addr = "bearloc.cal-sdb.org"
mqtt_broker_port = 52411

control_topic = "bearloc/algorithm/dummy"

algorithm_exec = "/root/workspace/BearLoc/python/bearloc/algorithms/dummy.py"

algorithm_processes = []

sensor_topic_map = {} # sensor topic to [capnp client, result_topic] list mapping
# TODO add heartbeat suppport

mqtt_client = None

# The callback for when the client receives a CONNACK response from the broker.
def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(control_topic)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    payload_json = json.loads(str(msg.payload))
    print(msg.topic+" "+str(payload_json))
    if msg.topic == control_topic:
        print msg.topic
        sensor_map = payload_json["sensormap"]
        result_topic = payload_json["resulttopic"]
        heartbeat_topic = payload_json["heartbeattopic"]
        
        # run a new algorithm instance
        global algorithm_next_port
        addr = algorithm_addr+":"+str(algorithm_next_port)
        algorithm_next_port = algorithm_next_port + 1
        proc = subprocess.Popen([algorithm_exec, addr], stderr=subprocess.PIPE)
        algorithm_processes.append(proc)
        print proc.stderr.readline() # wait for the algorithm instance to be ready, kinda hack
        capnp_client = init_capnp(addr)
        print("Initiated a new algorithm instance")

        # record mapping information and subscribe to sensors
        for sensor_key, sensor_topic in sensor_map.iteritems():
            if sensor_topic not in sensor_topic_map:
                sensor_topic_map[sensor_topic] = []
                mqtt_client.subscribe(sensor_topic)
                print("Subscribed to "+sensor_topic)
            sensor_topic_map[sensor_topic].append([capnp_client, result_topic])

        # TODO subscribe to heartbeat topic

    elif msg.topic in sensor_topic_map:
        print msg.topic
        capnp_clients = sensor_topic_map[msg.topic]
        for capnp_client, result_topic in capnp_clients:
            localize_promise = capnp_client.localize() # pass the data
            print localize_promise, result_topic
            publish_location_once = lambda response: publish_location(response, result_topic)
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

@atexit.register
def cleanup():
    for proc in algorithm_processes:
        proc.kill()
    print "cleaned up"

def main():
    global mqtt_client
    mqtt_client = init_mqtt()
    mqtt_client.loop_forever()
    

if __name__ == '__main__':
    main()
