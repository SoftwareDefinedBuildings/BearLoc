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
        final int bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL, AUDIO_FORMAT);
        mRecorder = new AudioRecord(AUDIO_SOURCE, AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL, AUDIO_FORMAT, bufferSize);

        mStartEpoch = System.currentTimeMillis();
        mRecorder.startRecording();

        while (mRun == true) {
          final byte[] buffer = new byte[bufferSize];
          // blocking read, which returns when buffer.length bytes are recorded
          mRecorder.read(buffer, 0, buffer.length); // Bytes
          for (byte data : buffer) {
            mRaw.put(data);
          }
        }

        mRecorder.stop();
        mRecorder.release();

        mListener.onAudioEvent(dump());
      } catch (Exception e) {
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
        audioEvent.put("source", AUDIO_SOURCE);
        audioEvent.put("channel", AUDIO_CHANNEL);
        audioEvent.put("sampwidth", AUDIO_FORMAT);
        audioEvent.put("framerate", AUDIO_SAMPLE_RATE);
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return audioEvent;
    }
  }

  public Audio(SamplerListener listener) {
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
  public boolean start(Integer period, Integer num) {
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
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      mHandler.removeCallbacks(mPauseTask);
    }
  }
}
