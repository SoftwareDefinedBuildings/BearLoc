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

package edu.berkeley.bearloc.sampler;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.util.SamplerSettings;

public class GeoLoc implements Sampler, LocationListener {
    private boolean mBusy;
    private int mSampleCap;
    private int nSampleNum;

    private final Context mContext;
    private final SamplerListener mListener;
    private final Handler mHandler;
    private final LocationManager mLocManager;

    public static interface SamplerListener {
        public abstract void onGeoLocEvent(Location location);
    }

    private final Runnable mPauseTask = new Runnable() {
        @Override
        public void run() {
            pause();
        }
    };

    public GeoLoc(final Context context, final SamplerListener listener) {
        mContext = context;
        mListener = listener;
        mHandler = new Handler();
        mLocManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean start() {
        if (mBusy == false && SamplerSettings.getGeoLocEnable(mContext) == true) {
            if (mLocManager == null) {
                SamplerSettings.setGeoLocEnable(mContext, false);
                Toast.makeText(mContext, R.string.bearloc_geoloc_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            final long duration = SamplerSettings.getGeoLocDuration(mContext);
            final int num = SamplerSettings.getGeoLocCnt(mContext);
            final int minDelay = SamplerSettings.getGeoLocMinDelay(mContext);
            final float minDist = SamplerSettings.getGeoLocMinDist(mContext);
            nSampleNum = 0;
            mSampleCap = num;
            // TODO get last know geoloc
            try {
                mLocManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minDelay, minDist,
                        this);
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            }
            try {
                mLocManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minDelay, minDist, this);
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(mPauseTask, duration);
            mBusy = true;
            return true;
        } else {
            return false;
        }
    }

    private void pause() {
        if (mBusy == true) {
            // If no geoloc returned, then return the last know ones
            if (nSampleNum == 0) {
                try {
                    final Location location = mLocManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (location != null && mListener != null) {
                        mListener.onGeoLocEvent(location);
                    }
                } catch (final IllegalArgumentException e) {
                    e.printStackTrace();
                }
                try {
                    final Location location = mLocManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location != null && mListener != null) {
                        mListener.onGeoLocEvent(location);
                    }
                } catch (final IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            mBusy = false;
            mLocManager.removeUpdates(this);
            mHandler.removeCallbacks(mPauseTask);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (location == null) {
            return;
        }

        if (mListener != null) {
            mListener.onGeoLocEvent(location);
        }

        nSampleNum++;
        if (nSampleNum >= mSampleCap) {
            pause();
        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(final String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(final String provider, final int status,
            final Bundle extras) {
        // TODO Auto-generated method stub

    }
}
