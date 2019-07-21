package io.waveshaper.waveforms;

/**
 * A reversed saw wave generator
 *
 * <pre>█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁</pre>
 */
public class ReverseSawWave extends SawWave {
  @Override
  public Amplitude getAmplitude(double step) {
    double value = super.getAmplitude(step).getValue() * -1;
    return new Amplitude(value);
  }
}
