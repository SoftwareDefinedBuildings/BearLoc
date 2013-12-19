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

package edu.berkeley.bearloc.sampler;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.widget.Toast;
import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.util.SamplerSettings;

public class Wifi implements Sampler {
    private boolean mBusy;

    private final Context mContext;
    private final SamplerListener mListener;
    private final Handler mHandler;
    private final WifiManager mWifiManager;
    private WifiLock mWifiLock;

    public static interface SamplerListener {
        public abstract void onWifiEvent(List<ScanResult> results);
    }

    private final BroadcastReceiver mOnScanDone = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mBusy == true) {
                final List<ScanResult> results = mWifiManager.getScanResults();

                if (mListener != null) {
                    mListener.onWifiEvent(results);
                }

                mHandler.postDelayed(mWifiScanTask, 0);
            }
        }
    };

    private final Runnable mWifiScanTask = new Runnable() {
        @Override
        public void run() {
            scan();
        }
    };

    public Wifi(final Context context, final SamplerListener listener) {
        mContext = context;
        mListener = listener;
        mHandler = new Handler();
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        final IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mOnScanDone, i);
    }

    @Override
    public boolean start() {
        if (mBusy == false && SamplerSettings.getWifiEnable(mContext) == true) {
            if (mWifiManager == null) {
                SamplerSettings.setWifiEnable(mContext, false);
                Toast.makeText(mContext, R.string.bearloc_wifi_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            mHandler.postDelayed(mWifiScanTask, 0);
            mBusy = true;
            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,
                    "BearLoc");
            mWifiLock.acquire();
            return true;
        } else {
            return false;
        }
    }

    private void scan() {
        final boolean success = mWifiManager.startScan();

        if (success == false) {
            mHandler.postDelayed(mWifiScanTask, 0);
        }
    }
}
