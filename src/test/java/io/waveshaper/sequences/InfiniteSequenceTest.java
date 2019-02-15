package io.waveshaper.sequences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InfiniteSequenceTest {
  @Test
  void testNavigatorLoops() {
    InfiniteSequence<String> seq =
        new InfiniteSequence.Builder<String>().add("so").add("much").add("magic").build();
    // assert basic iteration order
    assertEquals("so", seq.next());
    assertEquals("much", seq.next());
    assertEquals("magic", seq.next());
    // assert loop restarts
    assertEquals("so", seq.next(), "Infinite sequence did not loop");
  }

  @Test
  void testNavigatorForward() {
    InfiniteSequence<String> seq =
        new InfiniteSequence.Builder<String>()
            .add("only")
            .add(
                (context, nav) -> {
                  // move the pointer forward
                  if (context.iteration() == 1) {
                    nav.forward(4);
                  }
                  return "you";
                })
            .add("can")
            .add("fill")
            .add("those")
            .add("spaces")
            .build();
    assertEquals("only", seq.next());
    // the pointer should skip forward during the first iteration
    assertEquals("spaces", seq.next());
    assertEquals("only", seq.next());
    // the pointer should advance to this step during the second iteration
    assertEquals("you", seq.next());
  }

  @Test
  void testNavigatorBack() {
    InfiniteSequence<String> seq =
        new InfiniteSequence.Builder<String>()
            .add("shift")
            .add("in")
            .add("the")
            .add(
                (context, nav) -> {
                  // move the pointer backwards
                  if (context.iteration() == 1) {
                    nav.back(3);
                  }
                  return "unconscious";
                })
            .build();
    // step through the sequence
    assertEquals("shift", seq.next());
    assertEquals("in", seq.next());
    assertEquals("the", seq.next());
    assertEquals("shift", seq.next());
    // the pointer should have moved back during the first iteration
    assertEquals("in", seq.next());
    assertEquals("the", seq.next());
    // the pointer should advance to this step during the second iteration
    assertEquals("unconscious", seq.next());
  }
}
