package io.waveshaper.syncable;

import com.google.common.util.concurrent.RateLimiter;
import io.waveshaper.waveforms.Oscillator;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

public class WaveformRateLimiter implements Syncable {
  private final Oscillator osc;
  private RateLimiter limiter;
  private int totalAcquired = 0;
  private Instant start = Instant.now();

  // Store the previous rate and eps
  private double currentRate = 0.0;
  private double eps = 0.0;

  // Consumer to hold the callback function
  private Consumer<WaveformRateLimiter> callback;

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
      this.eps = totalAcquired / elapsed * 1000;
      this.currentRate = limiter.getRate();
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

      // Invoke the callback if it has been registered
      if (callback != null) {
          callback.accept(this);
      }
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

  // Getter for previous rate
  public double getCurrentRate() {
    return currentRate;
  }

  // Getter for events per second (eps)
  public double getEps() {
    return eps;
  }

  public void registerCallback(Consumer<WaveformRateLimiter> callback) {
      this.callback = callback;
  }


  public boolean updating() {
    return osc.running() && osc.hasNext();
  }
}
