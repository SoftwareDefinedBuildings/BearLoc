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

package edu.berkeley.bearloc;

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

  private final OnSampleEventListener mListener;

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
      public void onWifiEvent(final List<ScanResult> results) {
        final String type = "wifi";
        for (final ScanResult result : results) {
          mListener.onSampleEvent(type, result);
        }
      }
    });

    mAudio = new Audio(new Audio.SamplerListener() {
      @Override
      public void onAudioEvent(final JSONObject audio) {
        final String type = "audio";
        mListener.onSampleEvent(type, audio);
      }
    });

    mGeoLoc = new GeoLoc(context, new GeoLoc.SamplerListener() {
      @Override
      public void onGeoLocEvent(final Location location) {
        final String type = "geoloc";
        mListener.onSampleEvent(type, location);
      }
    });

    mAcc = new Acc(context, new Acc.SamplerListener() {
      @Override
      public void onAccEvent(final SensorEvent event) {
        final String type = "acc";
        mListener.onSampleEvent(type, event);
      }
    });

    mLAcc = new LinearAcc(context, new LinearAcc.SamplerListener() {
      @Override
      public void onLinearAccEvent(final SensorEvent event) {
        final String type = "lacc";
        mListener.onSampleEvent(type, event);
      }
    });

    mGravity = new Gravity(context, new Gravity.SamplerListener() {
      @Override
      public void onGravityEvent(final SensorEvent event) {
        final String type = "gravity";
        mListener.onSampleEvent(type, event);
      }
    });

    mGyro = new Gyro(context, new Gyro.SamplerListener() {
      @Override
      public void onGyroEvent(final SensorEvent event) {
        final String type = "gyro";
        mListener.onSampleEvent(type, event);
      }
    });

    mRotation = new Rotation(context, new Rotation.SamplerListener() {
      @Override
      public void onRotationEvent(final SensorEvent event) {
        final String type = "rotation";
        mListener.onSampleEvent(type, event);
      }
    });

    mMag = new Magnetic(context, new Magnetic.SamplerListener() {
      @Override
      public void onMagneticEvent(final SensorEvent event) {
        final String type = "magnetic";
        mListener.onSampleEvent(type, event);
      }
    });

    mLight = new Light(context, new Light.SamplerListener() {
      @Override
      public void onLightEvent(final SensorEvent event) {
        final String type = "light";
        mListener.onSampleEvent(type, event);
      }
    });

    mTemp = new Temp(context, new Temp.SamplerListener() {
      @Override
      public void onTempEvent(final SensorEvent event) {
        final String type = "temp";
        mListener.onSampleEvent(type, event);
      }
    });

    mPressure = new Pressure(context, new Pressure.SamplerListener() {

      @Override
      public void onPressureEvent(final SensorEvent event) {
        final String type = "pressure";
        mListener.onSampleEvent(type, event);
      }
    });

    mProximity = new Proximity(context, new Proximity.SamplerListener() {
      @Override
      public void onProximityEvent(final SensorEvent event) {
        final String type = "proximity";
        mListener.onSampleEvent(type, event);
      }
    });

    mHumidity = new Humidity(context, new Humidity.SamplerListener() {
      @Override
      public void onHumidityEvent(final SensorEvent event) {
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
