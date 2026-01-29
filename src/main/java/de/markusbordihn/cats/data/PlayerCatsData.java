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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public record PlayerCatsData(@Nonnull Set<UUID> ownedCatUUIDs) {

  @Nonnull
  public static PlayerCatsData empty() {
    return new PlayerCatsData(new HashSet<>());
  }

  @Nonnull
  public static PlayerCatsData fromSet(@Nonnull Set<UUID> uuidSet) {
    return new PlayerCatsData(new HashSet<>(uuidSet));
  }

  @Nonnull
  public Set<UUID> getOwnedCatUUIDs() {
    return new HashSet<>(ownedCatUUIDs);
  }

  public int getCatCount() {
    return ownedCatUUIDs.size();
  }

  public boolean hasCat(@Nonnull UUID catUuid) {
    return ownedCatUUIDs.contains(catUuid);
  }

  @Nonnull
  public PlayerCatsData withAddedCat(@Nonnull UUID catUuid) {
    Set<UUID> newSet = new HashSet<>(ownedCatUUIDs);
    newSet.add(catUuid);
    return new PlayerCatsData(newSet);
  }

  @Nonnull
  public PlayerCatsData withRemovedCat(@Nonnull UUID catUuid) {
    Set<UUID> newSet = new HashSet<>(ownedCatUUIDs);
    newSet.remove(catUuid);
    return new PlayerCatsData(newSet);
  }

  @Nonnull
  public Set<UUID> getOwnedCats() {
    return Collections.unmodifiableSet(ownedCatUUIDs);
  }
}
