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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatNeedsComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.CatNeedsData;
import de.markusbordihn.cats.data.PersonalityType;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class CatNeedsManager {

  static final float REST_DECAY_PER_MIN = 0.2f;
  static final float SOCIAL_DECAY_PER_MIN = 0.2f;
  static final float PLAY_DECAY_PER_MIN = 0.125f;

  private static final float SLEEP_REST_REDUCE_PER_MIN = 1.0f;
  private static final float BED_SLEEP_REST_REDUCE_PER_MIN = 1.5f;
  private static final float SOCIAL_SATISFY_PER_MIN = 0.5f;
  private static final float COMPANION_SOCIAL_SATISFY_PER_MIN = 0.3f;
  private static final double OWNER_PROXIMITY_RADIUS_SQ = 8.0 * 8.0;
  private static final double COMPANION_PROXIMITY_RADIUS_SQ = 6.0 * 6.0;

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public boolean updateNeeds(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return false;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    CatDataEntry catData = resource.getCat(catUuid);
    if (catData == null) {
      return false;
    }

    long now = System.currentTimeMillis();
    long lastUpdate = catData.lastNeedUpdate();
    if (lastUpdate <= 0) {
      resource.updateCat(catUuid, catData.withLastNeedUpdate(now));
      syncNeedsComponent(catRef, catData, store);
      return false;
    }

    float deltaMinutes = (now - lastUpdate) / 60_000f;
    if (deltaMinutes < 0.01f) {
      return false;
    }

    PersonalityType personality = catData.personalityType();
    boolean isSleeping = catData.state().isSleepingState();
    boolean ownerNearby = isOwnerNearby(catRef, catData, store);
    boolean companionNearby = hasCompanionNearby(catRef, catUuid, store);
    float restDecay = REST_DECAY_PER_MIN * getPersonalityModifier(personality, CatNeedType.REST);
    float socialDecay =
        SOCIAL_DECAY_PER_MIN * getPersonalityModifier(personality, CatNeedType.SOCIAL);
    float playDecay = PLAY_DECAY_PER_MIN * getPersonalityModifier(personality, CatNeedType.PLAY);
    float newRest = catData.restNeed();
    float newSocial = catData.socialNeed();
    float newPlay = catData.playNeed();

    if (isSleeping) {
      boolean inBed =
          catData.state() == de.markusbordihn.cats.data.CatState.SLEEPING
              && catData.position() != null;
      float restReduce = inBed ? BED_SLEEP_REST_REDUCE_PER_MIN : SLEEP_REST_REDUCE_PER_MIN;
      newRest = Math.clamp(newRest - restReduce * deltaMinutes, 0f, 100f);
    } else {
      newRest = Math.clamp(newRest + restDecay * deltaMinutes, 0f, 100f);
    }

    if (ownerNearby || companionNearby) {
      float socialSatisfy = ownerNearby ? SOCIAL_SATISFY_PER_MIN : COMPANION_SOCIAL_SATISFY_PER_MIN;
      socialSatisfy *= getPersonalityModifier(personality, CatNeedType.SOCIAL);
      newSocial = Math.clamp(newSocial - socialSatisfy * deltaMinutes, 0f, 100f);
    } else {
      newSocial = Math.clamp(newSocial + socialDecay * deltaMinutes, 0f, 100f);
    }

    newPlay = Math.clamp(newPlay + playDecay * deltaMinutes, 0f, 100f);
    boolean changed =
        Math.abs(newRest - catData.restNeed()) > 0.001f
            || Math.abs(newSocial - catData.socialNeed()) > 0.001f
            || Math.abs(newPlay - catData.playNeed()) > 0.001f;

    if (changed) {
      CatDataEntry updated = catData.withNeeds(newRest, newSocial, newPlay, now);
      resource.updateCat(catUuid, updated);
      syncNeedsComponent(catRef, updated, store);
      LOGGER.at(Level.FINE).log(
          "Cat %s needs updated: rest=%.1f social=%.1f play=%.1f",
          catUuid, newRest, newSocial, newPlay);
    } else {
      resource.updateCat(catUuid, catData.withLastNeedUpdate(now));
    }

    return changed;
  }

  public void satisfyNeed(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull CatNeedType needType,
      float amount,
      @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    CatDataEntry catData = resource.getCat(catUuid);
    if (catData == null) {
      return;
    }

    long now = System.currentTimeMillis();
    CatDataEntry updated =
        switch (needType) {
          case REST ->
              catData.withNeeds(
                  catData.restNeed() - amount, catData.socialNeed(), catData.playNeed(), now);
          case SOCIAL ->
              catData.withNeeds(
                  catData.restNeed(), catData.socialNeed() - amount, catData.playNeed(), now);
          case PLAY ->
              catData.withNeeds(
                  catData.restNeed(), catData.socialNeed(), catData.playNeed() - amount, now);
          case NONE -> catData;
        };

    if (updated != catData) {
      resource.updateCat(catUuid, updated);
      syncNeedsComponent(catRef, updated, store);
    }
  }

  @Nonnull
  public CatNeedType getCriticalNeed(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    CatNeedsComponent needsComponent =
        store.getComponent(catRef, CatNeedsComponent.getComponentType());
    return needsComponent != null ? needsComponent.getCriticalNeed() : CatNeedType.NONE;
  }

  @Nonnull
  public CatNeedType getHighestNeed(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    CatNeedsComponent needsComponent =
        store.getComponent(catRef, CatNeedsComponent.getComponentType());
    return needsComponent != null ? needsComponent.getHighestNeed() : CatNeedType.NONE;
  }

  private float getPersonalityModifier(
      @Nullable PersonalityType personality, @Nonnull CatNeedType needType) {
    if (personality == null) {
      return 1.0f;
    }

    return switch (needType) {
      case REST -> personality == PersonalityType.LAZY ? 1.5f : 1.0f;
      case SOCIAL ->
          switch (personality) {
            case CUDDLY -> 1.5f;
            case INDEPENDENT -> 0.5f;
            default -> 1.0f;
          };
      case PLAY -> personality == PersonalityType.PLAYFUL ? 1.5f : 1.0f;
      case NONE -> 1.0f;
    };
  }

  private boolean isOwnerNearby(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull CatDataEntry catData,
      @Nonnull Store<EntityStore> store) {
    if (catData.ownerUuid() == null) {
      return false;
    }

    Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(catData.ownerUuid());
    if (ownerRef == null || !ownerRef.isValid()) {
      return false;
    }

    TransformComponent catTransform =
        store.getComponent(catRef, TransformComponent.getComponentType());
    TransformComponent ownerTransform =
        store.getComponent(ownerRef, TransformComponent.getComponentType());
    if (catTransform == null || ownerTransform == null) {
      return false;
    }

    Vector3d catPos = catTransform.getPosition();
    Vector3d ownerPos = ownerTransform.getPosition();
    double dx = ownerPos.x - catPos.x;
    double dy = ownerPos.y - catPos.y;
    double dz = ownerPos.z - catPos.z;
    return (dx * dx + dy * dy + dz * dz) <= OWNER_PROXIMITY_RADIUS_SQ;
  }

  private boolean hasCompanionNearby(
      @Nonnull Ref<EntityStore> catRef, @Nonnull UUID catUuid, @Nonnull Store<EntityStore> store) {
    TransformComponent catTransform =
        store.getComponent(catRef, TransformComponent.getComponentType());
    if (catTransform == null) {
      return false;
    }

    Vector3d catPos = catTransform.getPosition();
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    for (CatDataEntry otherCat : resource.getAllCats()) {
      if (otherCat.uuid().equals(catUuid) || !otherCat.isSpawned()) {
        continue;
      }

      Ref<EntityStore> otherRef = store.getExternalData().getRefFromUUID(otherCat.uuid());
      if (otherRef == null || !otherRef.isValid()) {
        continue;
      }

      TransformComponent otherTransform =
          store.getComponent(otherRef, TransformComponent.getComponentType());
      if (otherTransform == null) {
        continue;
      }

      Vector3d otherPos = otherTransform.getPosition();
      double dx = otherPos.x - catPos.x;
      double dy = otherPos.y - catPos.y;
      double dz = otherPos.z - catPos.z;
      if ((dx * dx + dy * dy + dz * dz) <= COMPANION_PROXIMITY_RADIUS_SQ) {
        return true;
      }
    }
    return false;
  }

  private void syncNeedsComponent(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull CatDataEntry catData,
      @Nonnull Store<EntityStore> store) {
    CatNeedsData needsData =
        new CatNeedsData(
            catData.restNeed(), catData.socialNeed(), catData.playNeed(), catData.lastNeedUpdate());
    CatNeedsComponent needsComponent =
        store.getComponent(catRef, CatNeedsComponent.getComponentType());
    if (needsComponent != null) {
      needsComponent.setData(needsData);
    } else {
      store.putComponent(
          catRef, CatNeedsComponent.getComponentType(), new CatNeedsComponent(needsData));
    }
  }

  @Nullable
  private UUID getUuid(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    return component != null ? component.getUuid() : null;
  }
}
