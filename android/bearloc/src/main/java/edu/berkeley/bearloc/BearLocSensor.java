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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BearLocSensor {
    private MqttAndroidClient mMQTTClient;
    private final Context mContext;
    private final Driver mDriver;
    private String mSensorTopic;  // topic to publish data to

    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

//    // Sets the amount of time an idle thread waits before terminating
//    private static final int KEEP_ALIVE_TIME = 1;
//    // Sets the Time Unit to seconds
//    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
//    // Creates a thread pool manager
//    private ExecutorService mNetworkThreadPool = new ThreadPoolExecutor(
//            NUMBER_OF_CORES,       // Initial pool size
//            NUMBER_OF_CORES,       // Max pool size
//            KEEP_ALIVE_TIME,
//            KEEP_ALIVE_TIME_UNIT,
//            new LinkedBlockingQueue<Runnable>());

    private final Driver.SensorListener mListener = new Driver.SensorListener() {
        @Override
        public void onSampleEvent(Object data) {
            Log.d("BearLocSensor", "Got sensor data");
//            mNetworkThreadPool.execute(new DataPublishRunnable(data.toString()));
//            AsyncTask.execute(new DataPublishRunnable(data));
            new DataPublishTask().execute(data.toString());
        }

    };

    private MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");
        }

        @Override
        public void messageArrived(String str, MqttMessage msg) {}

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d("BearLocSensor", "Delivery complete");
        }
    };

//    private class ConnectRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setCleanSession(true);
//            try {
//                IMqttToken token = mMQTTClient.connect(options);
//                token.waitForCompletion();
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    private class DataPublishRunnable implements Runnable {
//
//        private Object mPayload;
//
//        DataPublishRunnable(Object _payload) {
//            this.mPayload = _payload;
//        }
//
//        @Override
//        public void run() {
//            Log.d("BearLocSensor", "Publishing Data");
//            final JSONObject json = new JSONObject();
//            String uuid = DeviceUUID.getDeviceUUID(mContext).toString();
//            Long epoch = System.currentTimeMillis();
//
//            try {
//                json.put("msgtype", "data");
//                json.put("uuid", uuid);
//                json.put("epoch", epoch);
//                json.put("data", this.mPayload);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                MqttMessage message = new MqttMessage();
//                message.setPayload(json.toString().getBytes());
//                IMqttDeliveryToken token = mMQTTClient.publish(mSensorTopic, message);
//                token.waitForCompletion();
//                Log.d("BearLocSensor", mSensorTopic + " published");
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    // start can be blocking
//    private class StartRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            mDriver.start();
//        }
//    }

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
            Log.d("BearLocSensor", "Publishing Data");
            String payload = str[0];
            final JSONObject json = new JSONObject();
            String uuid = DeviceUUID.getDeviceUUID(mContext).toString();
            Long epoch = System.currentTimeMillis();

            try {
                json.put("msgtype", "data");
                json.put("uuid", uuid);
                json.put("epoch", epoch);
                json.put("data", payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(json.toString().getBytes());
                IMqttDeliveryToken token = mMQTTClient.publish(mSensorTopic, message);
//                token.waitForCompletion();
                Log.d("BearLocSensor", mSensorTopic + " published");
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
        mMQTTClient.setCallback(mMqttCallback);
        new ConnectTask().execute();
//        AsyncTask.execute(new ConnectRunnable());
//        mNetworkThreadPool.execute(new ConnectRunnable());

        mDriver.setListener(mListener);
    }

    public boolean start() {
//        mNetworkThreadPool.execute(new StartRunnable());
        new StartTask().execute();
        return true;
    }

    public boolean stop() {
        return mDriver.stop();
    }

    public void destroy() {
        mMQTTClient.unregisterResources();
        mMQTTClient.close();
//        mNetworkThreadPool.shutdownNow();
    }
}