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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonalityTypeTest {

  @Test
  @DisplayName("All personality types have base profile")
  void getBaseProfile_allTypes_returnsNonNull() {
    for (PersonalityType type : PersonalityType.values()) {
      assertNotNull(type.getBaseProfile(), type + " should have a base profile");
    }
  }

  @Test
  @DisplayName("LAZY has correct modifiers")
  void modifiers_lazyPersonality_hasCorrectValues() {
    assertEquals(0.8f, PersonalityType.LAZY.getActivityModifier(), 0.001f);
    assertEquals(1.2f, PersonalityType.LAZY.getPetModifier(), 0.001f);
    assertEquals(0.6f, PersonalityType.LAZY.getPlayModifier(), 0.001f);
  }

  @Test
  @DisplayName("PLAYFUL has correct modifiers")
  void modifiers_playfulPersonality_hasCorrectValues() {
    assertEquals(1.3f, PersonalityType.PLAYFUL.getActivityModifier(), 0.001f);
    assertEquals(0.8f, PersonalityType.PLAYFUL.getPetModifier(), 0.001f);
    assertEquals(1.4f, PersonalityType.PLAYFUL.getPlayModifier(), 0.001f);
  }

  @Test
  @DisplayName("SHY has correct modifiers")
  void modifiers_shyPersonality_hasCorrectValues() {
    assertEquals(0.7f, PersonalityType.SHY.getActivityModifier(), 0.001f);
    assertEquals(1.0f, PersonalityType.SHY.getPetModifier(), 0.001f);
    assertEquals(0.5f, PersonalityType.SHY.getPlayModifier(), 0.001f);
  }

  @Test
  @DisplayName("MISCHIEVOUS has highest activity modifier")
  void modifiers_mischievous_hasHighestActivity() {
    float mischievousActivity = PersonalityType.MISCHIEVOUS.getActivityModifier();
    for (PersonalityType type : PersonalityType.values()) {
      assertTrue(
          type.getActivityModifier() <= mischievousActivity,
          type + " activityModifier should not exceed MISCHIEVOUS");
    }
  }

  @Test
  @DisplayName("Random personality returns valid type")
  void random_returnsValidType() {
    List<PersonalityType> validTypes = Arrays.asList(PersonalityType.values());
    for (int i = 0; i < 50; i++) {
      PersonalityType random = PersonalityType.random();
      assertNotNull(random);
      assertTrue(validTypes.contains(random));
    }
  }

  @Test
  @DisplayName("All personality types have valid base profiles")
  void allTypes_haveBaseProfileWithValidRange() {
    for (PersonalityType type : PersonalityType.values()) {
      CatBehaviorProfile profile = type.getBaseProfile();
      assertTrue(
          profile.activityWeight() >= 0f && profile.activityWeight() <= 1f,
          type + " activityWeight out of range");
      assertTrue(
          profile.wanderRadius() >= 5f && profile.wanderRadius() <= 25f,
          type + " wanderRadius out of range");
      assertTrue(
          profile.idleDurationSeconds() >= 5f && profile.idleDurationSeconds() <= 40f,
          type + " idleDurationSeconds out of range");
    }
  }

  @Test
  @DisplayName("All personality types have positive modifiers")
  void allTypes_havePositiveModifiers() {
    for (PersonalityType type : PersonalityType.values()) {
      assertTrue(type.getActivityModifier() > 0f, type + " activityModifier should be > 0");
      assertTrue(type.getPetModifier() > 0f, type + " petModifier should be > 0");
      assertTrue(type.getPlayModifier() > 0f, type + " playModifier should be > 0");
    }
  }
}
