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
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.HappinessLevel;
import de.markusbordihn.cats.data.HappinessSource;
import de.markusbordihn.cats.data.MoodData;
import de.markusbordihn.cats.data.PersonalityType;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class CatsHappinessManager {

  public static final long MOOD_DECAY_INTERVAL_MS = 20 * 60 * 1000L;
  public static final int MOOD_DECAY_AMOUNT = 1;

  private static final double OWNER_PROXIMITY_RADIUS_SQ = 16.0 * 16.0;
  private static final double WARMTH_RADIUS = 5.0;
  private static final Set<String> WARMTH_BLOCK_IDS =
      Set.of("Campfire", "Flame", "Torch", "Brazier");

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static double distanceSq(@Nonnull Vector3d a, @Nonnull Vector3d b) {
    double dx = b.x - a.x;
    double dy = b.y - a.y;
    double dz = b.z - a.z;
    return dx * dx + dy * dy + dz * dz;
  }

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

    long now = System.currentTimeMillis();
    if (catData.ownerUuid() != null) {
      Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(catData.ownerUuid());
      if (ownerRef == null || !ownerRef.isValid()) {
        return catData.happiness();
      }

      TransformComponent catTransform =
          store.getComponent(catRef, TransformComponent.getComponentType());
      TransformComponent ownerTransform =
          store.getComponent(ownerRef, TransformComponent.getComponentType());
      if (catTransform != null && ownerTransform != null) {
        if (distanceSq(catTransform.getPosition(), ownerTransform.getPosition())
            <= OWNER_PROXIMITY_RADIUS_SQ) {
          resource.updateCat(catUuid, catData.withLastMoodUpdate(now));
          return catData.happiness();
        }
      }
    }

    long lastUpdate = catData.lastMoodUpdate();
    if (lastUpdate > 0 && now > lastUpdate) {
      int decayTicks = (int) ((now - lastUpdate) / MOOD_DECAY_INTERVAL_MS);
      if (decayTicks > 0) {
        if (isNearWarmth(catRef, store)) {
          resource.updateCat(catUuid, catData.withLastMoodUpdate(now));
          return catData.happiness();
        }
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

  private boolean isNearWarmth(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    TransformComponent transform =
        store.getComponent(catRef, TransformComponent.getComponentType());
    if (transform == null) {
      return false;
    }

    World world = null;
    if (store.getExternalData() instanceof EntityStore entityStoreData) {
      world = entityStoreData.getWorld();
    }
    if (world == null) {
      return false;
    }

    Vector3d center = transform.getPosition();
    double radiusSq = WARMTH_RADIUS * WARMTH_RADIUS;
    int centerChunkX = (int) Math.floor(center.x) >> ChunkUtil.BITS;
    int centerChunkZ = (int) Math.floor(center.z) >> ChunkUtil.BITS;
    int chunkRadius = (int) Math.ceil(WARMTH_RADIUS / ChunkUtil.SIZE) + 1;

    for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
      for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
        int chunkX = centerChunkX + dx;
        int chunkZ = centerChunkZ + dz;
        long chunkIndex =
            ChunkUtil.indexChunkFromBlock(chunkX << ChunkUtil.BITS, chunkZ << ChunkUtil.BITS);

        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
        if (chunkRef == null) {
          continue;
        }

        Store<ChunkStore> chunkStore = chunkRef.getStore();
        WorldChunk worldChunk = chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());
        if (worldChunk == null) {
          continue;
        }

        BlockComponentChunk blockComponentChunk =
            chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
        if (blockComponentChunk == null) {
          continue;
        }

        Int2ReferenceMap<Ref<ChunkStore>> entityRefs = blockComponentChunk.getEntityReferences();
        if (entityRefs == null || entityRefs.isEmpty()) {
          continue;
        }

        for (Entry<Ref<ChunkStore>> entry : entityRefs.int2ReferenceEntrySet()) {
          int blockIndex = entry.getIntKey();
          int localX = ChunkUtil.xFromIndex(blockIndex);
          int localY = blockIndex / (ChunkUtil.SIZE * ChunkUtil.SIZE);
          int localZ = ChunkUtil.zFromColumn(blockIndex % ChunkUtil.SIZE_COLUMNS);
          int worldX = (chunkX << ChunkUtil.BITS) + localX;
          int worldZ = (chunkZ << ChunkUtil.BITS) + localZ;

          BlockType blockType = worldChunk.getBlockType(new Vector3i(worldX, localY, worldZ));
          if (blockType == null) {
            continue;
          }

          String blockTypeId = blockType.getId();
          if (blockTypeId == null) {
            continue;
          }

          for (String warmthId : WARMTH_BLOCK_IDS) {
            if (blockTypeId.contains(warmthId)) {
              Vector3d blockPos = new Vector3d(worldX + 0.5, localY, worldZ + 0.5);
              if (distanceSq(center, blockPos) <= radiusSq) {
                return true;
              }
              break;
            }
          }
        }
      }
    }

    return false;
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
