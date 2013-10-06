package edu.berkeley.bearloc.loc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.util.DeviceUUIDFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.util.Pair;

public class BearLocFormat {

  private final JSONObject mDeviceInfo;
  private final JSONObject mSensorInfo;

  public BearLocFormat(Context context) {
    mDeviceInfo = getDeviceInfo(context);
    mSensorInfo = getSensorInfo(context);
  }

  public JSONObject dump(
      final Map<String, BlockingQueue<Pair<Long, Object>>> dataMap) {
    final JSONObject dumpObj = new JSONObject();
    // add "device" and data
    try {
      dumpObj.put("device", mDeviceInfo);
      dumpObj.put("meta", mSensorInfo);

      Iterator<Entry<String, BlockingQueue<Pair<Long, Object>>>> it = dataMap
          .entrySet().iterator();
      while (it.hasNext()) {
        final Map.Entry<String, BlockingQueue<Pair<Long, Object>>> entry = it
            .next();
        final String type = entry.getKey();
        final BlockingQueue<Pair<Long, Object>> queue = entry.getValue();

        final JSONArray eventArr = new JSONArray();
        for (Pair<Long, Object> event : queue) {
          final Long epoch = event.first;
          final Object data = event.second;
          final JSONArray formated = format(type, data, epoch);
          for (int i = 0; i < formated.length(); i++) {
            eventArr.put(formated.get(i));
          }
        }

        if (eventArr.length() > 0) {
          dumpObj.put(type, eventArr);
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return dumpObj;
  }

  private static JSONObject getDeviceInfo(Context context) {
    final JSONObject deviceInfo = new JSONObject();
    try {
      // Device Info
      deviceInfo.put("uuid", (new DeviceUUIDFactory(context)).getDeviceUUID()
          .toString());
      deviceInfo.put("make", Build.MANUFACTURER);
      deviceInfo.put("model", Build.MODEL);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return deviceInfo;
  }

  // Only call getMinDelay() before Gingerbread
  @SuppressLint("NewApi")
  private static JSONObject getSensorInfo(Context context) {
    final JSONObject sensorInfo = new JSONObject();
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

          sensorInfo.put(type, meta);
        }

        // TODO add audio, wifi, and bluetooth info
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return sensorInfo;
  }

  private static JSONArray format(final String type, final Object data,
      final Long epoch) {
    if ("semloc".equals(type)) {
      return formatSemLoc(data, epoch);
    } else if ("wifi".equals(type)) {
      return formatWifi(data, epoch);
    } else if ("audio".equals(type)) {
      return formatAudio(data, epoch);
    }

    return null;
  }

  private static JSONArray formatSemLoc(final Object data, final Long epoch) {
    final JSONArray to = new JSONArray();
    final JSONObject from = (JSONObject) data;
    try {
      final Iterator<?> it = from.keys();
      while (it.hasNext()) {
        final JSONObject event = new JSONObject();
        final String sem = (String) it.next();
        event.put("epoch", epoch);
        event.put("semantic", sem);
        event.put("location", from.getString(sem));

        to.put(event);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatWifi(final Object data, final Long epoch) {
    final JSONArray to = new JSONArray();
    final ScanResult from = (ScanResult) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", epoch);
      event.put("BSSID", from.BSSID);
      event.put("SSID", from.SSID);
      event.put("capability", from.capabilities);
      event.put("frequency", from.frequency);
      event.put("RSSI", from.level);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatAudio(final Object data, final Long epoch) {
    final JSONArray to = new JSONArray();

    return to;
  }
}
