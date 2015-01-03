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

public class BearLocSensor {
    private MqttAndroidClient mMQTTClient;
    private final Context mContext;
    private final Driver mDriver;
    private String mSensorTopic;  // topic to publish data to

    private final Driver.SensorListener mListener = new Driver.SensorListener() {
        @Override
        public void onSampleEvent(Object data) {
            new DataPublishTask().execute(data.toString());
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

    private class DataPublishTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... str) {
            String payload = str[0];
            final JSONObject json = new JSONObject();
            String uuid = DeviceUUID.getDeviceUUID(mContext).toString();
            Long epoch = System.currentTimeMillis()/1000;

            try {
                json.put("msgtype", "wifidata");
                json.put("uuid", uuid);
                json.put("epoch", epoch);
                json.put("data", str);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(json.toString().getBytes());
                IMqttDeliveryToken token = mMQTTClient.publish(mSensorTopic, message);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // start can be blocking
    private class StartTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... v) {
            mDriver.start();
            return null;
        }
    }

    public interface Driver {
        public abstract void setListener(SensorListener listener);

        public abstract boolean start();

        public abstract boolean stop();

        public static interface SensorListener {
            void onSampleEvent(Object data);
        }
    }

    public BearLocSensor(Context context, Driver driver, String mqttServerURI, String sensorTopic) {
        mContext = context;
        mDriver = driver;
        mSensorTopic = sensorTopic;

        mMQTTClient = new MqttAndroidClient(mContext, mqttServerURI, MqttClient.generateClientId());
        new ConnectTask().execute();

        mDriver.setListener(mListener);
    }

    public boolean start() {
        new StartTask().execute();
        return true;
    }

    public boolean stop() {
        return mDriver.stop();
    }
}