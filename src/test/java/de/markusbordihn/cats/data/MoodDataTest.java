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

class MoodDataTest {

  @Test
  @DisplayName("Default mood has 50 happiness")
  void defaultMood_hasHappiness50() {
    MoodData mood = MoodData.defaultMood();
    assertEquals(MoodData.DEFAULT_HAPPINESS, mood.happiness());
  }

  @Test
  @DisplayName("Happiness above max clamps to max")
  void withHappiness_clampsToMax() {
    MoodData mood = new MoodData(50, 0L).withHappiness(200);
    assertEquals(MoodData.MAX_HAPPINESS, mood.happiness());
  }

  @Test
  @DisplayName("Happiness below min clamps to min")
  void withHappiness_clampsToMin() {
    MoodData mood = new MoodData(50, 0L).withHappiness(-50);
    assertEquals(MoodData.MIN_HAPPINESS, mood.happiness());
  }

  @Test
  @DisplayName("Timestamp is preserved when updating happiness")
  void withHappiness_preservesTimestamp() {
    long timestamp = 12345L;
    MoodData mood = new MoodData(50, timestamp).withHappiness(75);
    assertEquals(timestamp, mood.lastMoodUpdate());
  }

  @Test
  @DisplayName("Positive adjustment adds to happiness")
  void adjustHappiness_positive_adds() {
    MoodData mood = new MoodData(50, 0L).adjustHappiness(10);
    assertEquals(60, mood.happiness());
  }

  @Test
  @DisplayName("Negative adjustment subtracts from happiness")
  void adjustHappiness_negative_subtracts() {
    MoodData mood = new MoodData(50, 0L).adjustHappiness(-20);
    assertEquals(30, mood.happiness());
  }

  @Test
  @DisplayName("Large positive adjustment clamps to max")
  void adjustHappiness_doesNotExceedMax() {
    MoodData mood = new MoodData(50, 0L).adjustHappiness(999);
    assertEquals(MoodData.MAX_HAPPINESS, mood.happiness());
  }

  @Test
  @DisplayName("Large negative adjustment clamps to min")
  void adjustHappiness_doesNotGoBelowMin() {
    MoodData mood = new MoodData(50, 0L).adjustHappiness(-999);
    assertEquals(MoodData.MIN_HAPPINESS, mood.happiness());
  }

  @Test
  @DisplayName("Adjustment updates timestamp to current time")
  void adjustHappiness_updatesTimestamp() {
    long before = System.currentTimeMillis();
    MoodData mood = new MoodData(50, 0L).adjustHappiness(5);
    long after = System.currentTimeMillis();
    assertTrue(mood.lastMoodUpdate() >= before && mood.lastMoodUpdate() <= after);
  }

  @Test
  @DisplayName("Returns correct happiness level for value")
  void getLevel_returnsCorrectLevel() {
    assertEquals(HappinessLevel.HAPPY, new MoodData(75, 0L).getLevel());
    assertEquals(HappinessLevel.NEUTRAL, new MoodData(50, 0L).getLevel());
    assertEquals(HappinessLevel.MISERABLE, new MoodData(0, 0L).getLevel());
  }

  @Test
  @DisplayName("withHappiness returns new immutable record")
  void withHappiness_returnsNewRecord() {
    MoodData original = new MoodData(50, 0L);
    MoodData updated = original.withHappiness(80);
    assertEquals(50, original.happiness());
    assertEquals(80, updated.happiness());
    assertNotSame(original, updated);
  }

  @Test
  @DisplayName("withLastMoodUpdate returns new immutable record")
  void withLastMoodUpdate_returnsNewRecord() {
    MoodData original = new MoodData(50, 100L);
    MoodData updated = original.withLastMoodUpdate(999L);
    assertEquals(100L, original.lastMoodUpdate());
    assertEquals(999L, updated.lastMoodUpdate());
    assertEquals(original.happiness(), updated.happiness());
  }
}
