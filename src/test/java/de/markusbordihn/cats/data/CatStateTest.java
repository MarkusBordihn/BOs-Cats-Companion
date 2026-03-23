/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.cats.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatStateTest {

  @Test
  @DisplayName("SLEEPING state is a sleeping state")
  void isSleepingState_sleeping_returnsTrue() {
    assertTrue(CatState.SLEEPING.isSleepingState());
  }

  @Test
  @DisplayName("GOING_TO_BED state is a sleeping state")
  void isSleepingState_goingToBed_returnsTrue() {
    assertTrue(CatState.GOING_TO_BED.isSleepingState());
  }

  @Test
  @DisplayName("Non-sleeping states return false")
  void isSleepingState_nonSleepingStates_returnFalse() {
    assertFalse(CatState.SITTING.isSleepingState());
    assertFalse(CatState.FOLLOWING.isSleepingState());
    assertFalse(CatState.WANDERING.isSleepingState());
    assertFalse(CatState.PLAYING.isSleepingState());
    assertFalse(CatState.SEARCHING.isSleepingState());
    assertFalse(CatState.WAITING.isSleepingState());
    assertFalse(CatState.ATTACKING.isSleepingState());
  }

  @Test
  @DisplayName("Exactly two states are sleeping states")
  void isSleepingState_onlyTwoStatesAreTrue() {
    long sleepingCount = 0;
    for (CatState state : CatState.values()) {
      if (state.isSleepingState()) {
        sleepingCount++;
      }
    }
    assertEquals(2, sleepingCount, "Exactly 2 states should be sleeping states");
  }
}
