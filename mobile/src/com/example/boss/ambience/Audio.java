package com.example.boss.ambience;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Pair;

public class Audio implements Ambience {

  private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
  private static final int AUDIO_SAMPLE_RATE = 11025; // Hz
  private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private static final int AUDIO_BUFFER_RATIO = 2;
  private static final long AUDIO_HISTORY_LEN = 3000L; // millisecond

  private final BlockingQueue<Pair<Long, ByteArrayOutputStream>> mAudioEventQueue;
  private AudioRecordThread mAudioRecordThread;

  private class AudioRecordThread extends Thread {

    private volatile boolean mRun = true;
    private AudioRecord recorder;

    @Override
    public void run() {
      final int minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
          AUDIO_CHANNEL, AUDIO_FORMAT);
      final int bufferSize = AUDIO_BUFFER_RATIO * minBufferSize;
      recorder = new AudioRecord(AUDIO_SOURCE, AUDIO_SAMPLE_RATE,
          AUDIO_CHANNEL, AUDIO_FORMAT, bufferSize);

      recorder.startRecording();

      while (mRun == true) {
        final byte[] buffer = new byte[bufferSize];
        final int streamSize = recorder.read(buffer, 0, buffer.length); // Bytes
        final Long timestamp = System.currentTimeMillis();
        final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        final Pair<Long, ByteArrayOutputStream> event = new Pair<Long, ByteArrayOutputStream>(
            timestamp, byteArrayOS);
        byteArrayOS.write(buffer, 0, streamSize);
        // TODO check return result
        mAudioEventQueue.offer(event);

        final Long curTimestamp = System.currentTimeMillis();

        while (mAudioEventQueue.isEmpty() == false) {
          final Long eventTimestamp = mAudioEventQueue.peek().first;
          if ((curTimestamp - eventTimestamp) > AUDIO_HISTORY_LEN) {
            // Remove old data
            mAudioEventQueue.poll();
          } else {
            break;
          }
        }
      }

      recorder.stop();
      recorder.release();
    }

    public void terminate() {
      mRun = false;
    }
  }

  public Audio() {
    mAudioEventQueue = new LinkedBlockingQueue<Pair<Long, ByteArrayOutputStream>>();
  }

  @Override
  public void resume() {
    mAudioRecordThread = new AudioRecordThread();
    mAudioRecordThread.start();
  }

  @Override
  public void pause() {
    mAudioRecordThread.terminate();
    try {
      mAudioRecordThread.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
    }
  }

  @Override
  public void clear() {
    mAudioEventQueue.clear();
  }

  @Override
  public JSONObject get() {
    final JSONObject audioPack = new JSONObject();

    try {
      if (mAudioEventQueue.isEmpty() == false) {
        // Pack cached audio data
        final JSONObject audioJSONObject = new JSONObject();
        audioPack.put("audio", audioJSONObject);

        audioJSONObject.put("name", "audio");
        audioJSONObject.put("type", "audio");
        audioJSONObject.put("source", AUDIO_SOURCE);
        audioJSONObject.put("sample rate", AUDIO_SAMPLE_RATE);
        audioJSONObject.put("channel", AUDIO_CHANNEL);
        audioJSONObject.put("format", AUDIO_FORMAT);

        final JSONArray eventJSONArray = new JSONArray();
        audioJSONObject.put("events", eventJSONArray);

        for (Pair<Long, ByteArrayOutputStream> event : mAudioEventQueue) {
          final JSONObject eventJSONObject = new JSONObject();
          eventJSONArray.put(eventJSONObject);

          final Long timestamp = event.first;
          eventJSONObject.put("timestamp", timestamp);

          final JSONArray valueJSONArray = new JSONArray();
          eventJSONObject.put("values", valueJSONArray);

          final ByteArrayOutputStream byteArrayOS = event.second;
          byte[] audioData = byteArrayOS.toByteArray();
          for (byte data : audioData) {
            valueJSONArray.put(data);
          }
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    return audioPack;
  }
}
