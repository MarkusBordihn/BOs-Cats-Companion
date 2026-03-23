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

class HappinessSourceTest {

  @Test
  @DisplayName("Null personality returns base delta")
  void calculateDelta_nullPersonality_returnsBase() {
    assertEquals(
        HappinessSource.PETTING.getBaseDelta(), HappinessSource.PETTING.calculateDelta(null));
    assertEquals(
        HappinessSource.PLAYING.getBaseDelta(), HappinessSource.PLAYING.calculateDelta(null));
    assertEquals(HappinessSource.TREAT.getBaseDelta(), HappinessSource.TREAT.calculateDelta(null));
    assertEquals(
        HappinessSource.FEEDING.getBaseDelta(), HappinessSource.FEEDING.calculateDelta(null));
    assertEquals(
        HappinessSource.SLEEPING.getBaseDelta(), HappinessSource.SLEEPING.calculateDelta(null));
    assertEquals(
        HappinessSource.SLEEPING_IN_BED.getBaseDelta(),
        HappinessSource.SLEEPING_IN_BED.calculateDelta(null));
  }

  @Test
  @DisplayName("CUDDLY personality gives petting bonus")
  void petting_cuddlyPersonality_givesBonus() {
    assertEquals(7, HappinessSource.PETTING.calculateDelta(PersonalityType.CUDDLY));
  }

  @Test
  @DisplayName("PLAYFUL personality gives playing bonus")
  void playing_playfulPersonality_givesBonus() {
    assertEquals(12, HappinessSource.PLAYING.calculateDelta(PersonalityType.PLAYFUL));
  }

  @Test
  @DisplayName("LAZY personality gives treat bonus")
  void treat_lazyPersonality_givesBonus() {
    assertEquals(15, HappinessSource.TREAT.calculateDelta(PersonalityType.LAZY));
  }

  @Test
  @DisplayName("LAZY personality gives sleeping bonus")
  void sleeping_lazyPersonality_givesBonus() {
    assertEquals(3, HappinessSource.SLEEPING.calculateDelta(PersonalityType.LAZY));
  }

  @Test
  @DisplayName("LAZY personality gives bed sleeping bonus")
  void sleepingInBed_lazyPersonality_givesBonus() {
    assertEquals(15, HappinessSource.SLEEPING_IN_BED.calculateDelta(PersonalityType.LAZY));
  }

  @Test
  @DisplayName("Non-preferred personality uses modifier")
  void petting_shyPersonality_usesModifier() {
    assertEquals(5, HappinessSource.PETTING.calculateDelta(PersonalityType.SHY));
  }

  @Test
  @DisplayName("Independent personality reduces petting delta")
  void petting_independentPersonality_usesModifier() {
    assertEquals(3, HappinessSource.PETTING.calculateDelta(PersonalityType.INDEPENDENT));
  }

  @Test
  @DisplayName("LAZY personality reduces playing delta")
  void playing_lazyPersonality_usesModifier() {
    assertEquals(4, HappinessSource.PLAYING.calculateDelta(PersonalityType.LAZY));
  }

  @Test
  @DisplayName("All combinations have minimum delta of 1")
  void calculateDelta_minimumIsOne() {
    for (HappinessSource source : HappinessSource.values()) {
      for (PersonalityType personality : PersonalityType.values()) {
        int delta = source.calculateDelta(personality);
        assertTrue(
            delta >= 1,
            source + " with " + personality + " should give at least 1, but was " + delta);
      }
    }
  }

  @Test
  @DisplayName("FEEDING uses activity modifier for all personalities")
  void feeding_noPreferredPersonality_usesActivityModifier() {
    assertEquals(3, HappinessSource.FEEDING.calculateDelta(PersonalityType.PLAYFUL));
    assertEquals(4, HappinessSource.FEEDING.calculateDelta(PersonalityType.MISCHIEVOUS));
  }
}
