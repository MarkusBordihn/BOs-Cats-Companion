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
import javax.annotation.Nullable;

public enum CatType {
  UNKNOWN("", ""),
  GENERIC_CAT("Cats_Tamed", "Cats_Wild"),
  BLACK("Cats_Black_Tamed", "Cats_Black_Wild"),
  CALICO("Cats_Calico_Tamed", "Cats_Calico_Wild"),
  GRAY_TABBY("Cats_GrayTabby_Tamed", "Cats_GrayTabby_Wild"),
  KITTEN("Cats_Kitten_Tamed", "Cats_Kitten_Wild"),
  KITTEN_CALICO("Cats_Kitten_Calico_Tamed", "Cats_Kitten_Calico_Wild"),
  LONGHAIRED_RUSSIAN_BLUE("Cats_LonghairedRussianBlue_Tamed", "Cats_LonghairedRussianBlue_Wild"),
  ORANGE_TABBY("Cats_OrangeTabby_Tamed", "Cats_OrangeTabby_Wild"),
  SIAMESE("Cats_Siamese_Tamed", "Cats_Siamese_Wild"),
  TUXEDO("Cats_Tuxedo_Tamed", "Cats_Tuxedo_Wild");

  public static final EnumCodec<CatType> CODEC = new EnumCodec<>(CatType.class);

  private final String tamedRoleName;
  private final String wildRoleName;

  CatType(String tamedRoleName, String wildRoleName) {
    this.tamedRoleName = tamedRoleName;
    this.wildRoleName = wildRoleName;
  }

  @Nonnull
  public static CatType fromRoleName(@Nullable String roleName) {
    if (roleName == null || roleName.isEmpty()) {
      return UNKNOWN;
    }
    for (CatType type : values()) {
      if (type.tamedRoleName.equals(roleName) || type.wildRoleName.equals(roleName)) {
        return type;
      }
    }
    return UNKNOWN;
  }

  public static CatType fromString(String type) {
    if (type == null || type.isEmpty()) {
      return UNKNOWN;
    }
    try {
      return CatType.valueOf(type.toUpperCase(Locale.ROOT).replace(" ", "_"));
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }

  @Nonnull
  public String getTamedRoleName() {
    return tamedRoleName;
  }

  @Nonnull
  public String getWildRoleName() {
    return wildRoleName;
  }

  @Nonnull
  public String getRoleName(boolean isTamed) {
    return isTamed ? tamedRoleName : wildRoleName;
  }

  @Nonnull
  @Deprecated
  public String getRoleName() {
    return tamedRoleName;
  }

  @Override
  public String toString() {
    if (this == UNKNOWN) {
      return "Unknown";
    }
    String name = name().toLowerCase(Locale.ROOT).replace("_", " ");
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }
}
