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

public record CatNeedsData(float restNeed, float socialNeed, float playNeed, long lastNeedUpdate) {

  public static final float DEFAULT_REST_NEED = 20f;
  public static final float DEFAULT_SOCIAL_NEED = 30f;
  public static final float DEFAULT_PLAY_NEED = 25f;

  private static final float CRITICAL_THRESHOLD = 70f;

  public static CatNeedsData defaultNeeds() {
    return new CatNeedsData(DEFAULT_REST_NEED, DEFAULT_SOCIAL_NEED, DEFAULT_PLAY_NEED, 0L);
  }

  public CatNeedsData withRestNeed(float newRestNeed) {
    return new CatNeedsData(
        Math.clamp(newRestNeed, 0f, 100f), socialNeed, playNeed, lastNeedUpdate);
  }

  public CatNeedsData withSocialNeed(float newSocialNeed) {
    return new CatNeedsData(
        restNeed, Math.clamp(newSocialNeed, 0f, 100f), playNeed, lastNeedUpdate);
  }

  public CatNeedsData withPlayNeed(float newPlayNeed) {
    return new CatNeedsData(
        restNeed, socialNeed, Math.clamp(newPlayNeed, 0f, 100f), lastNeedUpdate);
  }

  public CatNeedsData withLastNeedUpdate(long newLastNeedUpdate) {
    return new CatNeedsData(restNeed, socialNeed, playNeed, newLastNeedUpdate);
  }

  public CatNeedsData withNeeds(
      float newRestNeed, float newSocialNeed, float newPlayNeed, long newLastNeedUpdate) {
    return new CatNeedsData(
        Math.clamp(newRestNeed, 0f, 100f),
        Math.clamp(newSocialNeed, 0f, 100f),
        Math.clamp(newPlayNeed, 0f, 100f),
        newLastNeedUpdate);
  }

  public CatNeedType getCriticalNeed() {
    if (restNeed > CRITICAL_THRESHOLD) {
      return CatNeedType.REST;
    }

    if (socialNeed > CRITICAL_THRESHOLD) {
      return CatNeedType.SOCIAL;
    }

    if (playNeed > CRITICAL_THRESHOLD) {
      return CatNeedType.PLAY;
    }

    return CatNeedType.NONE;
  }
}
