package io.waveshaper.waveforms;

import io.waveshaper.syncable.Syncable;
import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An Oscillator is responsible for repeating {@link io.waveshaper.waveforms.Waveform}s. This class
 * utilizes the builder pattern for constructing Oscillator instances.
 */
public class Oscillator implements Iterator<Double> {
  private final Waveform waveform;
  private final int cycles;
  private final double rangeMin;
  private final double rangeMax;
  private final int sampleRate;
  private final Duration sampleDuration;

  // used by the iterator
  private int currentPosition = 0;

  private int horizontalShift;

  // synchronized output devices for the oscillator signal
  private final HashSet<Syncable> syncables = new HashSet<>();

  private boolean running = false;

  // make the constructor private since we're using the
  // builder pattern
  private Oscillator(Builder builder) {
    this.waveform = builder.waveform;
    this.cycles = builder.cycles;
    this.rangeMin = builder.rangeMin;
    this.rangeMax = builder.rangeMax;
    this.sampleRate = builder.sampleRate;
    this.sampleDuration = builder.sampleDuration;
    this.horizontalShift = builder.horizontalShift + (int) Math.ceil((double) sampleRate / 2);
  }

  public static class Builder {
    private Waveform waveform = new SineWave();
    private int cycles = 1;
    private double rangeMin = -1;
    private double rangeMax = 1;
    private int sampleRate = 100;
    private Duration sampleDuration = Duration.ofMillis(0);
    private int horizontalShift = 0;

    public Builder waveform(Supplier<Waveform> waveformSupplier) {
      this.waveform = waveformSupplier.get();
      return this;
    }

    public Builder cycles(int cycles) {
      this.cycles = cycles;
      return this;
    }

    public Builder horizontalShift(int horizontalShift) {
      this.horizontalShift = horizontalShift;
      return this;
    }

    public Builder range(double min, double max) {
      this.rangeMin = min;
      this.rangeMax = max;
      return this;
    }

    public Builder sampleRate(int sampleRate) {
      this.sampleRate = sampleRate;
      return this;
    }

    public Builder sampleDuration(Duration sampleDuration) {
      this.sampleDuration = sampleDuration;
      return this;
    }

    public Oscillator build() {
      return new Oscillator(this);
    }
  }

  @Override
  public boolean hasNext() {
    return currentPosition < cycles * sampleRate;
  }

  @Override
  public Double next() {
    if (!hasNext()) {
      throw new RuntimeException("Tried to sample a fully sampled waveform");
    }
    double adjustedPosition = currentPosition + horizontalShift;
    double step = (double) (adjustedPosition % sampleRate) / sampleRate;
    synchronized (this) {
      currentPosition++;
    }
    return waveform.getAmplitude(step).getValue(rangeMin, rangeMax);
  }

  public Stream<Double> stream() {
    Iterable<Double> iterable = () -> this;
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  public boolean running() {
    return running;
  }

  public void startIfNotRunning() {
    synchronized (this) {
      if (!running) {
        start();
      }
    }
  }

  public void start() {
    // set the running state
    running = true;

    // Use a scheduled executor service to synchronize the current oscillator
    // signal across all output devices on a configurable, periodic schedule
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Iterate through each output device, and call the onSignalChange method.
    // WARNING: the implementation below is susceptible to head-of-line blocking
    // TODO: improve this implementation
    Runnable synchronizer =
        () -> {
          if (hasNext()) {
            // broadcast the new amplitude to all syncable outputs
            Double amplitude = next();
            for (Syncable syncable : syncables) {
              syncable.onSignalChange(amplitude);
            }
          } else {
            // finished iterating through the waveform
            scheduler.shutdown();
          }
        };

    scheduler.scheduleAtFixedRate(
        synchronizer, 0, sampleDuration.toMillis(), TimeUnit.MILLISECONDS);
  }

  public void syncWith(Syncable syncable) {
    syncables.add(syncable);
  }
}
