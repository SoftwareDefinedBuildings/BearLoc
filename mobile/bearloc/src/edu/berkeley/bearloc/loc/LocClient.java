package edu.berkeley.bearloc.loc;

import org.json.JSONObject;

public interface LocClient {
  public abstract boolean localize(LocClientListener listener);

  // report semloc data
  public abstract boolean report(final JSONObject semloc);

}
