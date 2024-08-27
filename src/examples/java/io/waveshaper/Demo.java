package io.waveshaper;

import com.google.common.util.concurrent.AtomicLongMap;
import io.waveshaper.sequences.*;
import io.waveshaper.syncable.WaveformRateLimiter;
import io.waveshaper.waveforms.*;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Demo {
  // build an infinite sequence for generating dummy data
  private static InfiniteSequence.Builder<String> sequenceBuilder =
      new InfiniteSequence.Builder<String>()
          // first message in the sequence
          .add("hello")
          // second message in the sequence, which utilizes the context object
          .add((context, nav) -> "world " + context.iteration())
          // this third step demonstrates how to navigate to different points
          // in the inifinite sequence
          .add(
              (context, nav) -> {
                // repeat the last step every 2 iterations
                if (context.iteration() % 2 == 0) {
                  nav.back(1);
                }
                return "goodbye";
              });

  // infinite sequences aren't thread safe, so build one for each thread
  private static ThreadLocal<InfiniteSequence<String>> seq =
      ThreadLocal.withInitial(() -> sequenceBuilder.build());

  // define a factory for creating worker threads that actually handle
  // load testing
  public static class ProducerThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      // t.setUncaughtExceptionHandler(handler);
      return thread;
    }
  }

  public static void main(String[] args) {
    int sampleRate = 20;
    int cycles = 3;
    int min = 1;
    int max = 500_000;
    // Generate and print the ASCII chart
    Sparkline sparkline = new Sparkline(sampleRate, min, max);

    // create an oscillator that generates the following waveform:
    // ▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁
    Oscillator osc =
        new Oscillator.Builder()
            .waveform(SawWave::new) // ReverseSawWave, SineWave, SquareWave, TriangleWave
            .cycles(cycles)
            .sampleRate(sampleRate)
            .sampleDuration(Duration.ofMillis(250))
            .range(min, max)
            .build();

    // synchronize the oscillator signal with a rate limiter (signal chaining)
    WaveformRateLimiter rateLimiter = WaveformRateLimiter.create(osc);

    // generate a threadpool to execute our tasks
    ExecutorService executor = Executors.newFixedThreadPool(10, new ProducerThreadFactory());

    // track the amount of permits that are issued to each thread
    AtomicLongMap<String> permitsByThread = AtomicLongMap.create();

    while (rateLimiter.updating()) {
      // try to acquire a permit for doing work. this will block if we're being
      // rate limited
      rateLimiter.acquire();
      // yay, we're unblocked! submit a task to our threadpool
      executor.execute(
          () -> {
            // increment the permit count for the executing thread
            permitsByThread.incrementAndGet(Thread.currentThread().getName());
            // do work here. e.g. if you are producing a message to kafka, this is
            // where you'd do it
            /*String message = */seq.get().next();

            // produce message somewhere
            // ...

            // print
            rateLimiter.registerCallback(rl -> {
                sparkline.addDataPoint(rl.getEps());
                sparkline.print();
                System.out.printf("eps: %.1f\n", rl.getEps());
            });
          });
    }

    executor.shutdown();
    // System.out.println(permitsByThread);
  }
}
