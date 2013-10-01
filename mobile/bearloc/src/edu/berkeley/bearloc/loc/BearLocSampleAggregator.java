package edu.berkeley.bearloc.loc;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;

import edu.berkeley.bearloc.sampler.Wifi;

public class BearLocSampleAggregator {

  private OnSampleDoneListener mDoneListener;
  private OnSampleEventListener mEventListener;

  private Integer mActiveSamplers;
  private Wifi mWifi;

  private final Handler mHandler;

  public static interface OnSampleDoneListener {
    void onSampleDone();
  }

  public static interface OnSampleEventListener {
    void onSampleEvent(String type, JSONObject data);
  }

  public BearLocSampleAggregator(final Context context,
      final OnSampleDoneListener doneListener,
      final OnSampleEventListener eventListener) {
    mDoneListener = doneListener;
    mEventListener = eventListener;

    mHandler = new Handler();

    mWifi = new Wifi(context, new Wifi.SamplerListener() {
      @Override
      public void onWifiEvent(List<ScanResult> results) {
        final String type = "wifi";
        final Long epoch = System.currentTimeMillis();
        for (ScanResult scan : results) {
          mEventListener.onSampleEvent(type,
              BearLocFormat.convert(type, scan, epoch));
        }

      }
    });
  }

  public void sample() {
    mActiveSamplers = 0;

    mWifi.start();
    mActiveSamplers++;
    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        mWifi.pause();
        mActiveSamplers--;
        if (mActiveSamplers == 0) {
          mDoneListener.onSampleDone();
        }
      }
    }, 3000);

  }
}
