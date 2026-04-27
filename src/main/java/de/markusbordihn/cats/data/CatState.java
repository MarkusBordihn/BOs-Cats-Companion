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
import java.util.Locale;
import javax.annotation.Nonnull;

public enum CatState {
  ATTACKING,
  FETCHING,
  FOLLOWING,
  GOING_TO_BED,
  PLAYING,
  SEARCHING,
  SITTING,
  SLEEPING,
  WAITING,
  WANDERING;

  public static final EnumCodec<CatState> CODEC = new EnumCodec<>(CatState.class);

  @Nonnull private final String translationKey;

  CatState() {
    this.translationKey = "cats.ui.state." + name().toLowerCase(Locale.ROOT);
  }

  @Nonnull
  public String getTranslationKey() {
    return translationKey;
  }

  public boolean isSleepingState() {
    return this == SLEEPING || this == GOING_TO_BED;
  }
}
