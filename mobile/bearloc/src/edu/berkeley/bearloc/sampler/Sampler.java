package edu.berkeley.bearloc.sampler;

import org.json.JSONObject;

public interface Sampler {
  public abstract JSONObject sample(Integer period);
}
