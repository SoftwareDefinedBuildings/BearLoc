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

public class GeoLoc implements Sampler, LocationListener {

  private static final long LOCATION_UPDATE_ITVL = 0L; // millisecond
  private static final float LOCATION_UPDATE_DIST = 0F; // meter

  private boolean mBusy;
  private Integer mSampleCap;
  private Integer nSampleNum;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final LocationManager mLocationManager;

  public static interface SamplerListener {
    public abstract void onGeoLocEvent(Location location);
  }

  private final Runnable mPauseTask = new Runnable() {
    @Override
    public void run() {
      pause();
    }
  };

  public GeoLoc(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mLocationManager = (LocationManager) context
        .getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public boolean start(Integer period, Integer num) {
    if (mBusy == false && mLocationManager != null) {
      try {
        mBusy = true;
        nSampleNum = 0;
        mSampleCap = num;
        mLocationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_ITVL,
            LOCATION_UPDATE_DIST, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
            LOCATION_UPDATE_ITVL, LOCATION_UPDATE_DIST, this);
        mHandler.postDelayed(mPauseTask, period);
        return true;
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  private void pause() {
    if (mBusy == true) {
      mBusy = false;
      mLocationManager.removeUpdates(this);
      mHandler.removeCallbacks(mPauseTask);
    }
  }

  @Override
  public void onLocationChanged(Location location) {
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
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }
}
