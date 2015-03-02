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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

import edu.berkeley.bearloc.BearLocApp;
import edu.berkeley.bearloc.BearLocApp.LocListener;
import edu.berkeley.bearloc.BearLocSensor;
import edu.berkeley.bearloc.driver.Audio;
import edu.berkeley.bearloc.driver.WiFi;

public class LocReporterActivity extends Activity {

    //Wifi
    private TextView mWifiSemLocTextView;
    private TextView mWifiPrefixTextView;
    private Button mWifiButton;

    private JSONObject mWifiLocJson;
    private String mWifiSemLoc;

    private BearLocApp mWifiBearLocApp;
    private String mWifiTopic;
    private BearLocSensor mWiFiSensor;
    private HashMap<String, String> mWifiSensorMap = new HashMap<String, String>();

    //ABS
    private TextView mAbsSemLocTextView;
    private TextView mAbsPrefixTextView;
    private Button mAbsButton;

    private JSONObject mAbsLocJson;
    private String mAbsSemLoc;
    private HashMap<String, String> mAbsSensorMap = new HashMap<String, String>();

    private BearLocApp mAbsBearLocApp;
    private BearLocSensor mAudioSensor;
    private String mAudioTopic;



    // TODO: define a location class that handles semantics, string and JSON conversions
    final private static String[] mSemantics = new String[]{"country", "state",
            "city", "street", "building", "locale"};

    private OnClickListener mWifiButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mWifiBearLocApp.sessionStarted()) {
                if (mWifiBearLocApp.stopSession()) {
                    mWifiButton.setText("Start Session");
                }
            } else {
                if (mWifiBearLocApp.startSession(mWifiSensorMap)) {
                    mWifiButton.setText("Stop Session");
                }
            }
        }

    };

    private OnClickListener mAbsButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mAbsBearLocApp.sessionStarted()) {
                if (mAbsBearLocApp.stopSession()) {
                    mAbsButton.setText("Start Session");
                }
            } else {
                if (mAbsBearLocApp.startSession(mAbsSensorMap)) {
                    mAbsButton.setText("Stop Session");
                }
            }
        }

    };

    private LocListener mWifiLocListener = new LocListener() {

        @Override
        public void onResponseReturned(JSONObject response) {

            if (response == null) {
                Toast.makeText(LocReporterActivity.this,
                        R.string.server_no_respond, Toast.LENGTH_SHORT).show();
                return;
            }
            onLocReturned("wifi", response);
        }

    };

    private LocListener mAbsLocListener = new LocListener() {

        @Override
        public void onResponseReturned(JSONObject response) {

            if (response == null) {
                Toast.makeText(LocReporterActivity.this,
                        R.string.server_no_respond, Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Main:", "ABS got response.");
            onLocReturned("abs", response);
        }

    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_reporter);

        String serverURI = getString(R.string.bearloc_server_addr);

        // Wifi App

        mWifiLocJson = new JSONObject();
        mWifiSemLoc = mSemantics[mSemantics.length - 1];

        mWifiSemLocTextView = (TextView) findViewById(R.id.wifi_sem_loc);
        mWifiPrefixTextView = (TextView) findViewById(R.id.wifi_loc_prefix);
        mWifiButton = (Button) findViewById(R.id.wifiButton);
        mWifiButton.setOnClickListener(mWifiButtonOnClickListener);
        mWifiButton.setEnabled(true);

        String wifiAlgorithmTopic = getString(R.string.bearloc_wifi_algorithm_topic);
        mWifiBearLocApp = new BearLocApp(this, mWifiLocListener, serverURI, wifiAlgorithmTopic);

        // Abs App

        mAbsLocJson = new JSONObject();
        mAbsSemLoc = mSemantics[mSemantics.length - 1];

        mAbsSemLocTextView = (TextView) findViewById(R.id.abs_sem_loc);
        mAbsPrefixTextView = (TextView) findViewById(R.id.abs_loc_prefix);
        mAbsButton = (Button) findViewById(R.id.absButton);
        mAbsButton.setOnClickListener(mAbsButtonOnClickListener);
        mAbsButton.setEnabled(true);

        //Sensor Setup

        mWifiTopic = getString(R.string.bearloc_wifi_topic);
        mWiFiSensor = new BearLocSensor(this, new WiFi(this), serverURI, mWifiTopic);
        mWiFiSensor.start();
        mWifiSensorMap.put("wifi", mWifiTopic);

        mAudioTopic = getString(R.string.bearloc_audio_topic);
        mAudioSensor = new BearLocSensor(this, new Audio(this), serverURI, mAudioTopic);
        mAudioSensor.start();
        mAbsSensorMap.put("audio", mAudioTopic);

        String absAlgorithmTopic = getString(R.string.bearloc_abs_algorithm_topic);
        mAbsBearLocApp = new BearLocApp(this, mAbsLocListener, serverURI, absAlgorithmTopic);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onDestroy() {
        mWifiBearLocApp.destroy();
        mAbsBearLocApp.destroy();
        mAudioSensor.destroy();
        mWiFiSensor.destroy();
        super.onDestroy();
    }

    /*
     * Update UI
     */
    private void refresh() {
        // update location text
        if (mWifiLocJson != null) {
            mWifiSemLocTextView.setText(locToStr(mWifiLocJson, mSemantics, mWifiSemLoc));
            mWifiPrefixTextView.setText(mWifiSemLoc + ":"
                    + mWifiLocJson.optString(mWifiSemLoc, null));
        }

        if (mAbsLocJson != null) {
            mAbsSemLocTextView.setText(locToStr(mAbsLocJson, mSemantics, mAbsSemLoc));
            mAbsPrefixTextView.setText(mAbsSemLoc + ":"
                    + mAbsLocJson.optString(mAbsSemLoc, null));
        }
    }

    public void onLocReturned(String algorithm, final JSONObject locEvent) {
        JSONObject oldLoc = null;
        if (algorithm.equals("wifi")) {
            oldLoc = mWifiLocJson;
            mWifiLocJson = locEvent; // The response is JSON Array
        } else if (algorithm.equals("abs")) {
            oldLoc = mAbsLocJson;
            mAbsLocJson = locEvent; // The response is JSON Array
        }
        if (oldLoc == null || oldLoc.toString().equals(locEvent.toString()) == false) {
            refresh();
            Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT).show();
        }

    }

    public String locToStr(final JSONObject loc, final String[] sems, final String endSem) {
        String locStr = "";

        for (final String sem : sems) {
            if (sem.equals(endSem)) {
                break;
            }
            locStr += "/" + loc.optString(sem, null);
        }

        return locStr;
    }
}