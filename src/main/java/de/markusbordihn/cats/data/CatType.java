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
  UNKNOWN(""),
  BLACK("Cats_Black"),
  CALICO("Cats_Calico"),
  GRAY_TABBY("Cats_GrayTabby"),
  KITTEN("Cats_Kitten"),
  KITTEN_CALICO("Cats_Kitten_Calico"),
  LONGHAIRED_RUSSIAN_BLUE("Cats_LonghairedRussianBlue"),
  ORANGE_TABBY("Cats_OrangeTabby"),
  SIAMESE("Cats_Siamese"),
  TUXEDO("Cats_Tuxedo");

  public static final EnumCodec<CatType> CODEC = new EnumCodec<>(CatType.class);

  private final String roleName;

  CatType(String roleName) {
    this.roleName = roleName;
  }

  @Nonnull
  public static CatType fromRoleName(@Nullable String roleName) {
    if (roleName == null || roleName.isEmpty()) {
      return UNKNOWN;
    }
    for (CatType type : values()) {
      if (type.roleName.equals(roleName)) {
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
  public String getRoleName() {
    return roleName;
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
