package io.waveshaper.waveforms;

/**
 * A square wave generator
 *
 * <pre>▁▁█████▁▁▁▁▁█████▁▁▁▁▁█████▁▁</pre>
 */
public class SquareWave implements Waveform {
  @Override
  public Amplitude getAmplitude(double step) {
    double value = step < 0.5 ? 1 : -1;
    return new Amplitude(value);
  }
}
