package edu.berkeley.bearloc.sampler;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class Wifi implements Sampler {

  private static final long WIFI_SAMPLE_ITVL = 100L; // millisecond

  private boolean mBusy;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final WifiManager mWifiManager;

  public static interface SamplerListener {
    public abstract void onWifiEvent(List<ScanResult> results);
  }

  private final Runnable mWifiScanTimeTask = new Runnable() {
    public void run() {
      final List<ScanResult> results = mWifiManager.getScanResults();
      // TODO check return result
      if (mListener != null) {
        mListener.onWifiEvent(results);
      }
      mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
    }
  };

  public Wifi(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }

  @Override
  public boolean start() {
    if (mBusy == false) {
      mBusy = true;
      mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean pause() {
    if (mBusy == true) {
      mBusy = false;
      mHandler.removeCallbacks(mWifiScanTimeTask);
      return true;
    } else {
      return false;
    }
  }
}