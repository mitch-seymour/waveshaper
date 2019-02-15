package io.waveshaper.syncable;

import com.google.common.util.concurrent.RateLimiter;
import io.waveshaper.waveforms.Oscillator;
import java.time.Duration;
import java.time.Instant;

public class WaveformRateLimiter implements Syncable {
  private final Oscillator osc;
  private final RateLimiter limiter;
  private int totalAcquired = 0;
  private Instant start = Instant.now();

  public static WaveformRateLimiter create(Oscillator osc) {
    return new WaveformRateLimiter(osc);
  }

  private WaveformRateLimiter(Oscillator osc_) {
    osc = osc_;
    limiter = RateLimiter.create(1);
    osc.syncWith(this);
    osc.startIfNotRunning();
  }

  @Override
  public synchronized void onSignalChange(double value) {
    // the minimum rate is 1
    synchronized (this) {
      // log the previous rate + events per second (eps)
      Instant finished = Instant.now();
      double elapsed = Duration.between(start, finished).toMillis();
      double eps = totalAcquired / elapsed * 1000;
      // System.out.printf("%.1f ", eps);
      double currentRate = limiter.getRate();
      System.out.printf(
          "previous rate: %.1f, eps: %.1f, diff: %.1f\n", currentRate, eps, currentRate - eps);
      // set the new rate and reset the properties for calculating eps
      limiter.setRate(value);
      totalAcquired = 0;
      start = Instant.now();
    }
  }

  public double acquire() {
    return acquire(1);
  }

  public double acquire(int permits) {
    double acquired = limiter.acquire(permits);
    synchronized (this) {
      totalAcquired += permits;
    }
    return acquired;
  }

  public boolean tryAcquire() {
    return tryAcquire(1);
  }

  public boolean tryAcquire(int permits) {
    boolean acquired = limiter.tryAcquire(permits);
    if (acquired) {
      synchronized (this) {
        totalAcquired += permits;
      }
    }
    return acquired;
  }

  public boolean updating() {
    return osc.running() && osc.hasNext();
  }
}
