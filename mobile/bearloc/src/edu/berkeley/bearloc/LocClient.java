package edu.berkeley.bearloc;

import org.json.JSONObject;

public interface LocClient {
  public abstract boolean localize(final LocClientListener listener);

  // report semloc data
  public abstract boolean report(final JSONObject semloc);

}
