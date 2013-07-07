package edu.berkeley.boss.ambience;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class Sensors implements Ambience, SensorEventListener {

  public static final long NANOS_PER_MILLIS = 1000000L;
  public static final long MICROS_PER_MILLIS = 1000L;

  private static final long SENSOR_DEFAULT_HISTORY_LEN = 10000L; // millisecond

  private final Context mContext;

  private final SensorManager mSensorManager;
  private final List<Sensor> mSensorList;
  private final SparseIntArray mSensorSampleRate;
  private final SparseArray<Long> mSensorHistoryLen;
  private final HashMap<Sensor, LinkedList<SensorEvent>> mSensorEventCache;

  public Sensors(Context context) {
    mContext = context;

    mSensorManager = (SensorManager) mContext
        .getSystemService(Context.SENSOR_SERVICE);
    mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

    mSensorSampleRate = new SparseIntArray();
    mSensorHistoryLen = new SparseArray<Long>();
    mSensorEventCache = new HashMap<Sensor, LinkedList<SensorEvent>>();
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

  @Override
  public void resume() {
    for (Sensor sensor : mSensorList) {
      final int rate = mSensorSampleRate.get(sensor.getType(),
          SensorManager.SENSOR_DELAY_NORMAL);
      mSensorManager.registerListener(this, sensor, rate);
    }
  }

  @Override
  public void pause() {
    mSensorManager.unregisterListener(this);
  }

  @Override
  public void clear() {
    mSensorEventCache.clear();
  }

  @Override
  public JSONObject get() {
    final JSONObject sensorPack = new JSONObject();

    try {
      // Pack all sensor data to JSONObject
      for (HashMap.Entry<Sensor, LinkedList<SensorEvent>> entry : mSensorEventCache
          .entrySet()) {
        final Sensor sensor = entry.getKey();

        final JSONObject sensorJSONObject = new JSONObject();
        sensorPack.put(sensor.getName(), sensorJSONObject);

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

    return sensorPack;
  }

}
