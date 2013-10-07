package edu.berkeley.bearloc.sampler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

public class Magnetic implements Sampler, SensorEventListener {

  private boolean mBusy;
  private Integer mSampleCap;
  private Integer nSampleNum;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final SensorManager mSensorManager;
  private final Sensor mMag;

  public static interface SamplerListener {
    public abstract void onMagneticEvent(SensorEvent event);
  }

  private final Runnable mPauseTask = new Runnable() {
    @Override
    public void run() {
      pause();
    }
  };

  public Magnetic(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mSensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
    mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
  }

  @Override
  public boolean start(Integer period, Integer num) {
    if (mBusy == false && mMag != null) {
      mBusy = true;
      nSampleNum = 0;
      mSampleCap = num;
      mSensorManager.registerListener(this, mMag,
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
      mListener.onMagneticEvent(event);
    }

    nSampleNum++;
    if (nSampleNum >= mSampleCap) {
      pause();
    }
  }
}
