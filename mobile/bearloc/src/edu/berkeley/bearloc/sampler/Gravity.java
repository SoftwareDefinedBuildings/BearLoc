package edu.berkeley.bearloc.sampler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;

public class Gravity implements Sampler, SensorEventListener {

  private boolean mBusy;
  private Integer mSampleCap;
  private Integer nSampleNum;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final SensorManager mSensorManager;
  private final Sensor mGravity;

  public static interface SamplerListener {
    public abstract void onGravityEvent(SensorEvent event);
  }

  private final Runnable mPauseTask = new Runnable() {
    @Override
    public void run() {
      pause();
    }
  };

  // get null for mGravity if not available
  @SuppressLint("InlinedApi")
  public Gravity(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mSensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    } else {
      mGravity = null;
    }
  }

  @Override
  public boolean start(Integer period, Integer num) {
    if (mBusy == false && mGravity != null) {
      mBusy = true;
      nSampleNum = 0;
      mSampleCap = num;
      mSensorManager.registerListener(this, mGravity,
          SensorManager.SENSOR_DELAY_NORMAL);
      mHandler.postDelayed(mPauseTask, period);
      return true;
    } else {
      return false;
    }
  }

  private void pause() {
    if (mBusy == true) {
      mBusy = false;
      mSensorManager.unregisterListener(this);
      mHandler.removeCallbacks(mPauseTask);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (mListener != null) {
      mListener.onGravityEvent(event);
    }

    nSampleNum++;
    if (nSampleNum >= mSampleCap) {
      pause();
    }
  }
}
