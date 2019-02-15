package io.waveshaper.syncable;

public class SignalLogger implements Syncable {
  @Override
  public synchronized void onSignalChange(double value) {
    System.out.println(value);
  }
}
