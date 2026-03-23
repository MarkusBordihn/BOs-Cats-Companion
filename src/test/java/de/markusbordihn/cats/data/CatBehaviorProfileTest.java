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

class CatBehaviorProfileTest {

  @Test
  @DisplayName("Constructor clamps negative weights to 0")
  void constructor_clampsWeightsBelow0() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(-1f, -1f, -1f, -1f, -1f, 5.0f, -1f, -1f, 5.0f, 5.0f);
    assertEquals(0.0f, profile.activityWeight());
    assertEquals(0.0f, profile.socialWeight());
    assertEquals(0.0f, profile.playWeight());
    assertEquals(0.0f, profile.restWeight());
    assertEquals(0.0f, profile.ownerAffinity());
    assertEquals(0.0f, profile.bedPreference());
    assertEquals(0.0f, profile.giftChance());
  }

  @Test
  @DisplayName("Constructor clamps weights above 1 to 1")
  void constructor_clampsWeightsAbove1() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(2f, 2f, 2f, 2f, 2f, 5.0f, 2f, 2f, 5.0f, 5.0f);
    assertEquals(1.0f, profile.activityWeight());
    assertEquals(1.0f, profile.socialWeight());
    assertEquals(1.0f, profile.playWeight());
    assertEquals(1.0f, profile.restWeight());
    assertEquals(1.0f, profile.ownerAffinity());
    assertEquals(1.0f, profile.bedPreference());
    assertEquals(1.0f, profile.giftChance());
  }

  @Test
  @DisplayName("Constructor clamps stranger distance to min 2.0")
  void constructor_clampsStrangerDistanceMin() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0f, 0.5f, 0.5f, 15.0f, 20.0f);
    assertEquals(2.0f, profile.strangerDistance());
  }

  @Test
  @DisplayName("Constructor clamps stranger distance to max 12.0")
  void constructor_clampsStrangerDistanceMax() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 99f, 0.5f, 0.5f, 15.0f, 20.0f);
    assertEquals(12.0f, profile.strangerDistance());
  }

  @Test
  @DisplayName("Constructor clamps wander radius to min 5.0")
  void constructor_clampsWanderRadiusMin() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 5.0f, 0.5f, 0.5f, 1.0f, 20.0f);
    assertEquals(5.0f, profile.wanderRadius());
  }

  @Test
  @DisplayName("Constructor clamps wander radius to max 25.0")
  void constructor_clampsWanderRadiusMax() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 5.0f, 0.5f, 0.5f, 100.0f, 20.0f);
    assertEquals(25.0f, profile.wanderRadius());
  }

  @Test
  @DisplayName("Constructor clamps idle duration to min 5.0 seconds")
  void constructor_clampsIdleDurationMin() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 5.0f, 0.5f, 0.5f, 15.0f, 0.0f);
    assertEquals(5.0f, profile.idleDurationSeconds());
  }

  @Test
  @DisplayName("Constructor clamps idle duration to max 40.0 seconds")
  void constructor_clampsIdleDurationMax() {
    CatBehaviorProfile profile =
        new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 5.0f, 0.5f, 0.5f, 15.0f, 99.0f);
    assertEquals(40.0f, profile.idleDurationSeconds());
  }

  @Test
  @DisplayName("Neutral profile has balanced values")
  void neutral_hasExpectedValues() {
    CatBehaviorProfile neutral = CatBehaviorProfile.neutral();
    assertEquals(0.5f, neutral.activityWeight());
    assertEquals(0.5f, neutral.socialWeight());
    assertEquals(0.5f, neutral.playWeight());
    assertEquals(0.5f, neutral.restWeight());
    assertEquals(0.5f, neutral.ownerAffinity());
    assertEquals(0.5f, neutral.bedPreference());
    assertEquals(0.5f, neutral.giftChance());
    assertEquals(15.0f, neutral.wanderRadius());
    assertEquals(20.0f, neutral.idleDurationSeconds());
  }

  @Test
  @DisplayName("All base profiles are not null")
  void allBaseProfiles_notNull() {
    assertNotNull(CatBehaviorProfile.baseLazy());
    assertNotNull(CatBehaviorProfile.basePlayful());
    assertNotNull(CatBehaviorProfile.baseShy());
    assertNotNull(CatBehaviorProfile.baseBrave());
    assertNotNull(CatBehaviorProfile.baseCurious());
    assertNotNull(CatBehaviorProfile.baseCuddly());
    assertNotNull(CatBehaviorProfile.baseIndependent());
    assertNotNull(CatBehaviorProfile.baseMischievous());
  }

  @Test
  @DisplayName("All base profiles have values in valid ranges")
  void allBaseProfiles_valuesInValidRange() {
    CatBehaviorProfile[] profiles = {
      CatBehaviorProfile.baseLazy(),
      CatBehaviorProfile.basePlayful(),
      CatBehaviorProfile.baseShy(),
      CatBehaviorProfile.baseBrave(),
      CatBehaviorProfile.baseCurious(),
      CatBehaviorProfile.baseCuddly(),
      CatBehaviorProfile.baseIndependent(),
      CatBehaviorProfile.baseMischievous()
    };
    for (CatBehaviorProfile p : profiles) {
      assertTrue(p.activityWeight() >= 0f && p.activityWeight() <= 1f);
      assertTrue(p.socialWeight() >= 0f && p.socialWeight() <= 1f);
      assertTrue(p.playWeight() >= 0f && p.playWeight() <= 1f);
      assertTrue(p.restWeight() >= 0f && p.restWeight() <= 1f);
      assertTrue(p.ownerAffinity() >= 0f && p.ownerAffinity() <= 1f);
      assertTrue(p.strangerDistance() >= 2f && p.strangerDistance() <= 12f);
      assertTrue(p.bedPreference() >= 0f && p.bedPreference() <= 1f);
      assertTrue(p.giftChance() >= 0f && p.giftChance() <= 1f);
      assertTrue(p.wanderRadius() >= 5f && p.wanderRadius() <= 25f);
      assertTrue(p.idleDurationSeconds() >= 5f && p.idleDurationSeconds() <= 40f);
    }
  }

  @Test
  @DisplayName("withActivityWeight returns new immutable record")
  void withActivityWeight_returnsNewRecordWithoutModifyingOriginal() {
    CatBehaviorProfile original = CatBehaviorProfile.neutral();
    CatBehaviorProfile updated = original.withActivityWeight(0.8f);
    assertEquals(0.5f, original.activityWeight());
    assertEquals(0.8f, updated.activityWeight());
    assertEquals(original.socialWeight(), updated.socialWeight());
    assertNotSame(original, updated);
  }
}
