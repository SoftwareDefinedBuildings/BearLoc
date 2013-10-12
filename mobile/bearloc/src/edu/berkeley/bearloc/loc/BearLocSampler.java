package edu.berkeley.bearloc.loc;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.wifi.ScanResult;

import edu.berkeley.bearloc.sampler.Acc;
import edu.berkeley.bearloc.sampler.Audio;
import edu.berkeley.bearloc.sampler.GeoLoc;
import edu.berkeley.bearloc.sampler.Gravity;
import edu.berkeley.bearloc.sampler.Gyro;
import edu.berkeley.bearloc.sampler.Humidity;
import edu.berkeley.bearloc.sampler.Light;
import edu.berkeley.bearloc.sampler.LinearAcc;
import edu.berkeley.bearloc.sampler.Magnetic;
import edu.berkeley.bearloc.sampler.Pressure;
import edu.berkeley.bearloc.sampler.Proximity;
import edu.berkeley.bearloc.sampler.Rotation;
import edu.berkeley.bearloc.sampler.Temp;
import edu.berkeley.bearloc.sampler.Wifi;

public class BearLocSampler {

  private OnSampleEventListener mListener;

  private final Wifi mWifi;
  private final Audio mAudio;
  private final GeoLoc mGeoLoc;
  private final Acc mAcc;
  private final LinearAcc mLAcc;
  private final Gravity mGravity;
  private final Gyro mGyro;
  private final Rotation mRotation;
  private final Magnetic mMag;
  private final Light mLight;
  private final Temp mTemp;
  private final Pressure mPressure;
  private final Proximity mProximity;
  private final Humidity mHumidity;

  public static interface OnSampleEventListener {
    void onSampleEvent(String type, Object data);
  }

  public BearLocSampler(final Context context,
      final OnSampleEventListener listener) {
    mListener = listener;

    mWifi = new Wifi(context, new Wifi.SamplerListener() {
      @Override
      public void onWifiEvent(List<ScanResult> results) {
        final String type = "wifi";
        for (ScanResult result : results) {
          mListener.onSampleEvent(type, result);
        }
      }
    });

    mAudio = new Audio(new Audio.SamplerListener() {
      @Override
      public void onAudioEvent(JSONObject audio) {
        final String type = "audio";
        mListener.onSampleEvent(type, audio);
      }
    });

    mGeoLoc = new GeoLoc(context, new GeoLoc.SamplerListener() {
      @Override
      public void onGeoLocEvent(Location location) {
        final String type = "geoloc";
        mListener.onSampleEvent(type, location);
      }
    });

    mAcc = new Acc(context, new Acc.SamplerListener() {
      @Override
      public void onAccEvent(SensorEvent event) {
        final String type = "acc";
        mListener.onSampleEvent(type, event);
      }
    });

    mLAcc = new LinearAcc(context, new LinearAcc.SamplerListener() {
      @Override
      public void onLinearAccEvent(SensorEvent event) {
        final String type = "lacc";
        mListener.onSampleEvent(type, event);
      }
    });

    mGravity = new Gravity(context, new Gravity.SamplerListener() {
      @Override
      public void onGravityEvent(SensorEvent event) {
        final String type = "gravity";
        mListener.onSampleEvent(type, event);
      }
    });

    mGyro = new Gyro(context, new Gyro.SamplerListener() {
      @Override
      public void onGyroEvent(SensorEvent event) {
        final String type = "gyro";
        mListener.onSampleEvent(type, event);
      }
    });

    mRotation = new Rotation(context, new Rotation.SamplerListener() {
      @Override
      public void onRotationEvent(SensorEvent event) {
        final String type = "rotation";
        mListener.onSampleEvent(type, event);
      }
    });

    mMag = new Magnetic(context, new Magnetic.SamplerListener() {
      @Override
      public void onMagneticEvent(SensorEvent event) {
        final String type = "magnetic";
        mListener.onSampleEvent(type, event);
      }
    });

    mLight = new Light(context, new Light.SamplerListener() {
      @Override
      public void onLightEvent(SensorEvent event) {
        final String type = "light";
        mListener.onSampleEvent(type, event);
      }
    });

    mTemp = new Temp(context, new Temp.SamplerListener() {
      @Override
      public void onTempEvent(SensorEvent event) {
        final String type = "temp";
        mListener.onSampleEvent(type, event);
      }
    });

    mPressure = new Pressure(context, new Pressure.SamplerListener() {

      @Override
      public void onPressureEvent(SensorEvent event) {
        final String type = "pressure";
        mListener.onSampleEvent(type, event);
      }
    });

    mProximity = new Proximity(context, new Proximity.SamplerListener() {
      @Override
      public void onProximityEvent(SensorEvent event) {
        final String type = "proximity";
        mListener.onSampleEvent(type, event);
      }
    });

    mHumidity = new Humidity(context, new Humidity.SamplerListener() {
      @Override
      public void onHumidityEvent(SensorEvent event) {
        final String type = "humidity";
        mListener.onSampleEvent(type, event);
      }
    });
  }

  public void sample() {
    mWifi.start(1000, 1);
    mAudio.start(1000, 1);
    mGeoLoc.start(1000, 1);
    mAcc.start(1000, 10);
    mLAcc.start(1000, 10);
    mGravity.start(1000, 10);
    mGyro.start(1000, 10);
    mRotation.start(1000, 10);
    mMag.start(1000, 10);
    mLight.start(1000, 1);
    mTemp.start(1000, 1);
    mPressure.start(1000, 1);
    mProximity.start(1000, 1);
    mHumidity.start(1000, 1);
  }
}
