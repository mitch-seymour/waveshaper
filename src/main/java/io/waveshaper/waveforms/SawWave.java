package io.waveshaper.waveforms;

/**
 * A saw wave generator
 *
 * <pre>▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁</pre>
 */
public class SawWave implements Waveform {
  @Override
  public Amplitude getAmplitude(double step) {
    double value = 2 * (step - Math.round(step));
    return new Amplitude(value);
  }
}
