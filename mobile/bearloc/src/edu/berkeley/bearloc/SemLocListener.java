package edu.berkeley.bearloc;

import org.json.JSONObject;

public interface SemLocListener {
  public abstract void onSemLocChanged(JSONObject semLocInfo);
}
