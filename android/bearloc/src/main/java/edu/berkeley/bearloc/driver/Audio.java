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

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocSensor;
import root.gast.audio.record.AudioClipListener;
import root.gast.audio.record.AudioClipRecorder;

public class Audio implements BearLocSensor.Driver {

    private final int STOP = 42;

    private int mSampleRate;
    private int mEncoding;
    private int mSampleLengthMillisSec;

    private AudioClipRecorder mAudioClipRecorder;
    private JSONArray mRaw;
    private Long mStartEpoch;
    private volatile boolean mRun = false;
    private Object runLock = new Object();

    private Context mContext;
    private SensorListener mListener;
    private Handler mHandler;
    private Handler sampleHandler;

    private AudioClipListener mAudioClipListener = new AudioClipListener() {
        @Override
        public boolean heard(short[] audioData, int sampleRate) {
            for (short b : audioData) {
                mRaw.put(b);
            }
            if (mListener != null) {
                mListener.onSampleEvent(dump2JSON());
            }
            return true;
        }
    };

    /*
    Default constructor customized for ABS
     */
    public Audio(final Context context) {
        mContext = context;
        mHandler = new Handler();

        mSampleRate = 44100;
        mEncoding = AudioFormat.ENCODING_PCM_16BIT;
        mSampleLengthMillisSec = 1000; //millisecond

        mAudioClipRecorder = new AudioClipRecorder(mAudioClipListener);
    }

    /*
    Customizable constructor where the user can select sampling options.
     */
    public Audio(final Context context, int _sampleRate, int _channel, int _format, int _sampleLength) {
        mContext = context;
        mHandler = new Handler();

        mSampleRate = _sampleRate;
        mEncoding = _format;
        mSampleLengthMillisSec = _sampleLength;

        mAudioClipRecorder = new AudioClipRecorder(mAudioClipListener);
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
            mRaw = new JSONArray();
            mAudioClipRecorder.startRecordingForTime(mSampleLengthMillisSec, mSampleRate, mEncoding);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject dump2JSON() {
        final JSONObject audioEvent = new JSONObject();

        try {
            audioEvent.put("raw", mRaw);
            audioEvent.put("epoch", mStartEpoch);
            audioEvent.put("source", MediaRecorder.AudioSource.MIC);
            audioEvent.put("channel", AudioFormat.CHANNEL_IN_MONO);
            audioEvent.put("encoding", mEncoding);
            audioEvent.put("lengthms", mSampleLengthMillisSec);
            audioEvent.put("samplerate", mSampleRate);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return audioEvent;
    }


}