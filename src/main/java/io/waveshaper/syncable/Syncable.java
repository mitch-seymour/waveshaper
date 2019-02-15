package io.waveshaper.syncable;

/** An interface for implementing syncable output devices. */
public interface Syncable {
  public void onSignalChange(double value);
}
