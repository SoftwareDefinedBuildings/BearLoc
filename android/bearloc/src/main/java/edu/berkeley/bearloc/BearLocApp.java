/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 * Author: Siyuan (Jack) He <siyuanhe@berkeley.edu>
 */

package edu.berkeley.bearloc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class BearLocApp {
    private MqttAndroidClient mMQTTClient;
    private Context mContext;
    private LocListener mListener;
    private String mAlgorithmTopic;

    private MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Log.d(getClass().getCanonicalName(), "Message arrived:" + topic + ":" + message.toString());

            if (mListener == null) {
                return;
            }

            JSONObject json = null;
            try {
                String payload = new String(message.getPayload());
                json = new JSONObject(payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (json.has("msgtype") && json.getString("msgtype").equals("locResult")) {
                    JSONObject loc = json.getJSONObject("result");
                    mListener.onResponseReturned(loc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(getClass().getCanonicalName(), "Delivery complete");
        }
    };

    private class ConnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... v) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            try {
                IMqttToken token = mMQTTClient.connect(options);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class LocRequestTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... vals) {
            String wifiTopic = (String) vals[0];
            final JSONObject json = new JSONObject();
            String uuid = DeviceUUID.getDeviceUUID(mContext).toString();
            Long epoch = System.currentTimeMillis()/1000;
            String backTopic = uuid + "-" + epoch;
            // TODO handle exceptions
            try {
                IMqttToken token = mMQTTClient.subscribe(backTopic, 0);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }

            try {
                json.put("msgtype", "locationRequest");
                json.put("uuid", uuid);
                json.put("epoch", epoch);
                json.put("backtopic", backTopic);
                json.put("wifitopic", wifiTopic);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(json.toString().getBytes());
                IMqttDeliveryToken token = mMQTTClient.publish(mAlgorithmTopic, message);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface LocListener {
        public abstract void onResponseReturned(JSONObject response);
    }

    public BearLocApp(Context context, LocListener listener, String mqttServerURI, String algorithmTopic) {
        mContext = context;
        mListener = listener;
        mAlgorithmTopic = algorithmTopic;

        mMQTTClient = new MqttAndroidClient(mContext, mqttServerURI, MqttClient.generateClientId());
        mMQTTClient.setCallback(mMqttCallback);
        new ConnectTask().execute();
    }

    public boolean getLocation(String wifiTopic) {
        // TODO blocking call for packing request and waiting for MQTT ack for now
        new LocRequestTask().execute(wifiTopic);
        return true;
    }
}