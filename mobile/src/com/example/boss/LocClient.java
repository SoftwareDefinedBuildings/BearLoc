package com.example.boss;

public interface LocClient {
  // TODO design the location data structure, it may be (country, state, city,
  // street, number, building, floor, confidence). But in the server, there may
  // be various possible country, state, city, street, number, building, and
  // floor for the user
  public abstract boolean getLocation(/* all sensor data (GPS,WiFi,Acoustic,...) */);

  public abstract boolean getMap(/* Location loc */);

  public abstract boolean getSemantic();

  public abstract boolean getMetadata(String semantic);

}
