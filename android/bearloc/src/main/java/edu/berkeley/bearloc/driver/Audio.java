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
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocFormat;
import edu.berkeley.bearloc.BearLocSensor;
import edu.berkeley.bearloc.DeviceUUID;
import root.gast.audio.record.AudioClipListener;
import root.gast.audio.record.AudioClipRecorder;

public class Audio implements BearLocSensor.Driver {

    private long mSampleDuration = -1; // millisecond
    private long mSampleItvl = 2000; // millisecond

    private int mSampleRate = 44100;
    private int mEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int mSampleLengthMillisSec = 1000; //millisecond
    private int mSource = MediaRecorder.AudioSource.MIC;
    private int mChannel = AudioFormat.CHANNEL_IN_MONO;

    private boolean mRun = false;
    private int mSampleNum;

    private Context mContext;
    private Handler mHandler;
    private SensorListener mListener;
    private AudioClipRecorder mAudioClipRecorder;

    private AudioClipListener mAudioClipListener = new AudioClipListener() {
        @Override
        public boolean heard(short[] audioData, int sampleRate) {
            if (mRun == true) {
                Log.d("Audio:", "received " + audioData.length*2 + " bytes of data at " + sampleRate + "Hz");
                JSONArray data = new JSONArray();
                for (short b : audioData) {
                    data.put(b);
                }
                if (mListener != null) {
                    mListener.onSampleEvent(BearLocFormat.format("audio", data, makeMeta()));
                }
                mSampleNum++;

                mHandler.postDelayed(mAudioSampleRunnable, mSampleItvl);
            }
            return true;
        }
    };

    private final Runnable mAudioSampleRunnable = new Runnable() {
        @Override
        public void run() {
            sample();
        }
    };

    private final Runnable mPauseRunnable = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    /*
    Default constructor customized for ABS
     */
    public Audio(final Context context) {
        mContext = context;

        mHandler = new Handler();
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
        if (mRun == false) {
            if (mAudioClipRecorder == null) {
                return false;
            }

            Log.d("Audio:", "Start recording");
            mSampleNum = 0;
            mRun = true;
            mHandler.postDelayed(mAudioSampleRunnable, 0);
            if (mSampleDuration > 0) {
                mHandler.postDelayed(mPauseRunnable, mSampleDuration);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean stop() {
        Log.d("Audio:", "Stopping recording");
        if (mRun == true) {
            mHandler.removeCallbacks(mAudioSampleRunnable);
            mHandler.removeCallbacks(mPauseRunnable);

            mRun = false;
        }
        return true;
    }

    private void sample() {
        try {
            Log.d("Audio:", "Making sample ...");
            mAudioClipRecorder.startRecordingForTime(mSampleLengthMillisSec, mSampleRate, mEncoding);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject makeMeta() {
        JSONObject meta = new JSONObject();
        try {
            meta.put("type", "audio");
            meta.put("uuid", DeviceUUID.getDeviceUUID(mContext));
            meta.put("sysnano", System.nanoTime());
            meta.put("epoch", System.currentTimeMillis());
            meta.put("source", mSource);
            meta.put("channel", mChannel);
            meta.put("encoding", mEncoding);
            meta.put("samplerate", mSampleRate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return meta;
    }
}