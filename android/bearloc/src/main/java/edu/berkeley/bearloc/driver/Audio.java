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

package edu.berkeley.bearloc.driver;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocSensor;

public class Audio implements BearLocSensor.Driver {

    private final int STOP = 42;

    private int mSrc;
    private int mSampleRate;
    private int mChannel;
    private int mFormat;
    private int mSampleLength;
    private int bufferSize;

    private AudioRecord mRecorder;
    private final JSONArray mRaw = new JSONArray();
    private Long mStartEpoch;
    private volatile boolean mRun = false;
    private Object runLock = new Object();

    private boolean mBusy;

    private Context mContext;
    private SensorListener mListener;
    private Handler mHandler;
    private Handler sampleHandler;

    /*
    Default constructor customized for ABS
     */
    public Audio(final Context context) {
        mContext = context;
        mHandler = new Handler();

        mSrc = MediaRecorder.AudioSource.MIC;
        mSampleRate = 44100;
        mChannel = AudioFormat.CHANNEL_IN_MONO;
        mFormat = AudioFormat.ENCODING_PCM_16BIT;
        mSampleLength = 1000; //millisecond

        bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mFormat);
        mRecorder = new AudioRecord(mSrc, mSampleRate, mChannel, mFormat, bufferSize);
    }

    /*
    Customizable constructor where the user can select sampling options.
     */
    public Audio(final Context context, int _src, int _sampleRate, int _channel, int _format, int _sampleLength) {
        mContext = context;
        mHandler = new Handler();

        mSrc = _src;
        mSampleRate = _sampleRate;
        mChannel = _channel;
        mFormat = _format;
        mSampleLength = _sampleLength;

        bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mFormat);
        mRecorder = new AudioRecord(mSrc, mSampleRate, mChannel, mFormat, bufferSize);
    }

    @Override
    public void setListener(SensorListener listener) {
        mListener = listener;
    }

    /*
    Running on another thread.
     */
    @Override
    public boolean start() {
        // TODO: How to do message passing between threads
        synchronized (runLock) {
            mRun = true;
        }
        if (sampleHandler == null) {
            sampleHandler = new Handler();
            while (mRun && !sampleHandler.hasMessages(STOP)) {
                makeAudioSample();
            }
            sampleHandler = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean stop() {
        synchronized (runLock) {
            mRun = false;
        }
        return mHandler.sendEmptyMessage(STOP);
    }

    public void makeAudioSample() {
        try {
            mStartEpoch = System.currentTimeMillis();
            mRecorder.startRecording();

            final byte[] buffer = new byte[bufferSize];
            while (System.currentTimeMillis() - mStartEpoch < mSampleLength) {
                // blocking read, which returns when buffer.length bytes are recorded
                mRecorder.read(buffer, 0, buffer.length); // Bytes
                for (final byte data : buffer) {
                    mRaw.put(data);
                }
            }


            mRecorder.stop();
            mRecorder.release();
            if (mListener != null) {
                mListener.onSampleEvent(dump2JSON());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject dump2JSON() {
        final JSONObject audioEvent = new JSONObject();

        try {
            audioEvent.put("raw", mRaw);
            audioEvent.put("epoch", mStartEpoch);
            audioEvent.put("source", mSrc);
            audioEvent.put("channel", mChannel);
            audioEvent.put("sampwidth", mFormat);
            audioEvent.put("framerate", mSampleRate);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return audioEvent;
    }


}