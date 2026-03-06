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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.CatStatus;
import de.markusbordihn.cats.data.CatType;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.Collection;
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
  private final Map<UUID, Ref<EntityStore>> catRefCache = new HashMap<>();

  public CatsManager(ComponentType<EntityStore, CatStateComponent> componentType) {
    this.componentType = componentType;
    instance = this;
  }

  @Nullable
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
    UUID entityUuid = getUuid(ref, store);
    if (entityUuid != null) {
      catRefCache.put(entityUuid, ref);
      CatOwnerComponent ownerComponent =
          store.getComponent(ref, CatOwnerComponent.getComponentType());
      if (ownerComponent != null) {
        NPCEntity npcEntity = store.getComponent(ref, NPCEntity.getComponentType());
        if (npcEntity != null && ownerComponent.hasOwner()) {
          npcEntity.setSpawnConfiguration(Integer.MIN_VALUE);
          npcEntity.updateSpawnTrackingState(false);
          LOGGER.at(Level.FINE).log(
              "Disabled spawn tracking for tamed cat UUID %s (Owner: %s)",
              entityUuid, ownerComponent.getOwnerName());
        }

        CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
        if (resource != null && resource.getCat(entityUuid) == null) {
          registerCat(ref, store);
          LOGGER.at(Level.INFO).log(
              "Auto-registered missing cat entry for UUID %s (Owner: %s)",
              entityUuid, ownerComponent.getOwnerName());
        }
      }
    }
  }

  @Override
  public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull RemoveReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    UUID entityUuid = getUuid(ref, store);
    if (entityUuid != null) {
      catRefCache.remove(entityUuid);
      CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
      if (resource != null) {
        CatDataEntry catData = resource.getCat(entityUuid);
        if (catData != null && reason == RemoveReason.REMOVE) {
          if (catData.status() == CatStatus.SPAWNED) {
            resource.updateCat(entityUuid, catData.withStatus(CatStatus.DESPAWNED));
          }
        }
      }
    }
  }

  @Nullable
  public Ref<EntityStore> getCatByUuid(
      @Nonnull UUID entityUuid, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> cached = catRefCache.get(entityUuid);
    if (cached != null && cached.isValid()) {
      return cached;
    }

    Ref<EntityStore> resolved = store.getExternalData().getRefFromUUID(entityUuid);
    if (resolved != null && resolved.isValid()) {
      catRefCache.put(entityUuid, resolved);
      return resolved;
    }
    return null;
  }

  @Nonnull
  public Set<Ref<EntityStore>> getCatsByOwner(
      @Nonnull UUID ownerUuid, @Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      return Collections.emptySet();
    }

    Set<Ref<EntityStore>> catRefs = new HashSet<>();
    for (CatDataEntry catDataEntry : resource.getCatsByOwner(ownerUuid)) {
      if (catDataEntry.isSpawned()) {
        Ref<EntityStore> catRef = getCatByUuid(catDataEntry.uuid(), store);
        if (catRef != null && catRef.isValid()) {
          catRefs.add(catRef);
        }
      }
    }
    return catRefs;
  }

  @Nonnull
  public Set<Ref<EntityStore>> getCatsWithoutOwner(@Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      return Collections.emptySet();
    }

    Set<Ref<EntityStore>> catsWithoutOwner = new HashSet<>();
    for (CatDataEntry catDataEntry : resource.getCatsWithoutOwner()) {
      if (catDataEntry.isSpawned()) {
        Ref<EntityStore> catRef = getCatByUuid(catDataEntry.uuid(), store);
        if (catRef != null && catRef.isValid()) {
          catsWithoutOwner.add(catRef);
        }
      }
    }
    return catsWithoutOwner;
  }

  @Nonnull
  public Set<Ref<EntityStore>> getAllCats(@Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      return Collections.emptySet();
    }

    Set<Ref<EntityStore>> allCats = new HashSet<>();
    for (CatDataEntry catDataEntry : resource.getSpawnedCats()) {
      Ref<EntityStore> catRef = getCatByUuid(catDataEntry.uuid(), store);
      if (catRef != null && catRef.isValid()) {
        allCats.add(catRef);
      }
    }
    return allCats;
  }

  public void registerOwner(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID newOwnerUuid,
      @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      LOGGER.at(Level.WARNING).log("Cat UUID not found for %s", catRef);
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      LOGGER.at(Level.WARNING).log("CatsDataResource not available");
      return;
    }

    CatDataEntry catDataEntry = resource.getCat(catUuid);
    if (catDataEntry != null) {
      resource.updateCat(catUuid, catDataEntry.withOwnerUuid(newOwnerUuid));
    }
  }

  public void unregisterOwner(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource != null) {
      CatDataEntry catDataEntry = resource.getCat(catUuid);
      if (catDataEntry != null) {
        resource.updateCat(catUuid, catDataEntry.withOwner(null, null));
      }
    }
  }

  public int getCatCount(@Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    return resource != null ? resource.getTotalCatCount() : 0;
  }

  public int getCatCountByOwner(@Nonnull UUID ownerUuid, @Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    return resource != null ? resource.getOwnedCatCount(ownerUuid) : 0;
  }

  public void registerCat(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      LOGGER.at(Level.WARNING).log("Cannot register cat - cat has no UUID");
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      LOGGER.at(Level.WARNING).log("CatsDataResource not available");
      return;
    }

    // Read current cat data from components
    CatOwnerComponent ownerComponent =
        store.getComponent(catRef, CatOwnerComponent.getComponentType());
    UUID ownerUuid = ownerComponent != null ? ownerComponent.getOwnerUUID() : null;
    String ownerName = ownerComponent != null ? ownerComponent.getOwnerName() : null;

    Nameplate nameplate = store.getComponent(catRef, Nameplate.getComponentType());
    String catName = nameplate != null ? nameplate.getText() : null;

    CatStateComponent stateComponent =
        store.getComponent(catRef, CatStateComponent.getComponentType());
    CatState catState = stateComponent != null ? stateComponent.getState() : CatState.FOLLOWING;

    NPCEntity npcEntity = store.getComponent(catRef, NPCEntity.getComponentType());
    CatType catType = CatType.UNKNOWN;
    if (npcEntity != null && npcEntity.getRole() != null) {
      catType = CatType.fromRoleName(npcEntity.getRole().getRoleName());
    }

    // Create or update entry in CatsDataResource
    CatDataEntry existingEntry = resource.getCat(catUuid);
    if (existingEntry != null) {
      resource.updateCat(
          catUuid,
          existingEntry
              .withOwner(ownerUuid, ownerName)
              .withName(catName)
              .withState(catState)
              .withStatus(CatStatus.SPAWNED));
    } else {
      CatDataEntry newEntry =
          new CatDataEntry(
              catUuid,
              ownerUuid,
              ownerName,
              catType,
              catName,
              catState,
              getPosition(catRef, store),
              CatStatus.SPAWNED);
      resource.addCat(newEntry);
    }
  }

  public void assignOwner(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID ownerUuid,
      @Nonnull String ownerName,
      @Nullable String catName,
      @Nullable String targetState,
      @Nonnull Store<EntityStore> store) {

    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      LOGGER.at(Level.WARNING).log("Cannot assign owner - cat has no UUID");
      return;
    }

    store.putComponent(
        catRef, CatOwnerComponent.getComponentType(), new CatOwnerComponent(ownerUuid, ownerName));

    if (catName != null && !catName.isEmpty()) {
      store.ensureAndGetComponent(catRef, Nameplate.getComponentType()).setText(catName);
    }

    store.putComponent(
        catRef, CatStateComponent.getComponentType(), new CatStateComponent(CatState.FOLLOWING));
    registerOwner(catRef, ownerUuid, store);

    if (targetState != null) {
      NPCEntity npcEntity = store.getComponent(catRef, NPCEntity.getComponentType());
      if (npcEntity != null && npcEntity.getRole() != null) {
        npcEntity.getRole().getStateSupport().setState(catRef, targetState, "Default", store);
      }
    }

    // Register/update cat in CatsDataResource with current state
    registerCat(catRef, store);
  }

  public void updateCatName(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull String catName,
      @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource != null) {
      CatDataEntry catDataEntry = resource.getCat(catUuid);
      if (catDataEntry != null) {
        resource.updateCat(catUuid, catDataEntry.withName(catName));
      }
    }
  }

  public void updateCatState(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull CatState state,
      @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    store.putComponent(catRef, CatStateComponent.getComponentType(), new CatStateComponent(state));

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource != null) {
      CatDataEntry catDataEntry = resource.getCat(catUuid);
      if (catDataEntry != null) {
        resource.updateCat(catUuid, catDataEntry.withState(state));
      }
    }
  }

  @Nullable
  public UUID getUuid(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    return component != null ? component.getUuid() : null;
  }

  @Nullable
  public String getCatName(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    Nameplate nameplate = store.getComponent(catRef, Nameplate.getComponentType());
    if (nameplate != null) {
      String name = nameplate.getText();
      if (name != null && !name.isEmpty()) {
        return name;
      }
    }
    return null;
  }

  @Nonnull
  public String getCatDisplayName(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    String name = getCatName(catRef, store);
    return name != null ? name : "Cat";
  }

  @Nullable
  public CatDataEntry getCatData(@Nonnull UUID catUuid, @Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    return resource != null ? resource.getCat(catUuid) : null;
  }

  @Nullable
  public CatDataEntry getCatData(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    return catUuid != null ? getCatData(catUuid, store) : null;
  }

  @Nonnull
  public Collection<CatDataEntry> getCatDataByOwner(
      @Nonnull UUID ownerUuid, @Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    return resource != null ? resource.getCatsByOwner(ownerUuid) : Collections.emptyList();
  }

  public void despawnCat(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    Vector3i position = getPosition(catRef, store);
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource != null) {
      CatDataEntry catData = resource.getCat(catUuid);
      if (catData != null) {
        resource.updateCat(catUuid, catData.withStatus(CatStatus.DESPAWNED).withPosition(position));
      }
    }
  }

  public void updateCatUuid(
      @Nonnull UUID oldUuid, @Nonnull UUID newUuid, @Nonnull Store<EntityStore> store) {
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource != null) {
      CatDataEntry catData = resource.getCat(oldUuid);
      if (catData != null) {
        resource.removeCat(oldUuid);
        resource.addCat(catData.withUuid(newUuid).withStatus(CatStatus.SPAWNED));
      }
    }
  }

  @Nullable
  private Vector3i getPosition(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
    if (transform != null) {
      Vector3d pos = transform.getPosition();
      return new Vector3i((int) pos.x, (int) pos.y, (int) pos.z);
    }
    return null;
  }

  public boolean isCatAliveInWorld(@Nonnull UUID catUuid, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> catRef = getCatByUuid(catUuid, store);
    if (catRef == null || !catRef.isValid()) {
      return false;
    }

    EntityStatMap statMap = store.getComponent(catRef, EntityStatMap.getComponentType());
    if (statMap == null) {
      return false;
    }

    EntityStatValue healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
    return healthStat != null && healthStat.get() > 0;
  }
}
