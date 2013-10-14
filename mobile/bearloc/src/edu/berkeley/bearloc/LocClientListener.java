package edu.berkeley.bearloc;

import org.json.JSONObject;

public interface LocClientListener {
  public abstract void onLocationReturned(JSONObject locInfo);
}
