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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.widget.Toast;
import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.util.SamplerSettings;

public class Audio implements Sampler {

    private int mSrc;
    private int mSampleRate;
    private int mChannel;
    private int mFormat;

    private boolean mBusy;

    private final Context mContext;
    private final SamplerListener mListener;
    private final Handler mHandler;

    public static interface SamplerListener {
        public abstract void onAudioEvent(JSONObject audio);
    }

    private AudioRecordThread mAudioRecordThread;

    private class AudioRecordThread extends Thread {

        private volatile boolean mRun = true;
        private Long mStartEpoch;
        private AudioRecord mRecorder;
        private final JSONArray mRaw = new JSONArray();

        @Override
        public void run() {
            try {
                final int bufferSize = AudioRecord.getMinBufferSize(
                        mSampleRate, mChannel, mFormat);
                mRecorder = new AudioRecord(mSrc, mSampleRate, mChannel,
                        mFormat, bufferSize);

                mStartEpoch = System.currentTimeMillis();
                mRecorder.startRecording();

                while (mRun == true) {
                    final byte[] buffer = new byte[bufferSize];
                    // blocking read, which returns when buffer.length bytes are
                    // recorded
                    mRecorder.read(buffer, 0, buffer.length); // Bytes
                    for (final byte data : buffer) {
                        mRaw.put(data);
                    }
                }

                mRecorder.stop();
                mRecorder.release();

                mListener.onAudioEvent(dump());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        public void terminate() {
            mRun = false;
        }

        private JSONObject dump() {
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

    public Audio(final Context context, final SamplerListener listener) {
        mContext = context;
        mListener = listener;
        mHandler = new Handler();
    }

    private final Runnable mPauseTask = new Runnable() {
        @Override
        public void run() {
            pause();
        }
    };

    @Override
    public boolean start() {
        if (mBusy == false && SamplerSettings.getAudioEnable(mContext) == true) {
            if (validate() == false) {
                return false;
            }

            final long duration = SamplerSettings.getAudioDuration(mContext);
            mAudioRecordThread = new AudioRecordThread();
            mAudioRecordThread.start();
            mHandler.postDelayed(mPauseTask, duration);
            mBusy = true;
            return true;
        } else {
            return false;
        }
    }

    private void pause() {
        if (mBusy == true) {
            mBusy = false;
            mAudioRecordThread.terminate();
            try {
                mAudioRecordThread.join();
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mHandler.removeCallbacks(mPauseTask);
        }
    }

    private boolean validate() {
        // TODO use listener to do it
        mSrc = SamplerSettings.getAudioSrc(mContext);
        mSampleRate = SamplerSettings.getAudioSampleRate(mContext);
        mChannel = SamplerSettings.getAudioChannel(mContext);
        mFormat = SamplerSettings.getAudioFormat(mContext);

        int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel,
                mFormat);

        if (bufferSize <= 0) {
            mSampleRate = Integer
                    .parseInt(mContext
                            .getString(R.string.bearloc_default_audio_sample_rate_value));
            mChannel = Integer.parseInt(mContext
                    .getString(R.string.bearloc_default_audio_channel_value));
            mFormat = Integer.parseInt(mContext
                    .getString(R.string.bearloc_default_audio_format_value));

            bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel,
                    mFormat);
            if (bufferSize <= 0) {
                SamplerSettings.setAudioEnable(mContext, false);
                Toast.makeText(mContext, R.string.bearloc_audio_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            SamplerSettings.setAudioSampleRate(mContext, mSampleRate);
            SamplerSettings.setAudioChannel(mContext, mChannel);
            SamplerSettings.setAudioFormat(mContext, mFormat);
        }

        return true;
    }
}
