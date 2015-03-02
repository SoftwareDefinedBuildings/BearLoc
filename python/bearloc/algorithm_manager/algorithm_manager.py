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
import time

algorithm_addr = 'localhost'
algorithm_next_port = 10000

mqtt_broker_addr = "bearloc.cal-sdb.org"
mqtt_broker_port = 52411

control_topic = "bearloc/algorithm/abs"

timeout = 15 # heartbeat timeout in second

algorithm_exec = "/root/workspace/BearLoc/python/bearloc/algorithms/abs.py"

algorithm_processes = []

sensor_topic_map = {} # sensor topic to [capnp client, result_topic] list mapping
heartbeat_topic_map = {} # heartbeat topic to [last_time, process, capnp client] mapping

mqtt_client = None

# The callback for when the client receives a CONNACK response from the broker.
def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(control_topic)
    print("Subscribed to "+control_topic)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    payload_json = json.loads(str(msg.payload))
    if msg.topic == control_topic:
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

        # subscribe to heartbeat topic
        heartbeat_topic_map[heartbeat_topic] = [int(time.time()), proc, capnp_client]
        mqtt_client.subscribe(heartbeat_topic)
        print("Subscribed to "+heartbeat_topic)

    elif msg.topic in sensor_topic_map:
        capnp_clients = sensor_topic_map[msg.topic]
        sensor_data = payload["data"]
        for capnp_client, result_topic in capnp_clients:
            localize_promise = capnp_client.localize(sensor_data) # pass the data
            publish_location_once = lambda response: publish_location(response, result_topic)
            localize_promise.then(publish_location_once).wait()
        print("Got data from "+msg.topic)

    elif msg.topic in heartbeat_topic_map:
        heartbeat_topic_map[msg.topic][0] = int(time.time())
        print("Got heartbeat from "+msg.topic)

    # check heartbeat on message
    check_heartbeat()

def publish_location(localize_response, back_topic):
    location = localize_response.location.to_dict()
    response = {"msgtype":"locResult", "uuid":"somerandomnumberhaha", "epoch": 142500000, "result": location}
    response_str = json.dumps(response)
    mqtt_client.publish(back_topic, payload=response_str, qos=1, retain=True)

# TODO This is too inefficient
def check_heartbeat():
    global heartbeat_topic_map
    global sensor_topic_map
    global algorithm_processes
    tokill = [(proc, capnp_client) for _, (timestamp, proc, capnp_client) in heartbeat_topic_map.iteritems() if int(time.time()) - timestamp > timeout]
    if not tokill:
        return

    tokill_processes, tokill_capnp_clients = zip(*tokill)
    new_heartbeat_topic_map = {}
    for heartbeat_topic, val in heartbeat_topic_map.iteritems():
        if val[1] not in tokill_processes:
            new_heartbeat_topic_map[heartbeat_topic] = val
        else:
            mqtt_client.unsubscribe(heartbeat_topic)
            print("Unsubscribed from "+heartbeat_topic)
    heartbeat_topic_map = new_heartbeat_topic_map

    for proc in tokill_processes:
        proc.kill()
    algorithm_processes = [proc for proc in algorithm_processes if proc not in tokill_processes]
    new_sensor_topic_map = {}
    for sensor_topic, val in sensor_topic_map.iteritems():
        new_val = []
        for capnp_client, result_topic in val:
            if capnp_client not in tokill_capnp_clients:
                new_val.append([capnp_client, result_topic])
        if not new_val:
            mqtt_client.unsubscribe(sensor_topic)
            print("Unsubscribed from "+sensor_topic)
        else:
            new_sensor_topic_map[sensor_topic] = new_val
    sensor_topic_map = new_sensor_topic_map

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
