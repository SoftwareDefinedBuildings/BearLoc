package edu.berkeley.bearloc.loc;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;

import edu.berkeley.bearloc.sampler.Audio;
import edu.berkeley.bearloc.sampler.GeoLoc;
import edu.berkeley.bearloc.sampler.Wifi;

public class BearLocSampler {

  private OnSampleEventListener mListener;

  private final Wifi mWifi;
  private final Audio mAudio;
  private final GeoLoc mGeoLoc;

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
  }

  public void sample() {
    mWifi.start(2000, 1);
    mAudio.start(1500, 1);
    mGeoLoc.start(5000, 2);
  }
}
