package edu.berkeley.boss.ambience;

import org.json.JSONObject;

public interface Ambience {

  public abstract void resume();

  public abstract void pause();

  public abstract void clear();

  public abstract JSONObject get();
}
