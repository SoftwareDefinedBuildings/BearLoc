package edu.berkeley.bearloc.loc;

import org.json.JSONObject;

public interface LocClient {
  public abstract boolean localize();

  // report semloc data
  public abstract boolean report(final JSONObject semloc);

}
