package io.waveshaper.syncable;

import com.google.common.util.concurrent.RateLimiter;
import io.waveshaper.waveforms.Oscillator;
import java.time.Duration;
import java.time.Instant;

public class WaveformRateLimiter implements Syncable {
  private final Oscillator osc;
  private RateLimiter limiter;
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
      /*!
       * Update the rate limiter. Note: we initially tried using
       * limiter.setRate(value), but according to the guava Javadocs:
       *
       * "each request repays (by waiting, if necessary) the cost of the previous request"
       *
       * Since the new rate is not observed immediately, recreating the rate
       * limiter leads to more predictable and accurate rendering of the
       * underlying waveform. The following issue is of particular interest and
       * could potentially allow us to leverage limiter.setRate in the future:
       *
       * @link https://github.com/google/guava/issues/3220
       */
      limiter = RateLimiter.create(value);
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
