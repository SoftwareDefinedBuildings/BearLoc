package edu.berkeley.bearloc.sampler;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class Wifi implements Sampler {

  private static final long WIFI_SAMPLE_ITVL = 1000L; // millisecond

  private boolean mBusy;
  private Integer mSampleCap;
  private Integer nSampleNum;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final WifiManager mWifiManager;

  public static interface SamplerListener {
    public abstract void onWifiEvent(List<ScanResult> results);
  }

  private final Runnable mWifiScanTask = new Runnable() {
    @Override
    public void run() {
      scan();
    }
  };

  private final Runnable mPauseTask = new Runnable() {
    @Override
    public void run() {
      pause();
    }
  };

  public Wifi(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }

  @Override
  public boolean start(Integer period, Integer num) {
    if (mBusy == false && mWifiManager != null) {
      mBusy = true;
      nSampleNum = 0;
      mSampleCap = num;
      mHandler.postDelayed(mWifiScanTask, 0);
      mHandler.postDelayed(mPauseTask, period);
      return true;
    } else {
      return false;
    }
  }

  private void pause() {
    if (mBusy == true) {
      mBusy = false;
      mHandler.removeCallbacks(mWifiScanTask);
      mHandler.removeCallbacks(mPauseTask);
    }
  }

  private void scan() {
    final List<ScanResult> results = mWifiManager.getScanResults();

    if (results == null) {
      return;
    }

    if (mListener != null) {
      mListener.onWifiEvent(results);
    }

    nSampleNum++;
    if (nSampleNum < mSampleCap) {
      mHandler.postDelayed(mWifiScanTask, WIFI_SAMPLE_ITVL);
    } else {
      pause();
    }
  }
}
