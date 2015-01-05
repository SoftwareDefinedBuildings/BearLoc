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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;

public class WifiLoggerFormat {

    private final Context mContext;
    private final WifiLoggerCache mCache;

    public WifiLoggerFormat(final Context context, final WifiLoggerCache cache) {
        mContext = context;
        mCache = cache;

        try {
            String type = mContext.getResources().getString(
                    R.string.device_info);
            JSONObject meta = new JSONObject();
            meta.put("type", type);
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
            JSONObject formated = format(getDeviceInfo(), meta);
            if (formated != null) {
                mCache.add(formated);
            }
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JSONObject format(final Object data, final JSONObject meta) {
        final String type = meta.optString("type");
        // Android requires compiler compliance level 5.0 or 6.0
        if (type.equals(mContext.getResources().getString(
                R.string.device_info))) {
            return formatDeviceInfo(data, meta);
        } else if (type.equals(mContext.getResources().getString(
                R.string.wifi))) {
            return formatWifi(data, meta);
        }
        return null;
    }

    private JSONObject getDeviceInfo() {
        final JSONObject deviceInfo = new JSONObject();
        try {
            // Device Info
            deviceInfo.put("make", Build.MANUFACTURER);
            deviceInfo.put("model", Build.MODEL);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return deviceInfo;
    }

    private JSONObject formatDeviceInfo(final Object data, final JSONObject meta) {
        final JSONObject to = new JSONObject();
        final JSONObject from = (JSONObject) data;
        try {
            to.put("type", meta.getString("type"));
            to.put("id", DeviceUUID.getDeviceUUID(mContext));
            to.put("epoch", meta.optLong("epoch"));
            to.put("sysnano", meta.optLong("sysnano"));
            to.put("make", from.optString("make"));
            to.put("model", from.optString("model"));
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return to;
    }

    private JSONObject formatWifi(final Object data, final JSONObject meta) {
        final JSONObject to = new JSONObject();
        final ScanResult from = (ScanResult) data;

        try {
            to.put("type", meta.getString("type"));
            to.put("id", DeviceUUID.getDeviceUUID(mContext));
            to.put("epoch", meta.getLong("epoch"));
            to.put("sysnano", meta.getLong("sysnano"));
            to.put("BSSID", from.BSSID);
            to.put("SSID", from.SSID);
            to.put("capability", from.capabilities);
            to.put("frequency", from.frequency);
            to.put("RSSI", from.level);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return to;
    }
}