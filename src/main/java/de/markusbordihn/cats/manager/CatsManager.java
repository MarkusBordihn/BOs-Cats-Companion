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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.component.PlayerCatsComponent;
import de.markusbordihn.cats.data.CatState;
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
  private static CatsManager instance;
  private final ComponentType<EntityStore, CatStateComponent> componentType;

  private final Set<Ref<EntityStore>> allCats = new HashSet<>();
  private final Map<UUID, Set<Ref<EntityStore>>> catsByOwner = new HashMap<>();
  private final Set<Ref<EntityStore>> catsWithoutOwner = new HashSet<>();

  public CatsManager(ComponentType<EntityStore, CatStateComponent> componentType) {
    this.componentType = componentType;
    instance = this;
  }

  @Nonnull
  public static CatsManager getInstance() {
    return instance;
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

  public void registerOwner(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID newOwnerUuid,
      @Nonnull Store<EntityStore> store) {

    // Find the old owner by searching through catsByOwner map
    UUID oldOwnerUuid = null;
    for (Map.Entry<UUID, Set<Ref<EntityStore>>> entry : catsByOwner.entrySet()) {
      if (entry.getValue().contains(catRef)) {
        oldOwnerUuid = entry.getKey();
        break;
      }
    }

    // Update catsByOwner tracking - remove from old owner if exists
    if (oldOwnerUuid != null && !oldOwnerUuid.equals(newOwnerUuid)) {
      Set<Ref<EntityStore>> oldOwnerCats = catsByOwner.get(oldOwnerUuid);
      if (oldOwnerCats != null) {
        oldOwnerCats.remove(catRef);
        if (oldOwnerCats.isEmpty()) {
          catsByOwner.remove(oldOwnerUuid);
        }
      }
    }

    // Add to new owner's cat list
    catsByOwner.computeIfAbsent(newOwnerUuid, k -> new HashSet<>()).add(catRef);
    catsWithoutOwner.remove(catRef);

    // Update PlayerCatsComponent
    UUIDComponent catUuidComponent = store.getComponent(catRef, UUIDComponent.getComponentType());
    if (catUuidComponent != null) {
      UUID catUuid = catUuidComponent.getUuid();

      // Remove from old owner
      if (oldOwnerUuid != null && !oldOwnerUuid.equals(newOwnerUuid)) {
        Ref<EntityStore> oldOwnerRef = findPlayerByUUID(store, oldOwnerUuid);
        if (oldOwnerRef != null) {
          PlayerCatsComponent oldPlayerCats =
              store.getComponent(oldOwnerRef, PlayerCatsComponent.getComponentType());
          if (oldPlayerCats != null) {
            oldPlayerCats.removeCat(catUuid);
            store.putComponent(oldOwnerRef, PlayerCatsComponent.getComponentType(), oldPlayerCats);
            LOGGER.at(Level.FINE).log("Removed cat %s from old owner %s", catUuid, oldOwnerUuid);
          }
        }
      }

      // Add to new owner
      Ref<EntityStore> newOwnerRef = findPlayerByUUID(store, newOwnerUuid);
      if (newOwnerRef != null) {
        PlayerCatsComponent newPlayerCats =
            store.getComponent(newOwnerRef, PlayerCatsComponent.getComponentType());
        if (newPlayerCats == null) {
          newPlayerCats = new PlayerCatsComponent();
        }
        newPlayerCats.addCat(catUuid);
        store.putComponent(newOwnerRef, PlayerCatsComponent.getComponentType(), newPlayerCats);
        LOGGER.at(Level.INFO).log(
            "Added cat %s to new owner %s PlayerCatsComponent", catUuid, newOwnerUuid);
      } else {
        LOGGER.at(Level.WARNING).log(
            "Could not find player entity for owner UUID %s - PlayerCatsComponent not updated",
            newOwnerUuid);
      }
    } else {
      LOGGER.at(Level.WARNING).log("Cat UUID component not found for cat %s", catRef);
    }

    LOGGER.at(Level.INFO).log(
        "Cat %s registered to owner %s in catsByOwner map", catRef, newOwnerUuid);
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

  public void assignOwner(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID ownerUuid,
      @Nonnull String ownerName,
      @Nullable String catName,
      @Nullable String targetState,
      @Nonnull Store<EntityStore> store) {

    // Set CatOwnerComponent
    CatOwnerComponent ownerComponent = new CatOwnerComponent(ownerUuid, ownerName, catName);
    store.putComponent(catRef, CatOwnerComponent.getComponentType(), ownerComponent);

    // Update nameplate if catName provided
    if (catName != null && !catName.isEmpty()) {
      Nameplate nameplate = store.ensureAndGetComponent(catRef, Nameplate.getComponentType());
      nameplate.setText(catName);
    }

    // Set cat state to FOLLOWING
    store.putComponent(
        catRef, CatStateComponent.getComponentType(), new CatStateComponent(CatState.FOLLOWING));

    // Register in CatsManager tracking and update PlayerCatsComponent
    registerOwner(catRef, ownerUuid, store);

    // Set NPC state if provided
    if (targetState != null) {
      NPCEntity npcEntity = store.getComponent(catRef, NPCEntity.getComponentType());
      if (npcEntity != null && npcEntity.getRole() != null) {
        npcEntity.getRole().getStateSupport().setState(catRef, targetState, "Default", store);
      }
    }

    LOGGER.at(Level.FINE).log(
        "Cat %s assigned to owner %s (name: %s, state: %s)",
        catRef, ownerName, catName, targetState);
  }

  private Ref<EntityStore> findPlayerByUUID(Store<EntityStore> store, UUID playerUuid) {
    final Ref<EntityStore>[] result = new Ref[1];
    store.forEachChunk(
        (chunk, buffer) -> {
          if (result[0] != null) {
            return;
          }
          for (int i = 0; i < chunk.size(); i++) {
            UUIDComponent uuidComponent = chunk.getComponent(i, UUIDComponent.getComponentType());
            if (uuidComponent != null && playerUuid.equals(uuidComponent.getUuid())) {
              result[0] = chunk.getReferenceTo(i);
              return;
            }
          }
        });
    return result[0];
  }
}
