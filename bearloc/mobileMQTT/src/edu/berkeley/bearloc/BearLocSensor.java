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

import android.content.Context;
import android.util.Log;

public class BearLocSensor {
    private MqttAndroidClient mqttClient;
    private final Context mContext;
    private final Driver mDriver;
    private String mAlgorithmTopic;

    private final Driver.OnSampleEventListener mListener = new Driver.OnSampleEventListener() {

        @Override
        public void onSampleEvent(Object data) {
            String topic = mAlgorithmTopic;
            String message = data.toString();
            postMqttMessage(topic, message);
        }

    };

    public interface Driver {
        public abstract boolean setListener(OnSampleEventListener listener);

        // duration in millisecond
        public abstract boolean start(long duration, long frequency);

        public abstract boolean stop();

        public static interface OnSampleEventListener {
            void onSampleEvent(Object data);
        }
    }

    class SensorMqttCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");
        }

        public void messageArrived(String topic, MqttMessage message) {
            Log.d(getClass().getCanonicalName(), "Message arrived:" + topic
                    + ":" + message.toString());
            JSONObject jsonMessage = null;
            try {
                jsonMessage = new JSONObject(message.getPayload().toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (mDriver != null) {
                // TODO get duration and frequency from jsonMessage
                mDriver.start(0, 0);
            }
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(getClass().getCanonicalName(), "Delivery complete");
        }
    }

    public BearLocSensor(Context context, String mqttServerURI, Driver driver,
            String algorithmTopic) {
        mContext = context;
        mDriver = driver;
        mAlgorithmTopic = algorithmTopic;

        mqttClient = new MqttAndroidClient(mContext, mqttServerURI,
                MqttClient.generateClientId());
        mqttClient.setCallback(new SensorMqttCallback());
        MqttConnectOptions options = new MqttConnectOptions();
        try {
            mqttClient.connect(options);
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(),
                    "Connection attempt failed with reason code = "
                            + e.getReasonCode() + ":" + e.getCause());
        }
        mqttSubscribe("sensor_name"); // TODO

        mDriver.setListener(mListener);
    }

    /* Private methods. */

    private boolean mqttSubscribe(final String topic) {
        try {
            IMqttToken token = mqttClient.subscribe(topic, 1);
            token.waitForCompletion();
            return true;
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(),
                    "Subscribe failed with reason code = " + e.getReasonCode());
            return false;
        }
    }

    /**
     * An MqttMessage holds the application payload and options specifying how
     * the message is to be delivered The message includes a "payload" (the body
     * of the message) represented as a byte[].
     * 
     * @return
     */
    private boolean postMqttMessage(final String topic, final String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            IMqttDeliveryToken token = mqttClient.publish(topic, message);
            token.waitForCompletion();
            return true;
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(),
                    "Publish failed with reason code = " + e.getReasonCode());
            return false;
        }
    }
}
