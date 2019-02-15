package io.waveshaper.waveforms;

/**
 * A sine wave generator
 *
 * <pre>▆██▆▄▂▁▁▂▄▆██▆▄▂▁▁▂▄▆██▆▄▂</pre>
 */
public class SineWave implements Waveform {
  @Override
  public Amplitude getAmplitude(double step) {
    double value = Math.sin(Math.toRadians(step * 360));
    return new Amplitude(value);
  }
}
