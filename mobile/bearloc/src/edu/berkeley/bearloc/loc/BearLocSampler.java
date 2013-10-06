package edu.berkeley.bearloc.loc;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;

import edu.berkeley.bearloc.sampler.Wifi;

public class BearLocSampler {

  private OnSampleEventListener mListener;

  private Integer mActiveSamplers = 0;
  private Wifi mWifi;

  private final Handler mHandler;

  public static interface OnSampleEventListener {
    void onSampleEvent(String type, Object data);

    void onSampleDone();
  }

  public BearLocSampler(final Context context,
      final OnSampleEventListener listener) {
    mListener = listener;

    mHandler = new Handler();

    mWifi = new Wifi(context, new Wifi.SamplerListener() {
      @Override
      public void onWifiEvent(List<ScanResult> results) {
        final String type = "wifi";
        for (ScanResult result : results) {
          mListener.onSampleEvent(type, result);
        }
      }
    });
  }

  public void sample() {
    if (mWifi.start() == true) {
      final Integer period = 500;
      mActiveSamplers++;
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          mWifi.pause();
          mActiveSamplers--;
          if (mActiveSamplers == 0) {
            mListener.onSampleDone();
          }
        }
      }, period);
    }

  }
}
