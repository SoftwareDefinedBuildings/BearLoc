package edu.berkeley.bearloc.sampler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

public class Light implements Sampler, SensorEventListener {

  private boolean mBusy;
  private Integer mSampleCap;
  private Integer nSampleNum;

  private final SamplerListener mListener;
  private final Handler mHandler;
  private final SensorManager mSensorManager;
  private final Sensor mLight;

  public static interface SamplerListener {
    public abstract void onLightEvent(SensorEvent event);
  }

  private final Runnable mPauseTask = new Runnable() {
    @Override
    public void run() {
      pause();
    }
  };

  public Light(Context context, SamplerListener listener) {
    mListener = listener;
    mHandler = new Handler();
    mSensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
    mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
  }

  @Override
  public boolean start(Integer period, Integer num) {
    if (mBusy == false && mLight != null) {
      mBusy = true;
      nSampleNum = 0;
      mSampleCap = num;
      mSensorManager.registerListener(this, mLight,
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
    if (event == null) {
      return;
    }

    if (mListener != null) {
      mListener.onLightEvent(event);
    }

    nSampleNum++;
    if (nSampleNum >= mSampleCap) {
      pause();
    }
  }
}
