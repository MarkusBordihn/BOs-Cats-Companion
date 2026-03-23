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

class CatBehaviorProfileResolverTest {

  private static final float FLOAT_TOLERANCE = 0.001f;

  @Test
  @DisplayName("Null primary returns neutral")
  void resolve_nullPrimary_returnsNeutral() {
    CatBehaviorProfile neutral = CatBehaviorProfile.neutral();
    assertEquals(
        neutral.activityWeight(),
        CatBehaviorProfileResolver.resolve(null, null, null).activityWeight(),
        FLOAT_TOLERANCE);
    assertEquals(
        neutral.socialWeight(),
        CatBehaviorProfileResolver.resolve(null, null, null).socialWeight(),
        FLOAT_TOLERANCE);
  }

  @Test
  @DisplayName("Primary only returns base profile")
  void resolve_primaryOnly_returnsBaseProfile() {
    CatBehaviorProfile lazy = CatBehaviorProfile.baseLazy();
    assertEquals(
        lazy.activityWeight(),
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, null).activityWeight(),
        FLOAT_TOLERANCE);
    assertEquals(
        lazy.restWeight(),
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, null).restWeight(),
        FLOAT_TOLERANCE);
  }

  @Test
  @DisplayName("Secondary blends 70/30")
  void resolve_withSecondary_blends70_30() {
    CatBehaviorProfile lazy = CatBehaviorProfile.baseLazy();
    CatBehaviorProfile playful = CatBehaviorProfile.basePlayful();
    CatBehaviorProfile result =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, PersonalityType.PLAYFUL, null);

    assertEquals(
        lazy.activityWeight() * 0.7f + playful.activityWeight() * 0.3f,
        result.activityWeight(),
        FLOAT_TOLERANCE);
  }

  @Test
  @DisplayName("ECSTATIC increases play and social")
  void resolve_moodEcstatic_increasesPlayAndSocial() {
    CatBehaviorProfile baseResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, HappinessLevel.NEUTRAL);
    CatBehaviorProfile ecstaticResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, HappinessLevel.ECSTATIC);

    assertTrue(ecstaticResult.socialWeight() > baseResult.socialWeight());
    assertTrue(ecstaticResult.playWeight() > baseResult.playWeight());
    assertTrue(ecstaticResult.giftChance() > baseResult.giftChance());
  }

  @Test
  @DisplayName("HAPPY increases less than ECSTATIC")
  void resolve_moodHappy_moderateIncrease() {
    CatBehaviorProfile happyResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, HappinessLevel.HAPPY);
    CatBehaviorProfile ecstaticResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, HappinessLevel.ECSTATIC);

    assertTrue(ecstaticResult.socialWeight() > happyResult.socialWeight());
    assertTrue(ecstaticResult.playWeight() > happyResult.playWeight());
  }

  @Test
  @DisplayName("NEUTRAL has no effect")
  void resolve_moodNeutral_noChange() {
    CatBehaviorProfile withNeutral =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, HappinessLevel.NEUTRAL);
    CatBehaviorProfile withoutMood =
        CatBehaviorProfileResolver.resolve(PersonalityType.LAZY, null, null);

    assertEquals(withoutMood.activityWeight(), withNeutral.activityWeight(), FLOAT_TOLERANCE);
    assertEquals(withoutMood.socialWeight(), withNeutral.socialWeight(), FLOAT_TOLERANCE);
    assertEquals(withoutMood.playWeight(), withNeutral.playWeight(), FLOAT_TOLERANCE);
  }

  @Test
  @DisplayName("SAD decreases activity")
  void resolve_moodSad_decreasesActivity() {
    CatBehaviorProfile baseResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.PLAYFUL, null, HappinessLevel.NEUTRAL);
    CatBehaviorProfile sadResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.PLAYFUL, null, HappinessLevel.SAD);

    assertTrue(sadResult.activityWeight() < baseResult.activityWeight());
    assertTrue(sadResult.socialWeight() < baseResult.socialWeight());
    assertTrue(sadResult.restWeight() > baseResult.restWeight());
  }

  @Test
  @DisplayName("MISERABLE decreases more than SAD")
  void resolve_moodMiserable_strongerDecreaseThanSad() {
    CatBehaviorProfile sadResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.PLAYFUL, null, HappinessLevel.SAD);
    CatBehaviorProfile miserableResult =
        CatBehaviorProfileResolver.resolve(PersonalityType.PLAYFUL, null, HappinessLevel.MISERABLE);

    assertTrue(miserableResult.activityWeight() < sadResult.activityWeight());
    assertTrue(miserableResult.socialWeight() < sadResult.socialWeight());
    assertTrue(miserableResult.restWeight() > sadResult.restWeight());
  }

  @Test
  @DisplayName("All mood modifiers stay within bounds")
  void resolve_moodModifier_doesNotExceedClampBounds() {
    for (PersonalityType primary : PersonalityType.values()) {
      for (HappinessLevel mood : HappinessLevel.values()) {
        CatBehaviorProfile result = CatBehaviorProfileResolver.resolve(primary, null, mood);
        assertTrue(
            result.activityWeight() >= 0f && result.activityWeight() <= 1f,
            primary + " + " + mood + " activityWeight out of range");
        assertTrue(
            result.socialWeight() >= 0f && result.socialWeight() <= 1f,
            primary + " + " + mood + " socialWeight out of range");
        assertTrue(
            result.playWeight() >= 0f && result.playWeight() <= 1f,
            primary + " + " + mood + " playWeight out of range");
        assertTrue(
            result.giftChance() >= 0f && result.giftChance() <= 1f,
            primary + " + " + mood + " giftChance out of range");
      }
    }
  }

  @Test
  @DisplayName("All combinations resolve without exceptions")
  void resolve_allPersonalityCombinations_doNotThrow() {
    for (PersonalityType primary : PersonalityType.values()) {
      for (PersonalityType secondary : PersonalityType.values()) {
        for (HappinessLevel mood : HappinessLevel.values()) {
          assertDoesNotThrow(
              () -> CatBehaviorProfileResolver.resolve(primary, secondary, mood),
              primary + " + " + secondary + " + " + mood + " threw exception");
        }
      }
    }
  }
}
