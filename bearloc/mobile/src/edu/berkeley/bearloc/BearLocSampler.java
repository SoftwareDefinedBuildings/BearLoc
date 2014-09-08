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

package edu.berkeley.bearloc;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.wifi.ScanResult;
import edu.berkeley.bearloc.sampler.Wifi;

public class BearLocSampler {

    private final Context mContext;
    private final OnSampleEventListener mListener;

    private final Wifi mWifi;

    public static interface OnSampleEventListener {
        void onSampleEvent(String type, Object data);
    }

    public BearLocSampler(final Context context,
            final OnSampleEventListener listener) {
        mContext = context;
        mListener = listener;

        mWifi = new Wifi(mContext, new Wifi.SamplerListener() {
            @Override
            public void onWifiEvent(final List<ScanResult> results) {
                final String type = "wifi";
                for (final ScanResult result : results) {
                    mListener.onSampleEvent(type, result);
                }
            }
        });
    }

    public void sample() {
        mWifi.start();
    }
}
