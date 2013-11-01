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

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

public class Audio implements Sampler {

  private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
  private static final int AUDIO_SAMPLE_RATE = 11025; // Hz
  private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

  private boolean mBusy;

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
            Audio.AUDIO_SAMPLE_RATE, Audio.AUDIO_CHANNEL, Audio.AUDIO_FORMAT);
        mRecorder = new AudioRecord(Audio.AUDIO_SOURCE,
            Audio.AUDIO_SAMPLE_RATE, Audio.AUDIO_CHANNEL, Audio.AUDIO_FORMAT,
            bufferSize);

        mStartEpoch = System.currentTimeMillis();
        mRecorder.startRecording();

        while (mRun == true) {
          final byte[] buffer = new byte[bufferSize];
          // blocking read, which returns when buffer.length bytes are recorded
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
        audioEvent.put("source", Audio.AUDIO_SOURCE);
        audioEvent.put("channel", Audio.AUDIO_CHANNEL);
        audioEvent.put("sampwidth", Audio.AUDIO_FORMAT);
        audioEvent.put("framerate", Audio.AUDIO_SAMPLE_RATE);
      } catch (final JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return audioEvent;
    }
  }

  public Audio(final SamplerListener listener) {
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
  public boolean start(final Integer period, Integer num) {
    num = null; // num is not used in Audio
    if (mBusy == false) {
      mBusy = true;
      mAudioRecordThread = new AudioRecordThread();
      mAudioRecordThread.start();
      mHandler.postDelayed(mPauseTask, period);
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
}
