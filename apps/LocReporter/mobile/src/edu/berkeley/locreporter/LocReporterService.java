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

package edu.berkeley.locreporter;

import org.json.JSONObject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import edu.berkeley.bearlocinterface.LocService;
import edu.berkeley.bearlocinterface.LocService.LocBinder;
import edu.berkeley.bearlocinterface.CandidateListener;
import edu.berkeley.bearlocinterface.LocListener;
import edu.berkeley.bearloc.R; //TODO: The configuration data should be provided as arguments 

public class LocReporterService extends Service implements SensorEventListener {

    private static final long AUTO_REPORT_ITVL = 180000L; // millisecond

    private LocService mBearLocService;
    private boolean mBound = false;
    private final ServiceConnection mBearLocConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name,
                final IBinder service) {
            final LocBinder binder = (LocBinder) service;
            mBearLocService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBound = false;
        }
    };

    final public static String[] Semantics = new String[]{"country", "state",
            "city", "street", "building", "locale"};

    private IBinder mBinder;
    private Handler mHandler;

    private Sensor mAcc;
    private boolean mAutoReport;

    private class LocReportTask implements Runnable {
        private final JSONObject mLoc;

        public LocReportTask(final JSONObject loc) {
            mLoc = loc;
        }

        @Override
        public void run() {
            if (mAutoReport == true) {
                reportSemLoc(mLoc);
            }
        }
    };

    public class LocReporterBinder extends Binder {
        public LocReporterService getService() {
            // Return this instance so clients can call public methods
            return LocReporterService.this;
        }
    }

    @Override
    public void onCreate() {
        final Intent intent = new Intent(this, LocService.class);
        bindService(intent, mBearLocConn, Context.BIND_AUTO_CREATE);

        mBinder = new LocReporterBinder();
        mHandler = new Handler();

        final SensorManager sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcc = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, mAcc,
                SensorManager.SENSOR_DELAY_NORMAL);

        mAutoReport = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mBearLocConn);
            mBound = false;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    public boolean localize(final LocListener listener) {
        if (listener == null) {
            return false;
        }

        return mBearLocService.getLocation(listener);
    }

    public void reportLocation(final JSONObject loc) {
        reportSemLoc(loc);
    }

    public boolean getCandidate(final JSONObject loc,
            final LocListener listener) {
        if (loc == null) {
            return false;
        }

        return mBearLocService.getCandidate(loc, listener);
    }

    private void reportSemLoc(final JSONObject loc) {
        if (loc == null) {
            return;
        }

        mBearLocService
                .postData(
                        getResources().getString(
                                R.string.reported_semantic_loc), loc);

        // start new auto report schedule if not yet
        if (mAutoReport == false && mAcc != null
                && LocReporterSettingsActivity.getAutoReport(this) == true) {
            mAutoReport = true;
            // report in AUTO_REPORT_ITVL milliseconds
            mHandler.postDelayed(new LocReportTask(loc),
                    LocReporterService.AUTO_REPORT_ITVL);
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event != null
                && (Math.abs(event.values[0]) > 1
                        || Math.abs(event.values[0]) > 1 || event.values[2] < 9)) {
            // If not statically face up, then stop reporting location
            mAutoReport = false;
        }
    }
}
