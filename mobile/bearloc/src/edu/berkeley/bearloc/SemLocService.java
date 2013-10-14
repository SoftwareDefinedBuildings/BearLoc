package edu.berkeley.bearloc;

import org.json.JSONObject;

public interface SemLocService {
  public abstract boolean localize(final SemLocListener listener);

  // report semloc data
  public abstract boolean report(final JSONObject semloc);

}
