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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;
import edu.berkeley.bearloc.BearLocSampler.OnSampleEventListener;
import edu.berkeley.bearloc.util.DeviceUUID;
import edu.berkeley.bearloc.util.JSONHttpPostTask;
import edu.berkeley.bearloc.util.JSONHttpPostTask.onJSONHttpPostRespondedListener;
import edu.berkeley.bearloc.util.ServerSettings;

public class BearLocService extends Service
        implements
            LocService,
            OnSampleEventListener {

    private static final int DATA_SEND_ITVL = 1000; // millisecond

    private IBinder mBinder;
    private Handler mHandler;
    // in millisecond, null if nothing is scheduled
    private Integer mDataSendItvl = null;

    private BearLocCache mCache;
    private BearLocSampler mSampler;
    private BearLocFormat mFormat;

    private class SendDataTask implements Runnable {
        @Override
        public void run() {
            sendData();
        }
    };

    public class BearLocBinder extends Binder {
        public BearLocService getService() {
            // Return this instance so clients can call public methods
            return BearLocService.this;
        }
    }

    @Override
    public void onCreate() {
        mBinder = new BearLocBinder();
        mHandler = new Handler();
        mCache = new BearLocCache(this);
        mSampler = new BearLocSampler(this, this);
        mFormat = new BearLocFormat(this, mCache);
    }

    @Override
    public void onDestroy() {
        mSampler.stop();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public boolean postData(final String type, final JSONObject data) {
        if (type != null || data != null) {
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
        }

        return true;
    }

    @Override
    public void stopPostData() {
        mSampler.stop();
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
    private void sendData() {
        final String path = "/api/data/"
                + DeviceUUID.getDeviceUUID(this).toString();
        final URL url = getHttpURL(path);

        // get all cached data
        final JSONArray data = mCache.get();

        if (data.length() > 0) {
            try {
                new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
                    @Override
                    public void onJSONHttpPostResponded(final JSONArray response) {
                        if (response == null) {
                            mCache.addAll(data);
                        }
                    }
                }).execute(url, data);
            } catch (final RejectedExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mHandler.postDelayed(new SendDataTask(), mDataSendItvl);
        } else {
            mDataSendItvl = null;
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
