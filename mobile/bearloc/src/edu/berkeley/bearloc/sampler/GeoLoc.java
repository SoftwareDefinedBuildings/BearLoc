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
