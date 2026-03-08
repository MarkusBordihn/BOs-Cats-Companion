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

public record MoodData(int happiness, long lastMoodUpdate) {

  public static final int DEFAULT_HAPPINESS = 50;
  public static final int MIN_HAPPINESS = 0;
  public static final int MAX_HAPPINESS = 100;

  @Nonnull
  public static MoodData defaultMood() {
    return new MoodData(DEFAULT_HAPPINESS, System.currentTimeMillis());
  }

  @Nonnull
  public HappinessLevel getLevel() {
    return HappinessLevel.fromValue(happiness);
  }

  @Nonnull
  public MoodData withHappiness(int newHappiness) {
    return new MoodData(Math.clamp(newHappiness, MIN_HAPPINESS, MAX_HAPPINESS), lastMoodUpdate);
  }

  @Nonnull
  public MoodData withLastMoodUpdate(long newLastMoodUpdate) {
    return new MoodData(happiness, newLastMoodUpdate);
  }

  @Nonnull
  public MoodData adjustHappiness(int delta) {
    return new MoodData(
        Math.clamp(happiness + delta, MIN_HAPPINESS, MAX_HAPPINESS), System.currentTimeMillis());
  }
}
