package com.example.boss;

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
import android.util.SparseIntArray;

public class SensorCache implements SensorEventListener {

  private final Context mContext;

  private final SensorManager mSensorManager;
  private final List<Sensor> mSensorList;
  private final SparseIntArray mSampleRate;
  private final HashMap<Sensor, LinkedList<SensorEvent>> mSensorEventCache;

  // private final Sensor mAccelerometer;
  // private final Sensor mTemperature;
  // private final Sensor mGravity;
  // private final Sensor mGyroscope;
  // private final Sensor mLight;
  // private final Sensor mLinearAcceleration;
  // private final Sensor mMagneticField;
  // private final Sensor mOrientation;
  // private final Sensor mPressure;
  // private final Sensor mProximity;
  // private final Sensor mRelativeHumidity;
  // private final Sensor mRotationVector;

  public SensorCache(Context context) {
    mContext = context;

    mSensorManager = (SensorManager) mContext
        .getSystemService(Context.SENSOR_SERVICE);
    mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

    mSampleRate = new SparseIntArray();
    mSensorEventCache = new HashMap<Sensor, LinkedList<SensorEvent>>();

    // mAccelerometer =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    // mTemperature =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    // mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    // mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    // mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    // mLinearAcceleration =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    // mMagneticField =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    // mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    // mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    // mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    // mRelativeHumidity =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    // mRotationVector =
    // mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
  }

  public void resume() {
    for (Sensor sensor : mSensorList) {
      final int rate = mSampleRate.get(sensor.getType(),
          SensorManager.SENSOR_DELAY_NORMAL);
      mSensorManager.registerListener(this, sensor, rate);
    }
  }

  public void pause() {
    mSensorManager.unregisterListener(this);
  }

  public JSONObject getSensorData() {
    final JSONObject sensorData = new JSONObject();

    for (HashMap.Entry<Sensor, LinkedList<SensorEvent>> entry : mSensorEventCache
        .entrySet()) {
      for (SensorEvent event : entry.getValue()) {
        final Sensor sensor = event.sensor;
        JSONObject sensorJSONObject = sensorData
            .optJSONObject(sensor.getName());

        try {
          final JSONArray valueJSONArray = new JSONArray();
          for (float value : event.values) {
            valueJSONArray.put(value);
          }

          final JSONObject eventJSONObject = new JSONObject();
          eventJSONObject.put("accuracy", event.accuracy);
          eventJSONObject.put("timestamp", event.timestamp);
          eventJSONObject.put("values", valueJSONArray);

          if (sensorJSONObject == null) {
            JSONArray eventJSONArray = new JSONArray();

            sensorJSONObject = new JSONObject();
            sensorJSONObject.put("maximum range", sensor.getMaximumRange());
            sensorJSONObject.put("minimum delay", sensor.getMinDelay());
            sensorJSONObject.put("name", sensor.getName());
            sensorJSONObject.put("power", sensor.getPower());
            sensorJSONObject.put("resolution", sensor.getResolution());
            sensorJSONObject.put("type", sensor.getType());
            sensorJSONObject.put("vendor", sensor.getVendor());
            sensorJSONObject.put("version", sensor.getVersion());
            sensorJSONObject.put("events", eventJSONArray);

            sensorData.put(sensor.getName(), sensorJSONObject);
          }

          final JSONArray eventJSONArray = sensorJSONObject
              .getJSONArray("events");

          eventJSONArray.put(eventJSONObject);
        } catch (JSONException e) {
          // TODO Auto-generated catch block
        }
      }
    }

    return sensorData;
  }
  
  public void clear() {
    mSensorEventCache.clear();
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
}