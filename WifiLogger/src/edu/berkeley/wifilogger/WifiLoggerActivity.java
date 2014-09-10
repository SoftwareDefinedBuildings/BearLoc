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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.berkeley.wifilogger.WifiLoggerService.WifiLoggerBinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WifiLoggerActivity extends Activity
        implements
            LoggerListener,
            OnClickListener {

    private static final int LOG_VIEW_MAX_LINE = 25;
    private static final int STATUS_VIEW_MAX_DOTS = 30;

    private List<String> mLogs = new LinkedList<String>();
    private int mNumDots = 0;
    
    private TextView mLogView;
    private TextView mStatusView;
    private Button mStartButton;
    private Button mStopButton;
    private boolean confirmingStop = false;

    private WifiLoggerService mService;
    private boolean mBound = false;
    
    private Handler mHandler;
    
    private PowerManager.WakeLock mWakeLock;
    
    private final Runnable mCancelConfirmTask = new Runnable() {
        @Override
        public void run() {
            stopConfirm();
        }
    };

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

        mLogView = (TextView) findViewById(R.id.logview);
        mLogView.setMovementMethod(new ScrollingMovementMethod());
        mStatusView = (TextView) findViewById(R.id.statusview);

        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setOnClickListener(this);
        mStartButton.setEnabled(true);
        mStopButton = (Button) findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mStopButton.setEnabled(false);

        final Intent intent = new Intent(this, WifiLoggerService.class);
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);

        mHandler = new Handler();
        
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
        switch (v.getId()) {
            case R.id.start :
                mService.start();
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                break;
            case R.id.stop :
                if (confirmingStop) 
                {
                    stopConfirm();
                    mService.stop();
                    mStartButton.setEnabled(true);
                    mStopButton.setEnabled(false);
                    confirmingStop = false;
                } else {
                    startConfirm();
                    mHandler.postDelayed(mCancelConfirmTask, 1000);
                }
                break;
            default :
                break;
        }
    }

    @Override
    public void onWritten(String written) {
        mLogs.add(written);
        
        // remove old logs when full
        if (mLogs.size() >= LOG_VIEW_MAX_LINE)
        {
            mLogs.remove(0);
        }
        
        // Show all logs
        String log = "";
        for (String str:mLogs){
            log += str +"\n";
        }
        mLogView.setText(log);
    }

    @Override
    public void onSampleEvent() {
        mNumDots = (mNumDots + 1) % STATUS_VIEW_MAX_DOTS;
        final char[] chars = new char[mNumDots];
        Arrays.fill(chars, '.');
        final String status = new String(chars);
        mStatusView.setText(status);
    }
    
    private void startConfirm()
    {
        mStopButton.setText(R.string.confirm);
        confirmingStop = true;
    }
    
    private void stopConfirm()
    {
        mHandler.removeCallbacks(mCancelConfirmTask);
        mStopButton.setText(R.string.stop);
        confirmingStop = false;
    }
}
