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

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record CatOwnerData(
    @Nullable UUID ownerId,
    @Nullable String ownerName,
    @Nullable String catName,
    long tamedTimestamp) {

  @Nonnull
  public static CatOwnerData empty() {
    return new CatOwnerData(null, null, null, 0);
  }

  @Nonnull
  public static CatOwnerData create(UUID ownerId, String ownerName) {
    return new CatOwnerData(ownerId, ownerName, null, System.currentTimeMillis());
  }

  @Nonnull
  public static CatOwnerData create(UUID ownerId, String ownerName, String catName) {
    return new CatOwnerData(ownerId, ownerName, catName, System.currentTimeMillis());
  }

  public boolean hasOwner() {
    return ownerId != null;
  }

  @Nonnull
  public CatOwnerData withOwnerId(UUID ownerId) {
    return new CatOwnerData(ownerId, ownerName, catName, tamedTimestamp);
  }

  @Nonnull
  public CatOwnerData withOwnerName(String ownerName) {
    return new CatOwnerData(ownerId, ownerName, catName, tamedTimestamp);
  }

  @Nonnull
  public CatOwnerData withCatName(String catName) {
    return new CatOwnerData(ownerId, ownerName, catName, tamedTimestamp);
  }

  @Nonnull
  public CatOwnerData withTamedTimestamp(long tamedTimestamp) {
    return new CatOwnerData(ownerId, ownerName, catName, tamedTimestamp);
  }

  @Nonnull
  public CatOwnerData withOwner(UUID ownerId, String ownerName) {
    return new CatOwnerData(ownerId, ownerName, catName, System.currentTimeMillis());
  }
}
