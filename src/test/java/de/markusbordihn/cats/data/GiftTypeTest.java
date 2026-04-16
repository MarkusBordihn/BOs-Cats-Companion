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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GiftTypeTest {

  @Test
  @DisplayName("All gifts have items for defined levels")
  void getItemId_allDefinedLevels_returnsValidNonBlankItem() {
    HappinessLevel[] definedLevels = {
      HappinessLevel.NEUTRAL, HappinessLevel.HAPPY, HappinessLevel.ECSTATIC
    };
    for (GiftType gift : GiftType.values()) {
      for (HappinessLevel level : definedLevels) {
        String item = gift.getItemId(level);
        assertNotNull(item, gift + " should have item for " + level);
        assertFalse(item.isBlank(), gift + " item for " + level + " should not be blank");
      }
    }
  }

  @Test
  @DisplayName("MISERABLE level falls back to valid item")
  void getItemId_miserable_fallsBackToNeutral() {
    for (GiftType gift : GiftType.values()) {
      String item = gift.getItemId(HappinessLevel.MISERABLE);
      assertNotNull(item, gift + " should fall back to a valid item for MISERABLE");
    }
  }

  @Test
  @DisplayName("SAD level falls back to valid item")
  void getItemId_sad_fallsBackToValidItem() {
    for (GiftType gift : GiftType.values()) {
      String item = gift.getItemId(HappinessLevel.SAD);
      assertNotNull(item, gift + " should fall back to a valid item for SAD");
    }
  }

  @Test
  @DisplayName("Random gift selection returns valid type")
  void randomForPersonality_null_returnsAnyGift() {
    List<GiftType> validGifts = Arrays.asList(GiftType.values());
    for (int i = 0; i < 20; i++) {
      GiftType gift = GiftType.randomForPersonality(null);
      assertNotNull(gift);
      assertTrue(validGifts.contains(gift));
    }
  }

  @Test
  @DisplayName("All personalities have at least one affinity gift")
  void affinityMapping_allPersonalitiesCovered() {
    Set<PersonalityType> coveredPersonalities = EnumSet.noneOf(PersonalityType.class);
    for (GiftType gift : GiftType.values()) {
      coveredPersonalities.add(gift.getAffinityPersonality());
    }

    for (PersonalityType type : PersonalityType.values()) {
      assertTrue(
          coveredPersonalities.contains(type),
          type + " has no affinity GiftType - every personality should have a preferred gift");
    }
  }

  @Test
  @DisplayName("Each personality has exactly one affinity gift")
  void eachPersonality_hasExactlyOneAffinityGift() {
    for (PersonalityType type : PersonalityType.values()) {
      long count =
          Arrays.stream(GiftType.values()).filter(g -> g.getAffinityPersonality() == type).count();
      assertEquals(1, count, type + " should have exactly 1 affinity GiftType, found " + count);
    }
  }
}
