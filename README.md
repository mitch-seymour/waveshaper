[![Build Status](https://travis-ci.com/mitch-seymour/waveshaper.svg?branch=master)](https://travis-ci.com/mitch-seymour/waveshaper) &nbsp;[![Maven Central](https://img.shields.io/maven-central/v/io.waveshaper/waveshaper.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.waveshaper%22%20AND%20a:%22waveshaper%22)
# waveshaper
Inspired by [ChucK][chuck], and also by the shape of traffic patterns I recently encountered in one of my projects (which looked like reverse saw waves), __waveshaper__ uses musical (or otherwise synthetic) waveforms for load testing applications. This is made possible through the implementation of a signal oscillator that generates a number of waveforms (see below), which is then chained to a [guava RateLimiter][guava_rl]. This threadsafe rate limiter can then be utilized by your data generation / load testing tools, as seen in the demo below.
This project is __pre-alpha__. Do not use in production yet.

[chuck]: http://chuck.cs.princeton.edu/
[guava_rl]: https://github.com/google/guava/blob/master/guava/src/com/google/common/util/concurrent/RateLimiter.java

## Waveforms
The following waveforms are currently supported

| Waveform  | Class | Shape |
| ------------- | ------------- | ------------- |
| Sine  | [SineWave.java][sine]  | `▁▂▄▆██▆▄▂▁▁▂▄▆██▆▄▂▁▁▂▄▆██▆▄▂` |
| Saw  | [SawWave.java][saw] | `▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁` |
| Reverse saw  | [ReverseSawWave.java][reverse_saw]  | `█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁▁█▇▆▅▄▄▃▂▁` |
| Square  | [SquareWave.java][square]  | `▁▁█████▁▁▁▁▁█████▁▁▁▁▁█████▁▁` |
| Triangle  | [Triangle.java][triangle]  | `▁▂▃▅▆█▆▅▃▂▁▂▃▅▆█▆▅▃▂▁▂▃▅▆█▆▅▃▂▁` |

[sine]: src/main/java/io/waveshaper/waveforms/SineWave.java
[saw]: src/main/java/io/waveshaper/waveforms/SawWave.java
[reverse_saw]: src/main/java/io/waveshaper/waveforms/ReverseSawWave.java
[square]: src/main/java/io/waveshaper/waveforms/SquareWave.java
[triangle]: src/main/java/io/waveshaper/waveforms/TriangleWave.java

Download
---------
Gradle:
```groovy
implementation 'io.waveshaper:waveshaper:0.1.0'
```

Maven:
```xml
<dependency>
  <groupId>io.waveshaper</groupId>
  <artifactId>waveshaper</artifactId>
  <version>0.1.0</version>
</dependency>
```

[Jar downloads](https://search.maven.org/artifact/io.waveshaper/waveshaper/0.1.0/jar) are available from Maven Central.

## Basic usage
```java
// create an oscillator that generates the following waveform:
// ▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁▂▃▄▄▅▆▇█▁▁
Oscillator osc =
    new Oscillator.Builder()
        .waveform(ReverseSawWave::new)
        .cycles(3)
        .sampleRate(8)
        .sampleDuration(Duration.ofSeconds(5))
        .range(1, 500_000)
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
        });
}
```

## ∞ Infinite sequences
In addition to synthetic waveforms, __waveshaper__ also allows you to build data generators for your load testing activities using a navigable, [InfiniteSequence][infinite_sequence]. These infinite sequences can generate any type of data. Here is an example:

```bash
InfiniteSequence<String> seq =
    new InfiniteSequence.Builder<String>()
        .add("hello")
        .add("world")
        .build();

while (true) {
  System.out.println(seq.next());
}
```

produces the following output (which would continue forever):
```bash
hello
world
hello
world
hello
world
```

Dynamic, infinite sequences are also possible. Simply provide a callable that accepts a context and navigator object, and utilize the context to either get info about the current iteration (good for dynamic ID generation) or to set custom data for other steps to utilize.

```java
BiFunction<Context, Navigator, String> sayHello =
    (ctx, nav) -> {
      // generate some fields to be used by a later step.
      // ctx.iteration() is convenient for id generation
      ctx.set("id", "abc" + ctx.iteration());
      ctx.set("name", "mitch");
      return "hello, mitch";
    };

BiFunction<Context, Navigator, String> sayGoodbye =
    (ctx, nav) -> {
      // use a field that was set by a previous step in this sequence
      return "goodbye, " + ctx.get("name");
    };

InfiniteSequence<String> seq =
    new InfiniteSequence.Builder<String>()
      .add(sayHello)
      .add(sayGoodbye)
      .build();

while (true) {
  System.out.println(seq.next());
}
```

The above would produce the following:

```
hello, mitch
goodbye, mitch
hello, mitch
goodbye, mitch
...
```

Finally, you can use the navigator object to move through an infinite sequence non-linearly.

```java
BiFunction<Context, Navigator, String> sometimesRequeue =
    (ctx, nav) -> {
      if (ctx.iteration() % 2 == 0) {
        nav.back(1);
      }
      return "queued";
    };

InfiniteSequence<String> seq =
    new InfiniteSequence.Builder<String>()
        .add("pending")
        .add(sometimesRequeue)
        .add("delivered")
        .build();
```

[infinite_sequence]: src/main/java/io/waveshaper/sequences/InfiniteSequence.java
