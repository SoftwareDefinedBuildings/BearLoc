package edu.berkeley.bearloc.loc;

import org.json.JSONObject;

public interface LocClient {
  public abstract boolean localize();

  public abstract void report(final JSONObject semloc);

}
