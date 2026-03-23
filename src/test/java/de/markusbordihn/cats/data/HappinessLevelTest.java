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

class HappinessLevelTest {

  @Test
  @DisplayName("Negative values return MISERABLE")
  void fromValue_negative_returnsMiserable() {
    assertEquals(HappinessLevel.MISERABLE, HappinessLevel.fromValue(-1));
    assertEquals(HappinessLevel.MISERABLE, HappinessLevel.fromValue(-100));
  }

  @Test
  @DisplayName("Zero returns MISERABLE")
  void fromValue_zero_returnsMiserable() {
    assertEquals(HappinessLevel.MISERABLE, HappinessLevel.fromValue(0));
  }

  @Test
  @DisplayName("1-19 returns MISERABLE")
  void fromValue_19_returnsMiserable() {
    assertEquals(HappinessLevel.MISERABLE, HappinessLevel.fromValue(19));
  }

  @Test
  @DisplayName("20-39 returns SAD")
  void fromValue_20_returnsSad() {
    assertEquals(HappinessLevel.SAD, HappinessLevel.fromValue(20));
  }

  @Test
  @DisplayName("39 returns SAD")
  void fromValue_39_returnsSad() {
    assertEquals(HappinessLevel.SAD, HappinessLevel.fromValue(39));
  }

  @Test
  @DisplayName("40-59 returns NEUTRAL")
  void fromValue_40_returnsNeutral() {
    assertEquals(HappinessLevel.NEUTRAL, HappinessLevel.fromValue(40));
  }

  @Test
  @DisplayName("59 returns NEUTRAL")
  void fromValue_59_returnsNeutral() {
    assertEquals(HappinessLevel.NEUTRAL, HappinessLevel.fromValue(59));
  }

  @Test
  @DisplayName("60-79 returns HAPPY")
  void fromValue_60_returnsHappy() {
    assertEquals(HappinessLevel.HAPPY, HappinessLevel.fromValue(60));
  }

  @Test
  @DisplayName("79 returns HAPPY")
  void fromValue_79_returnsHappy() {
    assertEquals(HappinessLevel.HAPPY, HappinessLevel.fromValue(79));
  }

  @Test
  @DisplayName("80+ returns ECSTATIC")
  void fromValue_80_returnsEcstatic() {
    assertEquals(HappinessLevel.ECSTATIC, HappinessLevel.fromValue(80));
  }

  @Test
  @DisplayName("100 returns ECSTATIC")
  void fromValue_100_returnsEcstatic() {
    assertEquals(HappinessLevel.ECSTATIC, HappinessLevel.fromValue(100));
  }

  @Test
  @DisplayName("Over 100 returns ECSTATIC")
  void fromValue_over100_returnsEcstatic() {
    assertDoesNotThrow(() -> HappinessLevel.fromValue(200));
    assertEquals(HappinessLevel.ECSTATIC, HappinessLevel.fromValue(200));
    assertEquals(HappinessLevel.ECSTATIC, HappinessLevel.fromValue(Integer.MAX_VALUE));
  }

  @Test
  @DisplayName("All values map correctly without gaps")
  void rangesDoNotOverlap() {
    for (int i = 0; i <= 100; i++) {
      HappinessLevel level = HappinessLevel.fromValue(i);
      assertNotNull(level);
      assertTrue(
          i >= level.getMinValue() && i <= level.getMaxValue(),
          "Value "
              + i
              + " should be within range ["
              + level.getMinValue()
              + ", "
              + level.getMaxValue()
              + "] for level "
              + level);
    }
  }
}
