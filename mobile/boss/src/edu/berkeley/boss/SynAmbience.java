package edu.berkeley.boss;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.boss.ambience.Ambience;
import edu.berkeley.boss.ambience.Audio;
import edu.berkeley.boss.ambience.Locations;
import edu.berkeley.boss.ambience.Sensors;
import edu.berkeley.boss.ambience.Wifi;

import android.content.Context;

public class SynAmbience implements Ambience {

  private final Sensors mSensors;
  private final Audio mAudio;
  private final Wifi mWifi;
  private final Locations mLocation;

  public SynAmbience(Context context) {

    mSensors = new Sensors(context);
    mAudio = new Audio();
    mWifi = new Wifi(context);
    mLocation = new Locations(context);
  }

  @Override
  public void resume() {
    mSensors.resume();
    mAudio.resume();
    mWifi.resume();
    mLocation.resume();
  }

  @Override
  public void pause() {
    mSensors.pause();
    mAudio.pause();
    mWifi.pause();
    mLocation.pause();
  }

  @Override
  public void clear() {
    mSensors.clear();
    mAudio.clear();
    mWifi.clear();
    mLocation.clear();
  }

  @Override
  public JSONObject get() {
    final JSONObject synAmbiencePack = new JSONObject();

    try {
      final JSONObject sensorPack = mSensors.get();
      final Iterator<?> iter = sensorPack.keys();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        synAmbiencePack.put(key, sensorPack.get(key));
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      final JSONObject audoPack = mAudio.get();
      final Iterator<?> iter = audoPack.keys();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        synAmbiencePack.put(key, audoPack.get(key));
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      final JSONObject wifiPack = mWifi.get();
      final Iterator<?> iter = wifiPack.keys();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        synAmbiencePack.put(key, wifiPack.get(key));
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      final JSONObject locationPack = mLocation.get();
      final Iterator<?> iter = locationPack.keys();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        synAmbiencePack.put(key, locationPack.get(key));
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return synAmbiencePack;
  }
}