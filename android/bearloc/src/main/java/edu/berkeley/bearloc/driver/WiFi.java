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

package edu.berkeley.bearloc.driver;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocFormat;
import edu.berkeley.bearloc.BearLocSensor;
import edu.berkeley.bearloc.DeviceUUID;

public class WiFi implements BearLocSensor.Driver {

    private long mSampleDuration = -1; // millisecond
    private long mSampleItvl = 2000; // millisecond

    private boolean mRun;
    private int mSampleNum;

    private Context mContext;
    private SensorListener mListener;
    private Handler mHandler;
    private WifiManager mWifiManager;
    private WifiLock mWifiLock;

    private final BroadcastReceiver mOnScanDone = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mRun == true) {
                final List<ScanResult> results = mWifiManager.getScanResults();
                JSONArray data = new JSONArray();

                for (ScanResult r : results) {
                    data.put(BearLocFormat.format("wifi", r, makeMeta()));
                }
                if (mListener != null) {
                    mListener.onSampleEvent(data);
                }
                mSampleNum++;

                mHandler.postDelayed(mWifiScanRunnable, mSampleItvl);
            }
        }
    };

    private final Runnable mWifiScanRunnable = new Runnable() {
        @Override
        public void run() {
            scan();
        }
    };

    private final Runnable mPauseRunnable = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    public WiFi(final Context context) {
        mContext = context;

        mHandler = new Handler();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void setListener(SensorListener listener) {
        mListener = listener;
    }

    @Override
    public boolean start() {
        if (mRun == false) {
            if (mWifiManager == null) {
                return false;
            }

            final IntentFilter i = new IntentFilter();
            i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(mOnScanDone, i);

            mSampleNum = 0;
            mRun = true;
            mHandler.postDelayed(mWifiScanRunnable, 0);
            if (mSampleDuration > 0) {
                mHandler.postDelayed(mPauseRunnable, mSampleDuration);
            }

            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "BearLoc");
            mWifiLock.acquire();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean stop() {
        if (mRun == true) {
            // If no wifi returned, then return the last know ones
            if (mSampleNum == 0) {
                final List<ScanResult> results = mWifiManager.getScanResults();
                JSONArray data = new JSONArray();
                for (ScanResult r : results) {
                    data.put(BearLocFormat.format("wifi", r, makeMeta()));
                }
                if (mListener != null) {
                    mListener.onSampleEvent(data);
                }
            }

            mWifiLock.release();
            mWifiLock = null;
            mHandler.removeCallbacks(mWifiScanRunnable);
            mHandler.removeCallbacks(mPauseRunnable);
            mContext.unregisterReceiver(mOnScanDone);

            mRun = false;
        }
        return true;
    }

    private void scan() {
        final boolean success = mWifiManager.startScan();

        if (success == false) {
            mHandler.postDelayed(mWifiScanRunnable, mSampleItvl);
        }
    }

    private JSONObject makeMeta() {
        JSONObject meta = new JSONObject();
        try {
            meta.put("type", "wifi");
            meta.put("uuid", DeviceUUID.getDeviceUUID(mContext));
            meta.put("sysnano", System.nanoTime());
            meta.put("epoch", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return meta;
    }
}