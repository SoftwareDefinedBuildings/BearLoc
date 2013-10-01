package edu.berkeley.bearloc.loc;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.util.DeviceUUIDFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Build;

public class BearLocFormat {

  public static JSONObject getDeviceInfo(Context context) {
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
  public static JSONObject getSensorInfo(Context context) {
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

  public static JSONObject convert(final String type, final Object data,
      final Long epoch, final String... opt) {

    if ("semloc".equals(type)) {
      return convertSemLoc(data, epoch, opt[0]);
    } else if ("wifi".equals(type)) {
      return convertWifi(data, epoch);
    } else if ("audio".equals(type)) {
      return convertAudio(data, epoch);
    }

    return null;
  }

  private static JSONObject convertSemLoc(final Object data, final Long epoch,
      final String sem) {
    final JSONObject from = (JSONObject) data;
    final JSONObject to = new JSONObject();
    try {
      to.put("epoch", epoch);
      to.put("semantic", sem);
      to.put("location", from.getString(sem));
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONObject convertWifi(final Object data, final Long epoch) {
    final ScanResult from = (ScanResult) data;
    final JSONObject to = new JSONObject();
    try {
      to.put("epoch", epoch);
      to.put("BSSID", from.BSSID);
      to.put("SSID", from.SSID);
      to.put("capability", from.capabilities);
      to.put("frequency", from.frequency);
      to.put("RSSI", from.level);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return to;
  }

  private static JSONObject convertAudio(final Object data, final Long epoch) {
    return null;
  }
}
