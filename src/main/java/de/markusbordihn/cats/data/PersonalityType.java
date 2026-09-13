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

import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.concurrent.ThreadLocalRandom;

public enum PersonalityType {
  LAZY(0.8f, 1.2f, 0.6f),
  PLAYFUL(1.3f, 0.8f, 1.4f),
  SHY(0.7f, 1.0f, 0.5f),
  BRAVE(1.1f, 0.9f, 1.2f),
  CURIOUS(1.2f, 0.9f, 1.1f),
  CUDDLY(1.0f, 1.1f, 0.9f),
  INDEPENDENT(0.9f, 0.7f, 1.0f),
  MISCHIEVOUS(1.4f, 0.7f, 1.3f);

  public static final EnumCodec<PersonalityType> CODEC = new EnumCodec<>(PersonalityType.class);

  private final float activityModifier;
  private final float petModifier;
  private final float playModifier;

  PersonalityType(float activityModifier, float petModifier, float playModifier) {
    this.activityModifier = activityModifier;
    this.petModifier = petModifier;
    this.playModifier = playModifier;
  }

  public static PersonalityType random() {
    PersonalityType[] values = values();
    return values[ThreadLocalRandom.current().nextInt(values.length)];
  }

  public float getActivityModifier() {
    return this.activityModifier;
  }

  public float getPetModifier() {
    return this.petModifier;
  }

  public float getPlayModifier() {
    return this.playModifier;
  }

  public CatBehaviorProfile getBaseProfile() {
    return switch (this) {
      case LAZY -> CatBehaviorProfile.baseLazy();
      case PLAYFUL -> CatBehaviorProfile.basePlayful();
      case SHY -> CatBehaviorProfile.baseShy();
      case BRAVE -> CatBehaviorProfile.baseBrave();
      case CURIOUS -> CatBehaviorProfile.baseCurious();
      case CUDDLY -> CatBehaviorProfile.baseCuddly();
      case INDEPENDENT -> CatBehaviorProfile.baseIndependent();
      case MISCHIEVOUS -> CatBehaviorProfile.baseMischievous();
    };
  }
}
