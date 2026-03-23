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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatTypeTest {

  @Test
  @DisplayName("fromRoleName null returns UNKNOWN")
  void fromRoleName_null_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromRoleName(null));
  }

  @Test
  @DisplayName("fromRoleName empty returns UNKNOWN")
  void fromRoleName_empty_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromRoleName(""));
  }

  @Test
  @DisplayName("fromRoleName recognizes tamed roles")
  void fromRoleName_tamedRole_returnsCorrectType() {
    assertEquals(CatType.GENERIC_CAT, CatType.fromRoleName("Cats_Tamed"));
    assertEquals(CatType.BLACK, CatType.fromRoleName("Cats_Black_Tamed"));
    assertEquals(CatType.SIAMESE, CatType.fromRoleName("Cats_Siamese_Tamed"));
    assertEquals(CatType.TUXEDO, CatType.fromRoleName("Cats_Tuxedo_Tamed"));
  }

  @Test
  @DisplayName("fromRoleName recognizes wild roles")
  void fromRoleName_wildRole_returnsCorrectType() {
    assertEquals(CatType.GENERIC_CAT, CatType.fromRoleName("Cats_Wild"));
    assertEquals(CatType.BLACK, CatType.fromRoleName("Cats_Black_Wild"));
    assertEquals(CatType.CALICO, CatType.fromRoleName("Cats_Calico_Wild"));
  }

  @Test
  @DisplayName("fromRoleName returns UNKNOWN for invalid roles")
  void fromRoleName_unknown_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromRoleName("NonExistent"));
    assertEquals(CatType.UNKNOWN, CatType.fromRoleName("cats_tamed"));
  }

  @Test
  @DisplayName("fromString null returns UNKNOWN")
  void fromString_null_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromString(null));
  }

  @Test
  @DisplayName("fromString empty returns UNKNOWN")
  void fromString_empty_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromString(""));
  }

  @Test
  @DisplayName("fromString recognizes uppercase names")
  void fromString_valid_returnsType() {
    assertEquals(CatType.BLACK, CatType.fromString("BLACK"));
    assertEquals(CatType.SIAMESE, CatType.fromString("SIAMESE"));
    assertEquals(CatType.TUXEDO, CatType.fromString("TUXEDO"));
  }

  @Test
  @DisplayName("fromString is case-insensitive")
  void fromString_lowercase_returnsType() {
    assertEquals(CatType.BLACK, CatType.fromString("black"));
    assertEquals(CatType.CALICO, CatType.fromString("calico"));
  }

  @Test
  @DisplayName("fromString handles space-separated names")
  void fromString_withSpaces_returnsType() {
    assertEquals(CatType.GRAY_TABBY, CatType.fromString("gray tabby"));
    assertEquals(CatType.ORANGE_TABBY, CatType.fromString("orange tabby"));
    assertEquals(CatType.KITTEN_CALICO, CatType.fromString("kitten calico"));
  }

  @Test
  @DisplayName("fromString returns UNKNOWN for invalid names")
  void fromString_invalid_returnsUnknown() {
    assertEquals(CatType.UNKNOWN, CatType.fromString("DRAGON"));
    assertEquals(CatType.UNKNOWN, CatType.fromString("random_string"));
  }

  @Test
  @DisplayName("getRoleName returns tamed role when true")
  void getRoleName_tamed_returnsTamedRole() {
    assertEquals("Cats_Black_Tamed", CatType.BLACK.getRoleName(true));
    assertEquals("Cats_Siamese_Tamed", CatType.SIAMESE.getRoleName(true));
  }

  @Test
  @DisplayName("getRoleName returns wild role when false")
  void getRoleName_wild_returnsWildRole() {
    assertEquals("Cats_Black_Wild", CatType.BLACK.getRoleName(false));
    assertEquals("Cats_Siamese_Wild", CatType.SIAMESE.getRoleName(false));
  }

  @Test
  @DisplayName("toString returns 'Unknown' for UNKNOWN")
  void toString_unknown_returnsUnknown() {
    assertEquals("Unknown", CatType.UNKNOWN.toString());
  }

  @Test
  @DisplayName("All cat types have valid role names")
  void allTypes_tamedAndWildRoleNamesNotNull() {
    for (CatType type : CatType.values()) {
      assertNotNull(type.getTamedRoleName());
      assertNotNull(type.getWildRoleName());
    }
  }
}
