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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import edu.berkeley.bearloc.BearLocSampler.OnSampleEventListener;
import edu.berkeley.bearloc.util.JSONHttpPostTask;
import edu.berkeley.bearloc.util.JSONHttpPostTask.onJSONHttpPostRespondedListener;
import edu.berkeley.bearloc.util.ServerSettings;

public class BearLocService extends Service implements SemLocService,
        OnSampleEventListener {

    private static final int DATA_SEND_ITVL = 100; // millisecond

    private IBinder mBinder;

    private List<SemLocListener> mListeners;
    private Handler mHandler;
    private Integer mDataSendItvl = null; // Millisecond, null if not scheduled

    private BearLocCache mCache;
    private BearLocSampler mSampler;
    private BearLocFormat mFormat;

    private final Runnable mSendLocRequestTask = new Runnable() {
        @Override
        public void run() {
            sendLocRequest();
        }
    };

    private final Runnable mSendDataTask = new Runnable() {
        @Override
        public void run() {
            sendData();
        }
    };

    public class BearLocBinder extends Binder {
        public BearLocService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return BearLocService.this;
        }
    }

    @Override
    public void onCreate() {
        mBinder = new BearLocBinder();
        mListeners = new LinkedList<SemLocListener>();
        mHandler = new Handler();
        mCache = new BearLocCache(this);
        mSampler = new BearLocSampler(this, this);
        mFormat = new BearLocFormat(this);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public boolean localize(final SemLocListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }

        mSampler.sample();

        // Post localization request after 1500 milliseconds
        mHandler.postDelayed(mSendLocRequestTask, 1500);

        return true;
    }

    private void sendLocRequest() {
        try {
            final String path = "/localize";
            final URL url = getHttpURL(path);

            final JSONObject request = new JSONObject();
            request.put("epoch", System.currentTimeMillis());
            request.put("device", BearLocFormat.getDeviceInfo(this));

            new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
                @Override
                public void onJSONHttpPostResponded(final JSONObject response) {
                    if (response == null) {
                        Toast.makeText(BearLocService.this,
                                R.string.bearloc_server_no_respond,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (final SemLocListener listener : mListeners) {
                        if (listener != null) {
                            try {
                                // Generate new copy of response as it calls
                                // back to several
                                // listeners
                                listener.onSemLocInfoReturned(new JSONObject(
                                        response.toString()));
                            } catch (final JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    mListeners.clear();
                }
            }).execute(url, request);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final RejectedExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean report(final JSONObject semloc) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCache.put("semloc", semloc, meta);

        mSampler.sample();

        return true;
    }

    @Override
    public boolean meta(final JSONObject semloc, final MetaListener listener) {
        try {
            final String path = "/meta";
            final URL url = getHttpURL(path);

            final JSONObject request = new JSONObject();
            request.put("semloc", semloc);

            new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
                @Override
                public void onJSONHttpPostResponded(final JSONObject response) {
                    if (response == null) {
                        Toast.makeText(BearLocService.this,
                                R.string.bearloc_server_no_respond,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (listener != null) {
                        listener.onMetaReturned(response);
                    }
                }
            }).execute(url, request);

            return true;
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final RejectedExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    private void sendData() {
        final String path = "/report";
        final URL url = getHttpURL(path);

        final JSONObject report = mFormat.dump(mCache.get());
        mCache.clear();

        if (report.length() > 0) {
            try {
                new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
                    @Override
                    public void onJSONHttpPostResponded(
                            final JSONObject response) {
                        if (response == null) {
                            Toast.makeText(BearLocService.this,
                                    R.string.bearloc_server_no_respond,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute(url, report);
            } catch (final RejectedExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mHandler.postDelayed(mSendDataTask, mDataSendItvl);
        } else {
            mDataSendItvl = null;
        }
    }

    @Override
    public void onSampleEvent(final String type, final Object data) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCache.put(type, data, meta);

        if (mDataSendItvl == null) {
            mDataSendItvl = BearLocService.DATA_SEND_ITVL;
            mHandler.postDelayed(mSendDataTask, mDataSendItvl);
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
            Toast.makeText(this, R.string.bearloc_url_error, Toast.LENGTH_SHORT)
                    .show();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.bearloc_url_error, Toast.LENGTH_SHORT)
                    .show();
        }

        return url;
    }
}
