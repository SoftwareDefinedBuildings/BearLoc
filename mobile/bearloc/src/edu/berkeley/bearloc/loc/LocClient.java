package edu.berkeley.bearloc.loc;

import org.json.JSONObject;

public interface LocClient {
  // TODO design the location data structure, it may be (country, state, city,
  // district, street, building, floor, ((semantic, zone), ...), confidence).
  // But in the server, there may be various possible country, state, city,
  // street, number, building, and floor for the user
  public abstract boolean localize();
  
  public abstract boolean report(JSONObject loc);

}
