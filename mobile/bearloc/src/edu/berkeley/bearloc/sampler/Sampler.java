package edu.berkeley.bearloc.sampler;

public interface Sampler {
  // duration in millisecond
  public abstract boolean start(Integer period, Integer num);
}
