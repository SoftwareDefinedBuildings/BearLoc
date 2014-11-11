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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
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

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import edu.berkeley.bearloc.BearLocSampler.OnSampleEventListener;
import edu.berkeley.bearloc.util.DeviceUUID;
import edu.berkeley.bearloc.util.ServerSettings;
import edu.berkeley.bearlocinterface.LocListener;
import edu.berkeley.bearlocinterface.LocService;

public class BearLocService extends LocService implements OnSampleEventListener {

    private static final int DATA_SEND_ITVL = 100; // millisecond
    private static final int LOC_DELAY = 300; // millisecond

    private static final List<String> mSemantic = new LinkedList<String>(
            Arrays.asList("country", "state", "city", "street", "building",
                    "locale"));

    private IBinder mBinder;
    private Handler mHandler;
    // in millisecond, null if nothing is scheduled
    private Integer mDataSendItvl = null;

    private BearLocCache mCache;
    private BearLocSampler mSampler;
    private BearLocFormat mFormat;
    
    private MqttAndroidClient mqttClient = null;
    private HashMap<String, LocListener> mListenerMap = new HashMap<String, LocListener>();

    private class SendRequestTask implements Runnable {
        private final LocListener mListener;
        private final String mReqType;
        private final Object mArg;

        public SendRequestTask(final String reqType, final LocListener listener, Object arg) {
            mArg = arg;
        	mReqType = reqType;
        	mListener = listener;
        }

        @Override
        public void run() {
        	if (mReqType == "location") {
        		sendLocRequest(mListener, mArg);
        	} else if (mReqType == "candidate") {
        		sendCandidateRequest(mListener, mArg);
        	}
        }
    };

    private class SendDataTask implements Runnable {
        @Override
        public void run() {
            sendData();
        }
    };
    
    public class exampleCallBack implements MqttCallback  
    {  
        public void connectionLost(Throwable cause)  
        {  
              Log.d(getClass().getCanonicalName(), "MQTT Server connection lost");  
        }  
        public void messageArrived(String topic, MqttMessage message)  {  
        	Log.d(getClass().getCanonicalName(), "Message arrived:" + topic + ":" + message.toString());
        	JSONObject jsonResponse = null;
			try {
				jsonResponse = new JSONObject(message.getPayload().toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (mListenerMap.containsKey(topic)) {
				mListenerMap.get(topic).onResponseReturned(jsonResponse);
			}
			
        }  
        public void deliveryComplete(IMqttDeliveryToken token)  
        {
              Log.d(getClass().getCanonicalName(), "Delivery complete");  
        }  
    }  

    @Override
    public void onCreate() {
        mBinder = new LocBinder();
        mHandler = new Handler();
        mCache = new BearLocCache(this);
        mSampler = new BearLocSampler(this, this);
        mFormat = new BearLocFormat(this, mCache);
        mqttClient = new MqttAndroidClient(getApplicationContext(), "tcp://bearloc.cal-sdb.org:52411", MqttClient.generateClientId());
    }

    @Override
    public IBinder onBind(final Intent intent) {
    	mqttClient.setCallback(new exampleCallBack());  
    	MqttConnectOptions options = new MqttConnectOptions();  
        try  
        {  
            mqttClient.connect(options);  
        }  
        catch (MqttException e)  
        {                    
            Log.d(getClass().getCanonicalName(), "Connection attempt failed with reason code = " + e.getReasonCode() + ":" + e.getCause());  
        } 
        return mBinder;
    }
    
    public void registerListener(String topic, LocListener listener) {
    	mListenerMap.put(topic, listener);
    }

    @Override
    public boolean getLocation(final LocListener listener) {
        if (listener == null) {
            return false;
        }

        mSampler.sample();
        
        // TODO: true doesn't mean it will be called, what a "G00d" design.
        return mHandler.postDelayed(new SendRequestTask("location", listener, null),
                BearLocService.LOC_DELAY);
    }

    @Override
    public boolean getLocation(final UUID id, final Long time,
            final LocListener listener) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getCandidate(final JSONObject loc,
            final LocListener listener) {
        if (listener == null) {
            return false;
        }
        
        return mHandler.postDelayed(new SendRequestTask("candidate", listener, loc),
                BearLocService.LOC_DELAY);
        
    }
    
    @Override
    public boolean postData(final String type, final JSONObject data) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("type", type);
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final JSONObject formated = mFormat.format(data, meta);
        if (formated != null) {
            mCache.add(formated);
            mSampler.sample();
        }

        return true;
    }
    
