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

package de.markusbordihn.cats.manager;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CatsManager extends RefSystem<EntityStore> {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, CatStateComponent> componentType;

  private final Set<Ref<EntityStore>> allCats = new HashSet<>();
  private final Map<UUID, Set<Ref<EntityStore>>> catsByOwner = new HashMap<>();
  private final Set<Ref<EntityStore>> catsWithoutOwner = new HashSet<>();

  public CatsManager(ComponentType<EntityStore, CatStateComponent> componentType) {
    this.componentType = componentType;
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return componentType;
  }

  @Override
  public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AddReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    allCats.add(ref);

    CatOwnerComponent ownerComponent =
        store.getComponent(ref, CatOwnerComponent.getComponentType());
    if (ownerComponent != null && ownerComponent.hasOwner()) {
      UUID ownerUuid = ownerComponent.getOwnerId();
      if (ownerUuid != null) {
        catsByOwner.computeIfAbsent(ownerUuid, k -> new HashSet<>()).add(ref);
        LOGGER.at(Level.FINE).log("Cat registered with owner - Ref: %s, Owner: %s", ref, ownerUuid);
      } else {
        catsWithoutOwner.add(ref);
      }
    } else {
      catsWithoutOwner.add(ref);
    }

    LOGGER.at(Level.FINE).log(
        "Cat registered - Ref: %s, Total: %d, Without owner: %d",
        ref, allCats.size(), catsWithoutOwner.size());
  }

  @Override
  public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull RemoveReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    allCats.remove(ref);
    catsWithoutOwner.remove(ref);
    catsByOwner.values().forEach(set -> set.remove(ref));

    LOGGER.at(Level.FINE).log("Cat unregistered - Ref: %s, Total: %d", ref, allCats.size());
  }

  @Nonnull
  public Set<Ref<EntityStore>> getCatsByOwner(@Nonnull UUID ownerUuid) {
    return catsByOwner.getOrDefault(ownerUuid, Collections.emptySet());
  }

  @Nonnull
  public Set<Ref<EntityStore>> getCatsWithoutOwner() {
    return Collections.unmodifiableSet(catsWithoutOwner);
  }

  @Nonnull
  public Set<Ref<EntityStore>> getAllCats() {
    return Collections.unmodifiableSet(allCats);
  }

  public void registerOwner(@Nonnull Ref<EntityStore> catRef, @Nonnull UUID ownerUuid) {
    catsByOwner.computeIfAbsent(ownerUuid, k -> new HashSet<>()).add(catRef);
    catsWithoutOwner.remove(catRef);

    LOGGER.at(Level.FINE).log("Cat %s registered to owner %s", catRef, ownerUuid);
  }

  public void unregisterOwner(@Nonnull Ref<EntityStore> catRef, @Nullable UUID ownerUuid) {
    if (ownerUuid != null) {
      Set<Ref<EntityStore>> ownerCats = catsByOwner.get(ownerUuid);
      if (ownerCats != null) {
        ownerCats.remove(catRef);
        if (ownerCats.isEmpty()) {
          catsByOwner.remove(ownerUuid);
        }
      }
    } else {
      catsByOwner.values().forEach(set -> set.remove(catRef));
    }

    if (allCats.contains(catRef)) {
      catsWithoutOwner.add(catRef);
    }

    LOGGER.at(Level.FINE).log("Cat %s unregistered from owner", catRef);
  }

  public int getCatCount() {
    return allCats.size();
  }

  public int getCatCountByOwner(@Nonnull UUID ownerUuid) {
    return catsByOwner.getOrDefault(ownerUuid, Collections.emptySet()).size();
  }
}
