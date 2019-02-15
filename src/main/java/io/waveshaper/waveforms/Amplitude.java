package io.waveshaper.waveforms;

/** Immutable object for storing the amplitude of a waveform. */
public class Amplitude {
  private final double value;

  /**
   * Constructor
   *
   * @param value a value representing the height of a waveform
   */
  public Amplitude(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.format("%.5f", value);
  }

  /**
   * Get the raw amplitude value
   *
   * @return a raw amplitude value
   */
  public double getValue() {
    return value;
  }

  /**
   * Get the amplitude value, scaled to the provided range
   *
   * @param min the minimum range of values
   * @param max the maximum range of values
   * @return an amplitude value, scalued to the provided range
   */
  public double getValue(double min, double max) {
    return ((max - min) * value + max + min) / 2;
  }
}
