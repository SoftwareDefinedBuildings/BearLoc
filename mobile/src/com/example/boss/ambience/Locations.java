package com.example.boss.ambience;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Locations implements Ambience, LocationListener {

  private static final long LOCATION_UPDATE_ITVL = 0L; // millisecond
  private static final float LOCATION_UPDATE_DIST = 0F; // meter
  private static final long LOCATION_HISTORY_LEN = 1000000L; // millisecond

  private final Context mContext;

  private final LocationManager mLocationManager;
  private final Queue<Location> mLocationEventQueue;

  public Locations(Context context) {
    mContext = context;

    mLocationManager = (LocationManager) mContext
        .getSystemService(Context.LOCATION_SERVICE);
    mLocationEventQueue = new LinkedList<Location>();
  }

  @Override
  public void onLocationChanged(Location location) {
    mLocationEventQueue.add(location);

    final Long curTimestamp = System.currentTimeMillis();
    while (mLocationEventQueue.isEmpty() == false) {
      final Long eventTimestamp = mLocationEventQueue.element().getTime();
      if ((curTimestamp - eventTimestamp) > LOCATION_HISTORY_LEN) {
        // Remove old data
        mLocationEventQueue.remove();
      } else {
        break;
      }
    }
  }

  @Override
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
        LOCATION_UPDATE_ITVL, LOCATION_UPDATE_DIST, this);
    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        LOCATION_UPDATE_ITVL, LOCATION_UPDATE_DIST, this);
  }

  @Override
  public void pause() {
    mLocationManager.removeUpdates(this);
  }

  @Override
  public void clear() {
    mLocationEventQueue.clear();
  }

  @Override
  public JSONObject get() {
    final JSONObject locationPack = new JSONObject();

    try {
      // Pack cached WiFi data
      final JSONObject locationJSONObject = new JSONObject();
      locationPack.put("location", locationJSONObject);

      locationJSONObject.put("name", "location");
      locationJSONObject.put("type", "location");

      final JSONArray eventJSONArray = new JSONArray();
      locationJSONObject.put("events", eventJSONArray);

      for (Location event : mLocationEventQueue) {
        final JSONObject eventJSONObject = new JSONObject();
        eventJSONArray.put(eventJSONObject);

        final Long timestamp = event.getTime();
        eventJSONObject.put("timestamp", timestamp);

        eventJSONObject.put("accuracy", event.getAccuracy());
        eventJSONObject.put("altitude", event.getAltitude());
        eventJSONObject.put("bearing", event.getBearing());
        eventJSONObject.put("latitude", event.getLatitude());
        eventJSONObject.put("longtitude", event.getLongitude());
        eventJSONObject.put("provider", event.getProvider());
        eventJSONObject.put("speed", event.getSpeed());
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    return locationPack;
  }
}
