package edu.berkeley.bearloc.loc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import edu.berkeley.bearloc.util.DeviceUUIDFactory;

public class BearLocCache {

  private JSONObject mDeviceInfo;
  private JSONObject mSensorInfo;

  private final Map<String, BlockingQueue<JSONObject>> mDataMap;

  // Only call getMinDelay() before Gingerbread
  @SuppressLint("NewApi")
  public BearLocCache(Context context) {
    mDeviceInfo = new JSONObject();
    try {
      // Device Info
      mDeviceInfo.put("uuid", (new DeviceUUIDFactory(context)).getDeviceUUID()
          .toString());
      mDeviceInfo.put("make", Build.MANUFACTURER);
      mDeviceInfo.put("model", Build.MODEL);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    mSensorInfo = new JSONObject();
    try {
      // Sensor Info
      SensorManager sensorMgr = (SensorManager) context
          .getSystemService(Context.SENSOR_SERVICE);
      List<Sensor> sensorList = sensorMgr.getSensorList(Sensor.TYPE_ALL);
      Iterator<Sensor> iterator = sensorList.iterator();
      while (iterator.hasNext()) {
        Sensor sensor = iterator.next();

        String type = null;
        switch (sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          type = "acc";
          break;
        case Sensor.TYPE_AMBIENT_TEMPERATURE:
          type = "temp";
          break;
        case Sensor.TYPE_GRAVITY:
          type = "gravity";
          break;
        case Sensor.TYPE_GYROSCOPE:
          type = "gyro";
          break;
        case Sensor.TYPE_LIGHT:
          type = "light";
          break;
        case Sensor.TYPE_LINEAR_ACCELERATION:
          type = "lacc";
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          type = "magnetic";
          break;
        case Sensor.TYPE_PRESSURE:
          type = "pressure";
          break;
        case Sensor.TYPE_PROXIMITY:
          type = "proximity";
          break;
        case Sensor.TYPE_RELATIVE_HUMIDITY:
          type = "humidity";
          break;
        case Sensor.TYPE_ROTATION_VECTOR:
          type = "rotation";
          break;
        default:
          break;
        }

        if (type != null) {
          JSONObject meta = new JSONObject();
          meta.put("vendor", sensor.getVendor());
          meta.put("name", sensor.getName());
          meta.put("power", sensor.getPower());
          if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            meta.put("minDelay", sensor.getMinDelay());
          }
          meta.put("maxRange", sensor.getMaximumRange());
          meta.put("version", sensor.getVersion());
          meta.put("resolution", sensor.getResolution());

          mSensorInfo.put(type, meta);
        }

        // TODO add audio, wifi, and bluetooth info
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    mDataMap = new HashMap<String, BlockingQueue<JSONObject>>();
  }

  public void add(final String type, final JSONObject data) {
    if (!mDataMap.containsKey(type)) {
      mDataMap.put(type, new LinkedBlockingQueue<JSONObject>());
    }

    if ("semloc".equals(type)) {
      addSemLoc(data);
    } else if ("audio".equals(type)) {
    }
  }

  public JSONObject getAll() {
    JSONObject data = new JSONObject();
    // add "device" and data
    try {
      data.put("device", mDeviceInfo);
      data.put("sensormeta", mSensorInfo);
      Iterator<Entry<String, BlockingQueue<JSONObject>>> it = mDataMap
          .entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, BlockingQueue<JSONObject>> entry = it.next();
        String type = entry.getKey();
        BlockingQueue<JSONObject> eventQ = entry.getValue();
        JSONArray eventArr = new JSONArray();
        for (JSONObject event : eventQ) {
          eventArr.put(event);
        }
        data.put(type, eventArr);
      }

      // Generate copy of data, rather than references
      final String dataStr = data.toString();
      data = new JSONObject(dataStr);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return data;
  }

  public void clear() {
    Iterator<Entry<String, BlockingQueue<JSONObject>>> it = mDataMap.entrySet()
        .iterator();
    while (it.hasNext()) {
      Map.Entry<String, BlockingQueue<JSONObject>> entry = it.next();
      BlockingQueue<JSONObject> eventQ = entry.getValue();
      eventQ.clear();
    }
  }

  private void addSemLoc(final JSONObject semloc) {
    final BlockingQueue<JSONObject> queue = mDataMap.get("semloc");
    final Long epoch = System.currentTimeMillis();

    try {
      final Iterator<?> dataIter = semloc.keys();
      while (dataIter.hasNext()) {
        final JSONObject event = new JSONObject();
        final String sem = (String) dataIter.next();

        event.put("epoch", epoch);
        event.put("semantic", sem);
        event.put("location", semloc.getString(sem));

        queue.add(event);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
