package edu.berkeley.boss.ambience;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class SynAmbience implements Ambience {

  private final List<Ambience> mAmbienceList;

  public SynAmbience(Context context) {
    final Sensors sensors = new Sensors(context);
    final Audio audio = new Audio();
    final Wifi wifi = new Wifi(context);
    final Locations location = new Locations(context);
    final Bluetooth bluetooth = new Bluetooth();

    mAmbienceList = new LinkedList<Ambience>();

    mAmbienceList.add(sensors);
    mAmbienceList.add(audio);
    mAmbienceList.add(wifi);
    mAmbienceList.add(location);
    mAmbienceList.add(bluetooth);
  }

  @Override
  public void resume() {
    final Iterator<Ambience> iterator = mAmbienceList.iterator();
    while (iterator.hasNext()) {
      final Ambience ambience = iterator.next();
      ambience.resume();
    }
  }

  @Override
  public void pause() {
    final Iterator<Ambience> iterator = mAmbienceList.iterator();
    while (iterator.hasNext()) {
      final Ambience ambience = iterator.next();
      ambience.pause();
    }
  }

  @Override
  public void clear() {
    final Iterator<Ambience> iterator = mAmbienceList.iterator();
    while (iterator.hasNext()) {
      final Ambience ambience = iterator.next();
      ambience.clear();
    }
  }

  @Override
  public JSONObject get() {
    final JSONObject synAmbiencePack = new JSONObject();

    final Iterator<Ambience> ambienceIter = mAmbienceList.iterator();
    while (ambienceIter.hasNext()) {
      final Ambience ambience = ambienceIter.next();
      try {
        final JSONObject dataPack = ambience.get();
        if (dataPack != null) {
          final Iterator<?> dataIter = dataPack.keys();
          while (dataIter.hasNext()) {
            String key = (String) dataIter.next();
            synAmbiencePack.put(key, dataPack.get(key));
          }
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return synAmbiencePack;
  }
}