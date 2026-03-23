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

import javax.annotation.Nullable;

public enum HappinessSource {
  PETTING(5, PersonalityType.CUDDLY),
  PLAYING(8, PersonalityType.PLAYFUL),
  TREAT(10, PersonalityType.LAZY),
  FEEDING(3, null),
  SLEEPING(2, PersonalityType.LAZY),
  SLEEPING_IN_BED(10, PersonalityType.LAZY);

  private final int baseDelta;
  @Nullable private final PersonalityType preferredPersonality;

  HappinessSource(int baseDelta, @Nullable PersonalityType preferredPersonality) {
    this.baseDelta = baseDelta;
    this.preferredPersonality = preferredPersonality;
  }

  public int calculateDelta(@Nullable PersonalityType catPersonality) {
    if (catPersonality == null || preferredPersonality == null) {
      return baseDelta;
    }
    if (catPersonality == preferredPersonality) {
      return (int) (baseDelta * 1.5f);
    }
    float modifier =
        switch (this) {
          case PETTING -> catPersonality.getPetModifier();
          case PLAYING -> catPersonality.getPlayModifier();
          case TREAT, FEEDING, SLEEPING, SLEEPING_IN_BED -> catPersonality.getActivityModifier();
        };
    return Math.max(1, (int) (baseDelta * modifier));
  }

  public int getBaseDelta() {
    return baseDelta;
  }

  @Nullable
  public PersonalityType getPreferredPersonality() {
    return preferredPersonality;
  }
}
