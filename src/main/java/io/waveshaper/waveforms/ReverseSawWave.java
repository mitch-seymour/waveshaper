package io.waveshaper.waveforms;

/**
 * A reversed saw wave generator
 *
 * <pre>█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁</pre>
 */
public class ReverseSawWave extends SawWave {
  @Override
  public Amplitude getAmplitude(double step) {
    return super.getAmplitude(1 - step);
  }
}
