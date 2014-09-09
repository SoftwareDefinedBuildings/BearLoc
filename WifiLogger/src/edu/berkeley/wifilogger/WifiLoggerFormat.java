/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 */

package edu.berkeley.wifilogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.util.Pair;

public class WifiLoggerFormat {

    private final JSONObject mDeviceInfo;
    private final JSONObject mSensorInfo;

    public WifiLoggerFormat(final Context context) {
        mDeviceInfo = WifiLoggerFormat.getDeviceInfo(context);
        mSensorInfo = WifiLoggerFormat.getSensorInfo(context);
    }

    public JSONObject dump(
            final Map<String, List<Pair<Object, JSONObject>>> dataMap) {
        final JSONObject dumpObj = new JSONObject();
        // add "device" and data
        try {
            final Iterator<Entry<String, List<Pair<Object, JSONObject>>>> it = dataMap
                    .entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<String, List<Pair<Object, JSONObject>>> entry = it
                        .next();
                final String type = entry.getKey();
                final List<Pair<Object, JSONObject>> list = entry.getValue();

                final JSONArray eventArr = new JSONArray();
                for (final Pair<Object, JSONObject> event : list) {
                    final Object data = event.first;
                    final JSONObject meta = event.second;
                    final JSONArray formated = WifiLoggerFormat.format(type, data,
                            meta);
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
                dumpObj.put("sensormeta", mSensorInfo);
            }
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dumpObj;
    }

    public static JSONObject getDeviceInfo(final Context context) {
        final JSONObject deviceInfo = new JSONObject();
        try {
            // Device Info
            deviceInfo.put("uuid", DeviceUUID.getDeviceUUID(context));
            deviceInfo.put("make", Build.MANUFACTURER);
            deviceInfo.put("model", Build.MODEL);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return deviceInfo;
    }

    // Only call getMinDelay() before Gingerbread
    @SuppressLint("NewApi")
    private static JSONObject getSensorInfo(final Context context) {
        final JSONObject sensorInfo = new JSONObject();
        try {
            // Sensor Info
            final SensorManager sensorMgr = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            final List<Sensor> sensorList = sensorMgr
                    .getSensorList(Sensor.TYPE_ALL);
            final Iterator<Sensor> iterator = sensorList.iterator();
            while (iterator.hasNext()) {
                final Sensor sensor = iterator.next();

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
                    final JSONObject meta = new JSONObject();
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
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sensorInfo;
    }

    private static JSONArray format(final String type, final Object data,
            final JSONObject meta) {
        if ("wifi".equals(type)) {
            return WifiLoggerFormat.formatWifi(data, meta);
        }

        return null;
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
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return to;
    }
}
