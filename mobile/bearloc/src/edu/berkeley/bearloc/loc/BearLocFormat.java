package edu.berkeley.bearloc.loc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.util.DeviceUUIDFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;
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
      final Map<String, List<Pair<Object, JSONObject>>> dataMap) {
    final JSONObject dumpObj = new JSONObject();
    // add "device" and data
    try {
      Iterator<Entry<String, List<Pair<Object, JSONObject>>>> it = dataMap
          .entrySet().iterator();
      while (it.hasNext()) {
        final Map.Entry<String, List<Pair<Object, JSONObject>>> entry = it
            .next();
        final String type = entry.getKey();
        final List<Pair<Object, JSONObject>> queue = entry.getValue();

        final JSONArray eventArr = new JSONArray();
        for (Pair<Object, JSONObject> event : queue) {
          final Object data = event.first;
          final JSONObject meta = event.second;
          final JSONArray formated = format(type, data, meta);
          for (int i = 0; i < formated.length(); i++) {
            eventArr.put(formated.get(i));
          }
        }

        if (eventArr.length() > 0) {
          dumpObj.put(type, eventArr);
        }
      }

      if (dumpObj.length() > 0) {
        dumpObj.put("device", mDeviceInfo);
        dumpObj.put("meta", mSensorInfo);
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
      final JSONObject meta) {
    if ("semloc".equals(type)) {
      return formatSemLoc(data, meta);
    } else if ("wifi".equals(type)) {
      return formatWifi(data, meta);
    } else if ("audio".equals(type)) {
      return formatAudio(data, meta);
    } else if ("geoloc".equals(type)) {
      return formatGeoLoc(data, meta);
    } else if ("acc".equals(type)) {
      return formatAcc(data, meta);
    } else if ("lacc".equals(type)) {
      return formatLinearAcc(data, meta);
    } else if ("gravity".equals(type)) {
      return formatGravity(data, meta);
    } else if ("gyro".equals(type)) {
      return formatGyro(data, meta);
    } else if ("rotation".equals(type)) {
      return formatRotation(data, meta);
    } else if ("magnetic".equals(type)) {
      return formatMagnetic(data, meta);
    } else if ("light".equals(type)) {
      return formatLight(data, meta);
    } else if ("temp".equals(type)) {
      return formatTemp(data, meta);
    } else if ("pressure".equals(type)) {
      return formatPressure(data, meta);
    } else if ("proximity".equals(type)) {
      return formatProximity(data, meta);
    } else if ("humidity".equals(type)) {
      return formatHumidity(data, meta);
    }

    return null;
  }

  private static JSONArray formatSemLoc(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final JSONObject from = (JSONObject) data;
    try {
      final Iterator<?> it = from.keys();
      while (it.hasNext()) {
        final JSONObject event = new JSONObject();
        final String sem = (String) it.next();
        event.put("epoch", meta.getLong("epoch"));
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

  private static JSONArray formatWifi(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final ScanResult from = (ScanResult) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
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

  private static JSONArray formatAudio(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final JSONObject from = (JSONObject) data;

    try {
      final JSONObject event = from;
      final String source = (from.getInt("source") == AudioSource.CAMCORDER) ? "CAMCORDER"
          : "MIC";
      final int channel = (from.getInt("channel") == AudioFormat.CHANNEL_IN_MONO) ? 1
          : 2;
      final int sampwidth = (from.getInt("sampwidth") == AudioFormat.ENCODING_PCM_16BIT) ? 2
          : 1;
      final int nframes = event.getJSONArray("raw").length()
          / (event.getInt("sampwidth") * event.getInt("channel"));
      event.put("source", source);
      event.put("channel", channel);
      event.put("sampwidth", sampwidth);
      event.put("nframes", nframes);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatGeoLoc(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final Location from = (Location) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("accuracy", from.getAccuracy());
      event.put("altitude", from.getAltitude());
      event.put("bearing", from.getBearing());
      event.put("latitude", from.getLatitude());
      event.put("longitude", from.getLongitude());
      event.put("provider", from.getProvider());
      event.put("speed", from.getSpeed());

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatAcc(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("x", from.values[0]);
      event.put("y", from.values[1]);
      event.put("z", from.values[2]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatLinearAcc(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("x", from.values[0]);
      event.put("y", from.values[1]);
      event.put("z", from.values[2]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatGravity(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("x", from.values[0]);
      event.put("y", from.values[1]);
      event.put("z", from.values[2]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatGyro(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("x", from.values[0]);
      event.put("y", from.values[1]);
      event.put("z", from.values[2]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatRotation(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("xr", from.values[0]);
      event.put("yr", from.values[1]);
      event.put("zr", from.values[2]);
      if (from.values.length >= 4) {
        event.put("cos", from.values[3]);
      }
      if (from.values.length >= 5) {
        event.put("head_accuracy", from.values[4]);
      }
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatMagnetic(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("x", from.values[0]);
      event.put("y", from.values[1]);
      event.put("z", from.values[2]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatLight(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("light", from.values[0]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatTemp(final Object data, final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("temp", from.values[0]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatPressure(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("pressure", from.values[0]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatProximity(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("proximity", from.values[0]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONArray formatHumidity(final Object data,
      final JSONObject meta) {
    final JSONArray to = new JSONArray();
    final SensorEvent from = (SensorEvent) data;

    try {
      final JSONObject event = new JSONObject();
      event.put("epoch", meta.getLong("epoch"));
      event.put("sysnano", meta.getLong("sysnano"));
      event.put("eventnano", from.timestamp);
      event.put("humidity", from.values[0]);
      event.put("accuracy", from.accuracy);

      to.put(event);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }
}
