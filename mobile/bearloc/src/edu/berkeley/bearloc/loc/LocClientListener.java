package edu.berkeley.bearloc.loc;

import org.json.JSONObject;

public interface LocClientListener {
  public abstract void onLocationReturned(JSONObject locInfo);
}
