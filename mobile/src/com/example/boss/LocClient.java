package com.example.boss;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.boss.Sensor.SensorDataPack;

public interface LocClient {
  // TODO design the location data structure, it may be (country, state, city,
  // district, street, building, floor, ((semantic, zone), ...), confidence).
  // But in the server, there may be various possible country, state, city,
  // street, number, building, and floor for the user
  public abstract boolean getLocation(SensorDataPack sensorDataPack);

  public abstract boolean getMetadata(JSONArray loc);

  public abstract boolean getMap(JSONObject metadata);

}
