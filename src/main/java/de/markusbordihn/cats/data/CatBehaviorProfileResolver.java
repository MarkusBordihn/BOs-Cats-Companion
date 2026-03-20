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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatBehaviorProfileResolver {

  private static final float PRIMARY_WEIGHT = 0.7f;
  private static final float SECONDARY_WEIGHT = 0.3f;

  private CatBehaviorProfileResolver() {}

  @Nonnull
  public static CatBehaviorProfile resolve(
      @Nullable PersonalityType primary,
      @Nullable PersonalityType secondary,
      @Nullable HappinessLevel mood) {

    if (primary == null) {
      return CatBehaviorProfile.neutral();
    }

    CatBehaviorProfile result = primary.getBaseProfile();

    if (secondary != null) {
      result = blend(result, secondary.getBaseProfile());
    }

    if (mood != null) {
      result = applyMoodModifier(result, mood);
    }

    return result;
  }

  private static CatBehaviorProfile blend(
      CatBehaviorProfile primaryProfile, CatBehaviorProfile secondaryProfile) {
    return new CatBehaviorProfile(
        primaryProfile.activityWeight() * PRIMARY_WEIGHT
            + secondaryProfile.activityWeight() * SECONDARY_WEIGHT,
        primaryProfile.socialWeight() * PRIMARY_WEIGHT
            + secondaryProfile.socialWeight() * SECONDARY_WEIGHT,
        primaryProfile.playWeight() * PRIMARY_WEIGHT
            + secondaryProfile.playWeight() * SECONDARY_WEIGHT,
        primaryProfile.restWeight() * PRIMARY_WEIGHT
            + secondaryProfile.restWeight() * SECONDARY_WEIGHT,
        primaryProfile.ownerAffinity() * PRIMARY_WEIGHT
            + secondaryProfile.ownerAffinity() * SECONDARY_WEIGHT,
        primaryProfile.strangerDistance() * PRIMARY_WEIGHT
            + secondaryProfile.strangerDistance() * SECONDARY_WEIGHT,
        primaryProfile.bedPreference() * PRIMARY_WEIGHT
            + secondaryProfile.bedPreference() * SECONDARY_WEIGHT,
        primaryProfile.giftChance() * PRIMARY_WEIGHT
            + secondaryProfile.giftChance() * SECONDARY_WEIGHT,
        primaryProfile.wanderRadius() * PRIMARY_WEIGHT
            + secondaryProfile.wanderRadius() * SECONDARY_WEIGHT,
        primaryProfile.idleDurationSeconds() * PRIMARY_WEIGHT
            + secondaryProfile.idleDurationSeconds() * SECONDARY_WEIGHT);
  }

  private static CatBehaviorProfile applyMoodModifier(
      CatBehaviorProfile profile, HappinessLevel mood) {
    return switch (mood) {
      case ECSTATIC ->
          profile
              .withSocialWeight(profile.socialWeight() + 0.15f)
              .withPlayWeight(profile.playWeight() + 0.15f)
              .withGiftChance(profile.giftChance() + 0.15f);
      case HAPPY ->
          profile
              .withSocialWeight(profile.socialWeight() + 0.08f)
              .withPlayWeight(profile.playWeight() + 0.08f)
              .withGiftChance(profile.giftChance() + 0.08f);
      case NEUTRAL -> profile;
      case SAD ->
          profile
              .withActivityWeight(profile.activityWeight() - 0.10f)
              .withSocialWeight(profile.socialWeight() - 0.10f)
              .withRestWeight(profile.restWeight() + 0.08f);
      case MISERABLE ->
          profile
              .withActivityWeight(profile.activityWeight() - 0.20f)
              .withSocialWeight(profile.socialWeight() - 0.20f)
              .withRestWeight(profile.restWeight() + 0.15f);
    };
  }
}
