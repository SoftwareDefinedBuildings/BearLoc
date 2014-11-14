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
 * Author: Siyuan (Jack) He <siyuanhe@berkeley.edu>
 */

package edu.berkeley.bearloc;

import org.json.JSONException;
import org.json.JSONObject;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.berkeley.bearloc.util.DeviceUUID;

public class BearLocApp {
    private MqttAndroidClient mqttClient;
    private Context mContext;
    private LocListener mListener;
    private String mAlgorithmTopic;
    
    private class ConnectTask extends AsyncTask<Void, Void, Void> {
    	
    	private MqttAndroidClient mClient;
        private MqttConnectOptions mOptions;

        ConnectTask (MqttAndroidClient client, MqttConnectOptions options) {
            mClient = client;
            mOptions = options;
        }
    	
        protected Void doInBackground(Void... v) {
        	Thread t = Thread.currentThread();
            t.setPriority(t.getPriority());
            try {
                IMqttToken token = mClient.connect(mOptions);
                token.waitForCompletion();
                Log.d(getClass().getCanonicalName(), "Connected!!!!!");
            } catch (MqttException e) {
                Log.d(getClass().getCanonicalName(),
                            "Connection attempt failed with reason code = "
                            + e.getReasonCode() + ":" + e.getCause());
            }
            return null;
        }

        protected void onPostExecute(Void... v) {
            return;
        }
    }

    private class LocRequestTask extends AsyncTask<Void, Void, Void> {
    	
        protected Void doInBackground(Void... v) {
        	sendLocRequest();
        	Log.d(getClass().getCanonicalName(), "Juse sent a request");
        	return null;
        }

        protected void onPostExecute(Void... v) {
            return;
        }
    }

    public interface LocListener {
        public abstract void onResponseReturned(JSONObject response);
    }

    class AppMqttCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");
        }

        public void messageArrived(String topic, MqttMessage message) {
            Log.d(getClass().getCanonicalName(), "Message arrived:" + topic
                    + ":" + message.toString());
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(message.getPayload().toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                if (jsonResponse.has("msgtype")) {
                    String msgtype = jsonResponse.getString("msgtype");
                    if (msgtype == "locResult") {
                        if (mListener != null) {
                            JSONObject locJson = new JSONObject(jsonResponse.getString("result"));
                            mListener.onResponseReturned(locJson);
                        }
                    }
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(getClass().getCanonicalName(), "Delivery complete");
        }
    }

    public BearLocApp(Context context, LocListener listener, String mqttServerURI, String algorithmTopic) {
        mContext = context;
        mListener = listener;
        mAlgorithmTopic = algorithmTopic;
        
        Thread t = Thread.currentThread();
        t.setPriority(t.getPriority());

        mqttClient = new MqttAndroidClient(mContext, mqttServerURI, MqttClient.generateClientId());
        mqttClient.setCallback(new AppMqttCallback());
        MqttConnectOptions options = new MqttConnectOptions();
        new ConnectTask(mqttClient, options).execute();
    }

    public boolean getLocation() {
        // TODO blocking call for packing request and waiting for MQTT ack for now
        new LocRequestTask().execute();
        return true;
    }

    /* Private methods. */

    private boolean sendLocRequest() {
        boolean success = false;

        final JSONObject request = new JSONObject();
        String uuid = DeviceUUID.getDeviceUUID(mContext).toString();
        String epoch = Long.toString(System.currentTimeMillis());
        String backtopic = uuid + "-" + epoch;

        try {
            request.put("msgtype", "locationRequest");
            request.put("uuid", uuid);
            request.put("epoch", epoch);
            request.put("backtopic", backtopic);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            success = false;
        }

        if (success == true) {
            success = mqttSubscribe(backtopic);
        }

        if (success == true) {
            String topic = mAlgorithmTopic;
            String message = request.toString();
            success = postMqttMessage(topic, message);
        }

        return success;
    }

    private boolean mqttSubscribe(final String topic) {
        try {
            IMqttToken token = mqttClient.subscribe(topic, 1);
            // token.waitForCompletion();
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
            // token.waitForCompletion();
            return true;
        } catch (MqttException e) {
            Log.d(getClass().getCanonicalName(),
                    "Publish failed with reason code = " + e.getReasonCode());
            return false;
        }
    }
}
