package io.waveshaper.waveforms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class OscillatorTest {

  public double round(double value) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(2, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  @Test
  void testSineWaveOscillation() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(SineWave::new)
            .cycles(1)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {
      1.0, 4.28, 13.25, 25.5, 37.75, 46.72, 50.0, 46.72, 37.75, 25.5, 13.25, 4.28
    };

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }

  @Test
  void testSineWaveOscillationShifted() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(SineWave::new)
            .cycles(1)
            .horizontalShift(6)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {
      50.0, 46.72, 37.75, 25.5, 13.25, 4.28, 1.0, 4.28, 13.25, 25.5, 37.75, 46.72
    };

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }

  @Test
  void testSawWaveOscillation() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(SawWave::new)
            .cycles(1)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {
      1.0, 5.08, 9.17, 13.25, 17.33, 21.42, 25.5, 29.58, 33.67, 37.75, 41.83, 45.92
    };

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }

  @Test
  void testReverseSawWaveOscillation() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(ReverseSawWave::new)
            .cycles(1)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {
      50.0, 45.92, 41.83, 37.75, 33.67, 29.58, 25.5, 21.42, 17.33, 13.25, 9.17, 5.08
    };

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }

  @Test
  void testSquareWaveOscillation() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(SquareWave::new)
            .cycles(1)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0};

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }

  @Test
  void testTriangleWaveOscillation() {
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(TriangleWave::new)
            .cycles(1)
            .sampleRate(12)
            .sampleDuration(Duration.ofSeconds(5))
            .range(1, 50)
            .build();
    Double[] expected = {
      1.0, 9.17, 17.33, 25.5, 33.67, 41.83, 50.0, 41.83, 33.67, 25.5, 17.33, 9.17
    };

    Double[] actual = osc.stream().map(this::round).toArray(Double[]::new);
    assertArrayEquals(expected, actual);
    assertFalse(osc.hasNext());
  }
}