    @Override
    public void onSampleEvent(final String type, final Object data) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("type", type);
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final JSONObject formated = mFormat.format(data, meta);
        if (formated != null) {
            mCache.add(formated);
            if (mDataSendItvl == null) {
                mDataSendItvl = BearLocService.DATA_SEND_ITVL;
                mHandler.postDelayed(new SendDataTask(), mDataSendItvl);
            }
        }
    }
    
    /* Private methods. */

    private void sendLocRequest(final LocListener listener, final Object arg) {
        final JSONObject request = new JSONObject();
        String uuid = DeviceUUID.getDeviceUUID(this).toString();
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
        }
        
        String topic = "algorithm001-request";
        String message = request.toString();
        
        mqttSubscribe(backtopic);
        registerListener(backtopic, listener);
        postMqttMessage(topic, message);
        
    }
    
    private void sendCandidateRequest(LocListener listener, final Object arg) {
    	final JSONObject loc = (JSONObject) arg;
    	
    	final JSONObject request = new JSONObject();
    	String uuid = DeviceUUID.getDeviceUUID(this).toString();
        String epoch = Long.toString(System.currentTimeMillis());
        String backtopic = uuid + "-" + epoch;
    	
    	String directory = "";
    	String locStr;
    	
    	for (final String semantic : mSemantic) {
            locStr = loc.optString(semantic);
            if (locStr.length() == 0) {
                break;
            }
            directory += locStr + "/";
        }
    	
    	try {
            request.put("msgtype", "candidateRequest");
            request.put("uuid", uuid);
            request.put("epoch", epoch);
            request.put("directory", directory);
            request.put("backtopic", backtopic);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String topic = "candidate-request";
        String message = request.toString();
        
        mqttSubscribe(backtopic);
        registerListener(backtopic, listener);
        postMqttMessage(topic, message);
    }

    private void sendData() {
    	final JSONObject request = new JSONObject();
    	final JSONArray jsondata = mCache.get();
    	String uuid = DeviceUUID.getDeviceUUID(this).toString();
        String epoch = Long.toString(System.currentTimeMillis());
        String data = jsondata.toString();

        try {
            request.put("msgtype", "data");
            request.put("uuid", uuid);
            request.put("epoch", epoch);
            request.put("data", data);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    	String topic = "algorithm001-data";
    	String message = request.toString();
        
    	// get all cached data
        
        if (jsondata.length() > 0) {
        	//TODO: Put the data back to the cache if we cannot publish it
        	if (!postMqttMessage(topic, message)) {
        		mCache.addAll(jsondata);
        	}
            mHandler.postDelayed(new SendDataTask(), mDataSendItvl);
        } else {
            mDataSendItvl = null;
        }
    }
    
    private boolean mqttSubscribe(final String topic) {
    	try  
        {  
            IMqttToken token = mqttClient.subscribe(topic, 2);
            token.waitForCompletion();
            return true;
        }  
        catch (MqttException e)  
        {  
            Log.d(getClass().getCanonicalName(), "Subscribe failed with reason code = " + e.getReasonCode());  
            return false;  
        }
    }
    
    /** 
     * An MqttMessage holds the application payload and options 
     * specifying how the message is to be delivered 
     * The message includes a "payload" (the body of the message) 
     * represented as a byte[]. 
     * @return 
     */  
    private boolean postMqttMessage(final String topic, final String msg) {  
         try  
         {  
             MqttMessage message = new MqttMessage();  
             message.setPayload(msg.getBytes());  
             IMqttDeliveryToken token = mqttClient.publish(topic, message);
             token.waitForCompletion();
             return true;
         }  
         catch (MqttException e)  
         {  
             Log.d(getClass().getCanonicalName(), "Publish failed with reason code = " + e.getReasonCode());
             return false;
         } 
    }

    private URL getHttpURL(final String path) {
        URL url = null;
        try {
            final String serverHost = ServerSettings.getServerAddr(this);
            final int serverPort = ServerSettings.getServerPort(this);
            // TODO handle the exception of using IP address
            final URI uri = new URI("http", null, serverHost, serverPort, path,
                    null, null);
            url = uri.toURL();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
}
