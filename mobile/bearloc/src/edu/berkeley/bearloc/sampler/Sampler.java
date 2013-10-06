package edu.berkeley.bearloc.sampler;

public interface Sampler {
  // duration in millisecond
  public abstract boolean start();

  public abstract boolean pause();
}
