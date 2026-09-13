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

public record CatBehaviorProfile(
    float activityWeight,
    float socialWeight,
    float playWeight,
    float restWeight,
    float ownerAffinity,
    float strangerDistance,
    float bedPreference,
    float giftChance,
    float wanderRadius,
    float idleDurationSeconds) {

  public CatBehaviorProfile {
    activityWeight = Math.clamp(activityWeight, 0.0f, 1.0f);
    socialWeight = Math.clamp(socialWeight, 0.0f, 1.0f);
    playWeight = Math.clamp(playWeight, 0.0f, 1.0f);
    restWeight = Math.clamp(restWeight, 0.0f, 1.0f);
    ownerAffinity = Math.clamp(ownerAffinity, 0.0f, 1.0f);
    strangerDistance = Math.clamp(strangerDistance, 2.0f, 12.0f);
    bedPreference = Math.clamp(bedPreference, 0.0f, 1.0f);
    giftChance = Math.clamp(giftChance, 0.0f, 1.0f);
    wanderRadius = Math.clamp(wanderRadius, 5.0f, 25.0f);
    idleDurationSeconds = Math.clamp(idleDurationSeconds, 5.0f, 40.0f);
  }

  @Nonnull
  public static CatBehaviorProfile neutral() {
    return new CatBehaviorProfile(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 5.0f, 0.5f, 0.5f, 15.0f, 20.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseLazy() {
    return new CatBehaviorProfile(
        0.30f, 0.50f, 0.30f, 0.80f, 0.50f, 5.0f, 0.80f, 0.40f, 8.0f, 30.0f);
  }

  @Nonnull
  public static CatBehaviorProfile basePlayful() {
    return new CatBehaviorProfile(
        0.80f, 0.60f, 0.85f, 0.25f, 0.55f, 4.0f, 0.30f, 0.50f, 18.0f, 8.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseShy() {
    return new CatBehaviorProfile(
        0.40f, 0.35f, 0.30f, 0.60f, 0.70f, 10.0f, 0.65f, 0.25f, 8.0f, 25.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseBrave() {
    return new CatBehaviorProfile(
        0.75f, 0.50f, 0.65f, 0.35f, 0.45f, 3.0f, 0.35f, 0.55f, 22.0f, 10.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseCurious() {
    return new CatBehaviorProfile(
        0.70f, 0.55f, 0.60f, 0.35f, 0.50f, 5.0f, 0.30f, 0.45f, 25.0f, 8.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseCuddly() {
    return new CatBehaviorProfile(
        0.50f, 0.85f, 0.45f, 0.55f, 0.85f, 3.0f, 0.60f, 0.55f, 10.0f, 20.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseIndependent() {
    return new CatBehaviorProfile(
        0.60f, 0.30f, 0.45f, 0.45f, 0.25f, 8.0f, 0.40f, 0.35f, 22.0f, 15.0f);
  }

  @Nonnull
  public static CatBehaviorProfile baseMischievous() {
    return new CatBehaviorProfile(
        0.85f, 0.40f, 0.75f, 0.20f, 0.35f, 4.0f, 0.20f, 0.60f, 20.0f, 6.0f);
  }

  public CatBehaviorProfile withActivityWeight(float newActivityWeight) {
    return new CatBehaviorProfile(
        newActivityWeight,
        socialWeight,
        playWeight,
        restWeight,
        ownerAffinity,
        strangerDistance,
        bedPreference,
        giftChance,
        wanderRadius,
        idleDurationSeconds);
  }

  public CatBehaviorProfile withSocialWeight(float newSocialWeight) {
    return new CatBehaviorProfile(
        activityWeight,
        newSocialWeight,
        playWeight,
        restWeight,
        ownerAffinity,
        strangerDistance,
        bedPreference,
        giftChance,
        wanderRadius,
        idleDurationSeconds);
  }

  public CatBehaviorProfile withPlayWeight(float newPlayWeight) {
    return new CatBehaviorProfile(
        activityWeight,
        socialWeight,
        newPlayWeight,
        restWeight,
        ownerAffinity,
        strangerDistance,
        bedPreference,
        giftChance,
        wanderRadius,
        idleDurationSeconds);
  }

  public CatBehaviorProfile withRestWeight(float newRestWeight) {
    return new CatBehaviorProfile(
        activityWeight,
        socialWeight,
        playWeight,
        newRestWeight,
        ownerAffinity,
        strangerDistance,
        bedPreference,
        giftChance,
        wanderRadius,
        idleDurationSeconds);
  }

  public CatBehaviorProfile withGiftChance(float newGiftChance) {
    return new CatBehaviorProfile(
        activityWeight,
        socialWeight,
        playWeight,
        restWeight,
        ownerAffinity,
        strangerDistance,
        bedPreference,
        newGiftChance,
        wanderRadius,
        idleDurationSeconds);
  }

}
