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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.HappinessLevel;
import de.markusbordihn.cats.data.HappinessSource;
import de.markusbordihn.cats.data.MoodData;
import de.markusbordihn.cats.data.PersonalityType;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CatsHappinessManager {

  public static final long MOOD_DECAY_INTERVAL_MS = 10 * 60 * 1000L;
  public static final int MOOD_DECAY_AMOUNT = 1;

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public void adjustHappiness(
      @Nonnull Ref<EntityStore> catRef, int delta, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      return;
    }

    CatDataEntry catData = resource.getCat(catUuid);
    if (catData == null) {
      return;
    }

    int newHappiness =
        Math.clamp(catData.happiness() + delta, MoodData.MIN_HAPPINESS, MoodData.MAX_HAPPINESS);
    long now = System.currentTimeMillis();
    resource.updateCat(catUuid, catData.withHappiness(newHappiness).withLastMoodUpdate(now));
    syncMoodComponent(catRef, newHappiness, now, store);
  }

  public int boostHappiness(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull HappinessSource source,
      @Nonnull Store<EntityStore> store) {
    CatDataEntry catData = getCatDataEntry(catRef, store);
    PersonalityType personality = catData != null ? catData.personalityType() : null;
    int delta = source.calculateDelta(personality);
    adjustHappiness(catRef, delta, store);
    return delta;
  }

  public int getHappiness(@Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return MoodData.DEFAULT_HAPPINESS;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      return MoodData.DEFAULT_HAPPINESS;
    }

    CatDataEntry catData = resource.getCat(catUuid);
    if (catData == null) {
      return MoodData.DEFAULT_HAPPINESS;
    }

    if (catData.state().isSleepingState()) {
      return catData.happiness();
    }

    if (catData.ownerUuid() != null) {
      Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(catData.ownerUuid());
      if (ownerRef == null || !ownerRef.isValid()) {
        return catData.happiness();
      }
    }

    long now = System.currentTimeMillis();
    long lastUpdate = catData.lastMoodUpdate();
    if (lastUpdate > 0 && now > lastUpdate) {
      int decayTicks = (int) ((now - lastUpdate) / MOOD_DECAY_INTERVAL_MS);
      if (decayTicks > 0) {
        int decayed =
            Math.max(
                MoodData.MIN_HAPPINESS, catData.happiness() - (decayTicks * MOOD_DECAY_AMOUNT));
        resource.updateCat(catUuid, catData.withHappiness(decayed).withLastMoodUpdate(now));
        LOGGER.at(Level.FINE).log(
            "Cat %s happiness decayed by %d", catUuid, decayTicks * MOOD_DECAY_AMOUNT);
        return decayed;
      }
    }

    return catData.happiness();
  }

  @Nonnull
  public HappinessLevel getHappinessLevel(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    return HappinessLevel.fromValue(getHappiness(catRef, store));
  }

  @Nullable
  private CatDataEntry getCatDataEntry(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    UUID catUuid = getUuid(catRef, store);
    if (catUuid == null) {
      return null;
    }
    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    return resource != null ? resource.getCat(catUuid) : null;
  }

  private void syncMoodComponent(
      @Nonnull Ref<EntityStore> catRef,
      int happiness,
      long timestamp,
      @Nonnull Store<EntityStore> store) {
    CatMoodComponent moodComponent =
        store.getComponent(catRef, CatMoodComponent.getComponentType());
    if (moodComponent != null) {
      moodComponent.setData(new MoodData(happiness, timestamp));
    } else {
      store.putComponent(
          catRef, CatMoodComponent.getComponentType(), new CatMoodComponent(happiness));
    }
  }

  @Nullable
  private UUID getUuid(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    return component != null ? component.getUuid() : null;
  }
}
