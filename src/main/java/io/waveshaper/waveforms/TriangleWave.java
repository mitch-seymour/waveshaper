package io.waveshaper.waveforms;

/**
 * A triangle wave generator
 *
 * <pre>▁▂▃▅▆█▆▅▃▂▁▂▃▅▆█▆▅▃▂▁▂▃▅▆█▆▅▃▂▁</pre>
 */
public class TriangleWave implements Waveform {
  @Override
  public Amplitude getAmplitude(double step) {
    double value = 1 - 4 * Math.abs(Math.round(step) - step);
    return new Amplitude(value);
  }
}
