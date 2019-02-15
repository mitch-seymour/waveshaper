package io.waveshaper.sequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * An infinite sequence can be used to build data generators. Note: this class is not thread safe
 *
 * @param <T> the type of data produced by this sequence
 */
public class InfiniteSequence<T> implements Iterator<T> {
  private final Context context;
  private final Navigator<T> nav;
  private final List<BiFunction<Context, Navigator<T>, T>> seq;
  private long iterations;

  // make the constructor private since we're using the
  // builder pattern
  private InfiniteSequence(List<BiFunction<Context, Navigator<T>, T>> seq) {
    this.context = new Context();
    this.nav = new Navigator<T>(seq.size());
    this.seq = seq;
    this.iterations = 0;
  }

  /** An object that can be used for navigating forward and backward in the infinite sequence */
  public static class Navigator<T> {
    private int currentStep;
    private final int stepsPerCycle;
    private boolean skipped;

    Navigator(int stepsPerCycle) {
      this.currentStep = 0;
      this.stepsPerCycle = stepsPerCycle;
      this.skipped = false;
    }

    private int advance() {
      currentStep = (currentStep + 1) % stepsPerCycle;
      skipped = false;
      return currentStep;
    }

    public int back(int steps) {
      int position = (currentStep - steps) % stepsPerCycle;
      if (position < 0) {
        // TODO: should we throw an exception here?
        position = 0;
      }
      currentStep = position;
      skipped = true;
      return currentStep;
    }

    public int forward(int steps) {
      currentStep = (currentStep + steps) % stepsPerCycle;
      skipped = true;
      return currentStep;
    }

    public int position() {
      return currentStep;
    }

    private boolean skipped() {
      return skipped;
    }
  }

  /**
   * Context objects are passed to sequence functions. They contain info about the current
   * iteration, and allow different sequence steps to pass data to one another.
   */
  public static class Context {
    private long iteration;
    private HashMap<String, Object> data;

    private Context() {
      this.iteration = 0;
      this.data = new HashMap<>();
    }

    private void setIteration(long iteration) {
      this.iteration = iteration;
    }

    public void set(String key, Object value) {
      data.put(key, value);
    }

    public Object get(String key) {
      return data.get(key);
    }

    public long iteration() {
      return iteration;
    }

    public void reset() {
      data.clear();
    }
  }

  /**
   * An builder that is used to create infinite sequences.
   *
   * @param <K> the type of data to be produced by the sequence
   */
  public static class Builder<K> {
    List<BiFunction<Context, Navigator<K>, K>> seq = new ArrayList<>();

    public Builder<K> add(BiFunction<Context, Navigator<K>, K> generatorFunction) {
      seq.add(generatorFunction);
      return this;
    }

    public Builder<K> add(K rawValue) {
      seq.add((c, n) -> rawValue);
      return this;
    }

    public InfiniteSequence<K> build() {
      return new InfiniteSequence<K>(seq);
    }
  }

  @Override
  public boolean hasNext() {
    return seq.size() > 0;
  }

  @Override
  public T next() {
    if (nav.position() == 0) {
      iterations++;
      context.setIteration(iterations);
    }
    // get the next callable in our sequence and run it using the
    // updated context
    T result = seq.get(nav.position()).apply(context, nav);
    // advance the pointer if it was not directly manipulated
    if (!nav.skipped()) {
      nav.advance();
      return result;
    } else {
      // reset the skip state and increment the iteration count
      // since the pointer was manipulated
      nav.skipped = false;
      iterations++;
      context.setIteration(iterations);
      return next();
    }
  }
}
