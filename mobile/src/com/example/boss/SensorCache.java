package com.example.boss;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Pair;
import android.util.SparseIntArray;

public class SensorCache implements SensorEventListener {

  private final int AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
  private final int AUDIO_SAMPLE_RATE = 44100; // Hz
  private final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private final int AUDIO_BUFFER_RATIO = 2;

  private final Context mContext;

  private final SensorManager mSensorManager;
  private final List<Sensor> mSensorList;
  private final SparseIntArray mSampleRate;
  private final HashMap<Sensor, LinkedList<SensorEvent>> mSensorEventCache;

  private AudioRecordThread mAudioRecordThread;
  private final LinkedBlockingQueue<Pair<Long, ByteArrayOutputStream>> mAudioDataBlockingQueue;

  public SensorCache(Context context) {
    mContext = context;

    mSensorManager = (SensorManager) mContext
        .getSystemService(Context.SENSOR_SERVICE);
    mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

    mSampleRate = new SparseIntArray();
    mSensorEventCache = new HashMap<Sensor, LinkedList<SensorEvent>>();

    mAudioDataBlockingQueue = new LinkedBlockingQueue<Pair<Long, ByteArrayOutputStream>>();

  }

  public void resume() {
    for (Sensor sensor : mSensorList) {
      final int rate = mSampleRate.get(sensor.getType(),
          SensorManager.SENSOR_DELAY_NORMAL);
      mSensorManager.registerListener(this, sensor, rate);
    }

    mAudioRecordThread = new AudioRecordThread();
    mAudioRecordThread.start();
  }

  public void pause() {
    mSensorManager.unregisterListener(this);

    mAudioRecordThread.terminate();
    try {
      mAudioRecordThread.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
    }
  }

  public JSONObject getSensorData() {
    final JSONObject sensorData = new JSONObject();

    try {
      // Pack all sensor data to JSONObject
      for (HashMap.Entry<Sensor, LinkedList<SensorEvent>> entry : mSensorEventCache
          .entrySet()) {
        final Sensor sensor = entry.getKey();

        final JSONObject sensorJSONObject = new JSONObject();
        sensorData.put(sensor.getName(), sensorJSONObject);

        sensorJSONObject.put("maximum range", sensor.getMaximumRange());
        sensorJSONObject.put("minimum delay", sensor.getMinDelay());
        sensorJSONObject.put("name", sensor.getName());
        sensorJSONObject.put("power", sensor.getPower());
        sensorJSONObject.put("resolution", sensor.getResolution());
        sensorJSONObject.put("type", sensor.getType());
        sensorJSONObject.put("vendor", sensor.getVendor());
        sensorJSONObject.put("version", sensor.getVersion());

        final JSONArray eventJSONArray = new JSONArray();
        sensorJSONObject.put("events", eventJSONArray);

        for (SensorEvent event : entry.getValue()) {
          final JSONObject eventJSONObject = new JSONObject();
          eventJSONArray.put(eventJSONObject);

          eventJSONObject.put("accuracy", event.accuracy);
          eventJSONObject.put("timestamp", event.timestamp);

          final JSONArray valueJSONArray = new JSONArray();
          eventJSONObject.put("values", valueJSONArray);

          for (float value : event.values) {
            valueJSONArray.put(value);
          }
        }
      }

      // Pack cached audio data
      final JSONObject audioJSONObject = new JSONObject();
      sensorData.put("audio", audioJSONObject);

      audioJSONObject.put("name", "audio");
      audioJSONObject.put("type", "audio");
      audioJSONObject.put("source", AUDIO_SOURCE);
      audioJSONObject.put("sample rate", AUDIO_SAMPLE_RATE);
      audioJSONObject.put("channel", AUDIO_CHANNEL);
      audioJSONObject.put("format", AUDIO_FORMAT);

      final JSONArray eventJSONArray = new JSONArray();
      audioJSONObject.put("events", eventJSONArray);

      for (Pair<Long, ByteArrayOutputStream> event : mAudioDataBlockingQueue) {
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

      // TODO Add WiFi data

    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    clear();

    return sensorData;
  }

  public void clear() {
    mSensorEventCache.clear();
    mAudioDataBlockingQueue.clear();
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    LinkedList<SensorEvent> eventList = mSensorEventCache.get(event.sensor);
    if (eventList == null) {
      eventList = new LinkedList<SensorEvent>();
      mSensorEventCache.put(event.sensor, eventList);
    }
    eventList.addLast(event);
  }

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
        mAudioDataBlockingQueue.offer(event);
      }

      recorder.stop();
      recorder.release();
    }

    public void terminate() {
      mRun = false;
    }
  }
}