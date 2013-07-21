package edu.berkeley.boss.loc;

import org.json.JSONArray;
import org.json.JSONObject;

public interface LocClient {
  // TODO design the location data structure, it may be (country, state, city,
  // district, street, building, floor, ((semantic, zone), ...), confidence).
  // But in the server, there may be various possible country, state, city,
  // street, number, building, and floor for the user
  public abstract boolean getLocation(JSONObject sensorData);
  
  public abstract boolean reportLocation(JSONObject sensorData, JSONObject loc);

  public abstract boolean getMetadata(JSONArray target);

  public abstract boolean getMap(JSONObject metadata);

}
