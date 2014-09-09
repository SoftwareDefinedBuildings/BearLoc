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

package edu.berkeley.wifilogger;

import edu.berkeley.wifilogger.WifiLoggerService.WifiLoggerBinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WifiLoggerActivity extends Activity
        implements
            WriteListener,
            OnClickListener {

    private TextView mTextView;
    private Button mStartButton;
    private Button mStopButton;

    private WifiLoggerService mService;
    private boolean mBound = false;
    
    private PowerManager.WakeLock mWakeLock;

    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name,
                final IBinder service) {
            final WifiLoggerBinder binder = (WifiLoggerBinder) service;
            mService = binder.getService();
            mService.setWriteListener(WifiLoggerActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBound = false;
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.textview);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setOnClickListener(this);
        mStartButton.setEnabled(true);
        mStopButton = (Button) findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mStopButton.setEnabled(false);

        final Intent intent = new Intent(this, WifiLoggerService.class);
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);

        // Keep screen on
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "BearLoc");
        mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("Wakelock")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound == true) {
            unbindService(mServiceConn);
            mBound = false;
        }
        mWakeLock.release();
    }

    @Override
    public void onClick(final View v) {
        AlertDialog.Builder builder;
        switch (v.getId()) {
            case R.id.start :
                mService.start();
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                break;
            case R.id.stop :
                mService.stop();
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                break;
            default :
                break;
        }
    }

    @Override
    public void onwrittenReturned(String written) {
        mTextView.append(written + "\n");
    }
}
