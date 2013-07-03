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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class SensorCache implements SensorEventListener {

  public static final long NANOS_PER_MILLIS = 1000000L;
  public static final long MICROS_PER_MILLIS = 1000L;

  private final long SENSOR_DEFAULT_HISTORY_LEN = 10000L; // millisecond

  private final int AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
  private final int AUDIO_SAMPLE_RATE = 11025; // Hz
  private final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private final int AUDIO_BUFFER_RATIO = 2;
  private final long AUDIO_HISTORY_LEN = 3000L; // millisecond

  private final long WIFI_SAMPLE_ITVL = 1000L; // millisecond
  private final long WIFI_HISTORY_LEN = 10000L; // millisecond

  private final Context mContext;
  private final Handler mHandler;

  private final SensorManager mSensorManager;
  private final List<Sensor> mSensorList;
  private final SparseIntArray mSensorSampleRate;
  private final SparseArray<Long> mSensorHistoryLen;
  private final HashMap<Sensor, LinkedList<SensorEvent>> mSensorEventCache;

  private final LinkedBlockingQueue<Pair<Long, ByteArrayOutputStream>> mAudioEventQueue;
  private AudioRecordThread mAudioRecordThread;

  private final WifiManager mWifiManager;
  private final LinkedBlockingQueue<Pair<Long, ScanResult>> mWifiEventQueue;
  private final Runnable mWifiScanTimeTask;

  public SensorCache(Context context) {
    mContext = context;

    mHandler = new Handler();

    mSensorManager = (SensorManager) mContext
        .getSystemService(Context.SENSOR_SERVICE);
    mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

    mSensorSampleRate = new SparseIntArray();
    mSensorHistoryLen = new SparseArray<Long>();
    mSensorEventCache = new HashMap<Sensor, LinkedList<SensorEvent>>();

    mAudioEventQueue = new LinkedBlockingQueue<Pair<Long, ByteArrayOutputStream>>();

    mWifiManager = (WifiManager) mContext
        .getSystemService(Context.WIFI_SERVICE);
    mWifiEventQueue = new LinkedBlockingQueue<Pair<Long, ScanResult>>();
    mWifiScanTimeTask = new Runnable() {
      public void run() {
        final List<ScanResult> newResultList = mWifiManager.getScanResults();
        for (ScanResult scanResult : newResultList) {
          final Long timestamp = System.currentTimeMillis();
          final Pair<Long, ScanResult> event = new Pair<Long, ScanResult>(
              timestamp, scanResult);
          mWifiEventQueue.offer(event);
        }

        final Long curTimestamp = System.currentTimeMillis();
        while (mWifiEventQueue.isEmpty() == false) {
          final Long eventTimestamp = mWifiEventQueue.peek().first;
          if ((curTimestamp - eventTimestamp) > WIFI_HISTORY_LEN) {
            // Remove old data
            mWifiEventQueue.poll();
          } else {
            break;
          }
        }

        mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
      }
    };
  }

  public void resume() {
    for (Sensor sensor : mSensorList) {
      final int rate = mSensorSampleRate.get(sensor.getType(),
          SensorManager.SENSOR_DELAY_NORMAL);
      mSensorManager.registerListener(this, sensor, rate);
    }

    mAudioRecordThread = new AudioRecordThread();
    mAudioRecordThread.start();

    mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
  }

  public void pause() {
    mSensorManager.unregisterListener(this);

    mAudioRecordThread.terminate();
    try {
      mAudioRecordThread.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
    }

    mHandler.removeCallbacks(mWifiScanTimeTask);
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

          final Long eventTimestamp = System.currentTimeMillis()
              + (event.timestamp - System.nanoTime()) / NANOS_PER_MILLIS;
          eventJSONObject.put("timestamp", eventTimestamp);

          final JSONArray valueJSONArray = new JSONArray();
          eventJSONObject.put("values", valueJSONArray);

          for (float value : event.values) {
            valueJSONArray.put(value);
          }
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    try {
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
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    try {
      // Pack cached WiFi data
      final JSONObject wifiJSONObject = new JSONObject();
      sensorData.put("wifi", wifiJSONObject);

      wifiJSONObject.put("name", "wifi");
      wifiJSONObject.put("type", "wifi");

      final JSONArray eventJSONArray = new JSONArray();
      wifiJSONObject.put("events", eventJSONArray);

      for (Pair<Long, ScanResult> event : mWifiEventQueue) {
        final JSONObject eventJSONObject = new JSONObject();
        eventJSONArray.put(eventJSONObject);

        final Long timestamp = event.first;
        eventJSONObject.put("timestamp", timestamp);

        final ScanResult scanResult = event.second;
        eventJSONObject.put("BSSID", scanResult.BSSID);
        eventJSONObject.put("SSID", scanResult.SSID);
        eventJSONObject.put("capabilities", scanResult.capabilities);
        eventJSONObject.put("frequency", scanResult.frequency);
        eventJSONObject.put("level", scanResult.level);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    // TODO add GPS data

    clear();

    return sensorData;
  }

  public void clear() {
    mSensorEventCache.clear();
    mAudioEventQueue.clear();
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

    final Long curTimestamp = System.currentTimeMillis();
    while (eventList.isEmpty() == false) {
      final Long eventTimestamp = System.currentTimeMillis()
          + (eventList.peekFirst().timestamp - System.nanoTime())
          / NANOS_PER_MILLIS;
      final Long sensorHistoryLen = mSensorHistoryLen.get(
          eventList.getFirst().sensor.getType(), SENSOR_DEFAULT_HISTORY_LEN);
      if ((curTimestamp - eventTimestamp) > sensorHistoryLen) {
        // Remove old data
        eventList.removeFirst();
      } else {
        break;
      }
    }
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
}