package io.waveshaper.waveforms;

/**
 * A sine wave generator
 *
 * <pre>▆██▆▄▂▁▁▂▄▆██▆▄▂▁▁▂▄▆██▆▄▂</pre>
 */
public class SineWave implements Waveform {
  @Override
  public Amplitude getAmplitude(double step) {
    // calculate the angle with a 90 degree phase shift
    double angle = (step * 360.0) + 90.0;
    double value = Math.sin(Math.toRadians(angle % 360));
    return new Amplitude(value);
  }
}
