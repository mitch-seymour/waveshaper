package io.waveshaper.waveforms;

/** An interface for implementing waveforms. */
public interface Waveform {
  /**
   * Get the amplitude of a waveform at the provided offset.
   *
   * @param offset a number between 0 and 1, representing the distance of the current sample from
   *     the beginning of the waveform's cycle
   * @return the amplitude of the waveform at the provided offset
   */
  public Amplitude getAmplitude(double offset);
}
