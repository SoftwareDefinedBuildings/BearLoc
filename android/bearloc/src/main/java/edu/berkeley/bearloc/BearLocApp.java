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
import android.os.Looper;
import android.util.Log;
import android.os.Handler;

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

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BearLocApp {

    private final int STOP = 42;
    private final int HEARTBEAT_PERIOD = 10000;

    private MqttAndroidClient mMQTTClient;
    private Context mContext;
    private LocListener mListener;
    private String mAlgorithmTopic;
    private String mResultTopic;
    private String mHeartBeatTopic;
    private boolean mSessionStarted;
    private String muuid;
    private Long mSessionEpoch;
    private Object mListenerLock = new Object();
    private JSONObject lastLocation;
    private Handler mHeartBeatHandler;

    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Creates a thread pool manager
    private static ExecutorService mNetworkThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            new LinkedBlockingQueue<Runnable>());


    private MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Log.d(getClass().getCanonicalName(), "Message arrived:" + topic + ":" + message.toString());

            if (!topic.equals(mResultTopic)) {
                return;
            }

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
                    synchronized (mListenerLock) {
                        mListener.onResponseReturned(loc);
                        lastLocation = loc;
                    }
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

    private class ConnectRunnable implements Runnable {

        @Override
        public void run() {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            try {
                IMqttToken token = mMQTTClient.connect(options);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private class HeartBeatRunnable implements Runnable {

        private String heartbeattopic;
        private String uuid;


        public HeartBeatRunnable (String _uuid,
                                    String _heartbeattopic) {
            this.uuid = _uuid;
            this.heartbeattopic = _heartbeattopic;
            mHeartBeatHandler = new Handler();
        }

        @Override
        public void run() {
            while (true) {
                final JSONObject json = new JSONObject();
                Long epoch = System.currentTimeMillis()/1000;

                try {
                    json.put("msgtype", "heartbeat");
                    json.put("uuid", uuid);
                    json.put("epoch", epoch);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(json.toString().getBytes());
                    IMqttDeliveryToken token = mMQTTClient.publish(heartbeattopic, message);
                    token.waitForCompletion();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(HEARTBEAT_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mHeartBeatHandler) {
                    if (!mSessionStarted) {
                        break;
                    }
                }
            }
            Log.d(getClass().getCanonicalName(), "HearBeat Stopped");
        }
    }

    private class StartSessionRunnable implements Runnable {

        private HashMap<String, String> sensorMap;
        private String resulttopic;
        private String heartbeattopic;
        private String uuid;
        private Long epoch;

        public StartSessionRunnable(HashMap<String, String> _sensorMap,
                                    String _uuid,
                                    Long _epoch,
                                    String _resulttopic,
                                    String _heartbeattopic) {
            this.sensorMap = _sensorMap;
            this.uuid = _uuid;
            this.epoch = _epoch;
            this.resulttopic = _resulttopic;
            this.heartbeattopic = _heartbeattopic;
        }

        @Override
        public void run() {
            final JSONObject json = new JSONObject();

            // TODO handle exceptions
            try {
                IMqttToken token = mMQTTClient.subscribe(resulttopic, 0);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonSensor = new JSONObject(sensorMap);
                json.put("msgtype", "startSession");
                json.put("uuid", uuid);
                json.put("epoch", epoch);
                json.put("sensormap", jsonSensor);
                json.put("resulttopic", resulttopic);
                json.put("heartbeattopic", heartbeattopic);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(json.toString().getBytes());
                Log.d(getClass().getCanonicalName(), json.toString());
                IMqttDeliveryToken token = mMQTTClient.publish(mAlgorithmTopic, message);
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public interface LocListener {
        public abstract void onResponseReturned(JSONObject response);
    }

    public BearLocApp(Context context, LocListener listener,
                      String mqttServerURI, String algorithmTopic) {
        mContext = context;
        mListener = listener;
        mAlgorithmTopic = algorithmTopic;
        mResultTopic = null;
        mHeartBeatTopic = null;
        mSessionStarted = false;
        muuid = DeviceUUID.getDeviceUUID(mContext).toString();

        mMQTTClient = new MqttAndroidClient(mContext, mqttServerURI, MqttClient.generateClientId());
        mMQTTClient.setCallback(mMqttCallback);
        mNetworkThreadPool.execute(new ConnectRunnable());
    }

    public boolean startSession(HashMap<String, String> sensorMap) {
        if (mSessionStarted) {
            return false;
        }
        mSessionEpoch = System.currentTimeMillis()/1000;
        mResultTopic = muuid + "-" + mSessionEpoch;
        mHeartBeatTopic = mResultTopic + "-heartbeat";
        mNetworkThreadPool.execute(new StartSessionRunnable(sensorMap, muuid, mSessionEpoch,
                mResultTopic, mHeartBeatTopic));
        mNetworkThreadPool.execute(new HeartBeatRunnable(muuid, mHeartBeatTopic));
        mSessionStarted = true;
        return true;
    }

    public boolean stopSession() {
        if (mHeartBeatHandler.sendEmptyMessage(STOP)){
            synchronized (mHeartBeatHandler) {
                mSessionStarted = false;
            }
            return true;
        } else {
            return false;
        }

    }

    public void registerListener(LocListener _listener) {
        synchronized (mListenerLock) {
            mListener = _listener;
        }
    }

    public JSONObject getLatestLocation() {
        return lastLocation;
    }

    public boolean sessionStarted() {
        return mSessionStarted;
    }
}