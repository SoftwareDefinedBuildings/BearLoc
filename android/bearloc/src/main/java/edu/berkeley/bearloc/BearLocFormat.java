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
 * Author: Siyuan He <siyuanhe@berkeley.edu>
 */

package edu.berkeley.bearloc;

import android.hardware.SensorEvent;
import android.location.Location;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;
import android.net.wifi.ScanResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BearLocFormat {

    public static JSONObject format(final String type, final Object data,
                                    final JSONObject meta) {
        if ("semloc".equals(type)) {
            return BearLocFormat.formatSemLoc(data, meta);
        } else if ("wifi".equals(type)) {
            return BearLocFormat.formatWifi(data, meta);
        } else if ("audio".equals(type)) {
            return BearLocFormat.formatAudio(data, meta);
        } else if ("geoloc".equals(type)) {
            return BearLocFormat.formatGeoLoc(data, meta);
        } else if ("acc".equals(type)) {
            return BearLocFormat.formatAcc(data, meta);
        } else if ("lacc".equals(type)) {
            return BearLocFormat.formatLinearAcc(data, meta);
        } else if ("gravity".equals(type)) {
            return BearLocFormat.formatGravity(data, meta);
        } else if ("gyro".equals(type)) {
            return BearLocFormat.formatGyro(data, meta);
        } else if ("rotation".equals(type)) {
            return BearLocFormat.formatRotation(data, meta);
        } else if ("magnetic".equals(type)) {
            return BearLocFormat.formatMagnetic(data, meta);
        } else if ("light".equals(type)) {
            return BearLocFormat.formatLight(data, meta);
        } else if ("temp".equals(type)) {
            return BearLocFormat.formatTemp(data, meta);
        } else if ("pressure".equals(type)) {
            return BearLocFormat.formatPressure(data, meta);
        } else if ("proximity".equals(type)) {
            return BearLocFormat.formatProximity(data, meta);
        } else if ("humidity".equals(type)) {
            return BearLocFormat.formatHumidity(data, meta);
        }

        return null;
    }

    private static JSONObject formatSemLoc(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final JSONObject from = (JSONObject) data;
        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("country", from.optString("country", null));
            event.put("state", from.optString("state", null));
            event.put("city", from.optString("city", null));
            event.put("street", from.optString("street", null));
            event.put("district", from.optString("district", null));
            event.put("building", from.optString("building", null));
            event.put("floor", from.optString("floor", null));
            event.put("room", from.optString("room", null));

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatWifi(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final ScanResult from = (ScanResult) data;

        try {

            event.put("uuid", meta.getString("uuid"));
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("BSSID", from.BSSID);
            event.put("SSID", from.SSID);
            event.put("capability", from.capabilities);
            event.put("frequency", from.frequency);
            event.put("RSSI", from.level);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatAudio(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final JSONArray from = (JSONArray) data;

        try {

            final String source = (meta.getInt("source") == AudioSource.CAMCORDER) ? "CAMCORDER"
                    : "MIC";
            final int channel = (meta.getInt("channel") == AudioFormat.CHANNEL_IN_MONO) ? 1
                    : 2;
            final int sampwidth = (meta.getInt("encoding") == AudioFormat.ENCODING_PCM_16BIT) ? 2
                    : 1;
            final int nframes = from.length() / (sampwidth * channel);
            event.put("source", source);
            event.put("channel", channel);
            event.put("sampwidth", sampwidth);
            event.put("nframes", nframes);
            event.put("raw", from);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatGeoLoc(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final Location from = (Location) data;

        try {
            event.put("epoch", from.getTime());
            event.put("accuracy", from.getAccuracy());
            event.put("altitude", from.getAltitude());
            event.put("bearing", from.getBearing());
            event.put("latitude", from.getLatitude());
            event.put("longitude", from.getLongitude());
            event.put("provider", from.getProvider());
            event.put("speed", from.getSpeed());

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatAcc(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("x", from.values[0]);
            event.put("y", from.values[1]);
            event.put("z", from.values[2]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatLinearAcc(final Object data,
                                             final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("x", from.values[0]);
            event.put("y", from.values[1]);
            event.put("z", from.values[2]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatGravity(final Object data,
                                           final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("x", from.values[0]);
            event.put("y", from.values[1]);
            event.put("z", from.values[2]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatGyro(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("x", from.values[0]);
            event.put("y", from.values[1]);
            event.put("z", from.values[2]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatRotation(final Object data,
                                            final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
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

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatMagnetic(final Object data,
                                            final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("x", from.values[0]);
            event.put("y", from.values[1]);
            event.put("z", from.values[2]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatLight(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {

            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("light", from.values[0]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatTemp(final Object data, final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {

            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("temp", from.values[0]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatPressure(final Object data,
                                            final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("pressure", from.values[0]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatProximity(final Object data,
                                             final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {
            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("proximity", from.values[0]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }

    private static JSONObject formatHumidity(final Object data,
                                            final JSONObject meta) {
        final JSONObject event = new JSONObject();
        final SensorEvent from = (SensorEvent) data;

        try {

            event.put("epoch", meta.getLong("epoch"));
            event.put("sysnano", meta.getLong("sysnano"));
            event.put("eventnano", from.timestamp);
            event.put("humidity", from.values[0]);
            event.put("accuracy", from.accuracy);

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return event;
    }
}
