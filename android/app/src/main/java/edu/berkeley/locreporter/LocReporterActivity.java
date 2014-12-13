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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocApp;
import edu.berkeley.bearloc.BearLocApp.LocListener;
import edu.berkeley.bearloc.BearLocSensor;

public class LocReporterActivity extends Activity {

    private JSONObject mCurLoc;
    private String mCurSem;

    private TextView mLocPrefixTextView;
    private TextView mCurSemLocTextView;
    private Button mLocButton;

    private BearLocApp mBearLocApp;
    private BearLocSensor mWiFiSensor;
    private BearLocSensor mLocationReporter;

    final private static String[] mSemantics = new String[]{"country", "state",
            "city", "street", "building", "locale"};

    private OnClickListener mLocateButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mBearLocApp.getLocation() == true) {
                //mLocButton.setEnabled(false);
            }
        }

    };

    private LocListener mLocListener = new LocListener() {

        @Override
        public void onResponseReturned(JSONObject response) {
            mLocButton.setEnabled(true);

            if (response == null) {
                Toast.makeText(LocReporterActivity.this,
                        R.string.server_no_respond, Toast.LENGTH_SHORT).show();
                return;
            }

            onLocReturned(response);
        }

    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_reporter);

        mCurLoc = new JSONObject();
        mCurSem = mSemantics[mSemantics.length - 1];

        mLocPrefixTextView = (TextView) findViewById(R.id.loc_prefix);
        mCurSemLocTextView = (TextView) findViewById(R.id.cur_sem_loc);

        mLocButton = (Button) findViewById(R.id.localize);
        mLocButton.setOnClickListener(mLocateButtonOnClickListener);
        mLocButton.setEnabled(true);

        mBearLocApp = new BearLocApp(this, mLocListener,
                "tcp://bearloc.cal-sdb.org:52411", "algorithm001-request");

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
     * Update UI
     */
    private void refresh() {
        // update location text
        if (mCurLoc != null) {
            mLocPrefixTextView.setText(getLocStr(mCurLoc, mSemantics, mCurSem));
            mCurSemLocTextView.setText(mCurSem + ":\n"
                    + mCurLoc.optString(mCurSem, null));
        }
    }

    public void onLocReturned(final JSONObject locEvent) {
        final JSONObject oldLoc = mCurLoc;
        mCurLoc = locEvent; // The response is JSON Array
        if (oldLoc == null
                || oldLoc.toString().equals(mCurLoc.toString()) == false) {
            refresh();
            Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public String getLocStr(final JSONObject loc, final String[] sems,
                            final String endSem) {
        String locStr = "";

        for (final String sem : sems) {
            if (sem == endSem) {
                break;
            }
            locStr += "/" + loc.optString(sem, null);
        }

        return locStr;
    }
}