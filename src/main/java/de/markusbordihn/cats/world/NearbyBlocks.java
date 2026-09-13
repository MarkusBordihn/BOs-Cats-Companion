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

package de.markusbordihn.cats.world;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public final class NearbyBlocks {

  private NearbyBlocks() {}

  public static void forEachWithinRadius(
      @Nonnull World world,
      @Nonnull Vector3d center,
      double radius,
      @Nonnull Predicate<String> blockTypeFilter,
      @Nonnull BlockVisitor visitor) {
    double radiusSquared = radius * radius;
    int minChunkX = ChunkUtil.chunkCoordinate(center.x - radius);
    int maxChunkX = ChunkUtil.chunkCoordinate(center.x + radius);
    int minChunkZ = ChunkUtil.chunkCoordinate(center.z - radius);
    int maxChunkZ = ChunkUtil.chunkCoordinate(center.z + radius);
    int minBlockY = (int) Math.floor(center.y - radius);
    int maxBlockY = (int) Math.floor(center.y + radius);
    int minSectionIndex = Math.max(ChunkUtil.MIN_SECTION, ChunkUtil.indexSection(minBlockY));
    int maxSectionIndex =
        Math.min(ChunkUtil.HEIGHT_SECTIONS - 1, ChunkUtil.indexSection(maxBlockY));

    Int2ObjectMap<String> matchingBlockTypeIds = new Int2ObjectOpenHashMap<>();
    boolean[] stopped = {false};

    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
        if (worldChunk == null) {
          continue;
        }

        BlockChunk blockChunk = worldChunk.getBlockChunk();
        if (blockChunk == null) {
          continue;
        }

        final int chunkBaseX = chunkX << ChunkUtil.BITS;
        final int chunkBaseZ = chunkZ << ChunkUtil.BITS;

        for (int sectionIndex = minSectionIndex; sectionIndex <= maxSectionIndex; sectionIndex++) {
          BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
          if (section == null) {
            continue;
          }

          IntArrayList matchingBlockIds =
              collectMatchingBlockIds(section, blockTypeFilter, matchingBlockTypeIds);
          if (matchingBlockIds.isEmpty()) {
            continue;
          }

          final int sectionBaseY = sectionIndex << ChunkUtil.BITS;
          section.find(
              matchingBlockIds,
              (blockIndex, blockId) -> {
                if (stopped[0]) {
                  return;
                }

                Vector3d blockPosition =
                    new Vector3d(
                        chunkBaseX + ChunkUtil.xFromIndex(blockIndex) + 0.5,
                        sectionBaseY + ChunkUtil.yFromIndex(blockIndex),
                        chunkBaseZ + ChunkUtil.zFromIndex(blockIndex) + 0.5);
                double distanceSquared = blockPosition.distanceSquared(center);
                if (distanceSquared > radiusSquared) {
                  return;
                }

                if (!visitor.visit(
                    matchingBlockTypeIds.get(blockId), blockPosition, distanceSquared)) {
                  stopped[0] = true;
                }
              });

          if (stopped[0]) {
            return;
          }
        }
      }
    }
  }

  @Nonnull
  private static IntArrayList collectMatchingBlockIds(
      @Nonnull BlockSection section,
      @Nonnull Predicate<String> blockTypeFilter,
      @Nonnull Int2ObjectMap<String> matchingBlockTypeIds) {
    IntArrayList matchingBlockIds = new IntArrayList();
    IntIterator blockIdIterator = section.values().iterator();

    while (blockIdIterator.hasNext()) {
      int blockId = blockIdIterator.nextInt();
      if (resolveMatchingBlockTypeId(blockId, blockTypeFilter, matchingBlockTypeIds) != null) {
        matchingBlockIds.add(blockId);
      }
    }

    return matchingBlockIds;
  }

  @Nullable
  private static String resolveMatchingBlockTypeId(
      int blockId,
      @Nonnull Predicate<String> blockTypeFilter,
      @Nonnull Int2ObjectMap<String> matchingBlockTypeIds) {
    if (matchingBlockTypeIds.containsKey(blockId)) {
      return matchingBlockTypeIds.get(blockId);
    }

    BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
    String blockTypeId = blockType != null ? blockType.getId() : null;
    String matchingBlockTypeId =
        blockTypeId != null && blockTypeFilter.test(blockTypeId) ? blockTypeId : null;
    matchingBlockTypeIds.put(blockId, matchingBlockTypeId);

    return matchingBlockTypeId;
  }

  @FunctionalInterface
  public interface BlockVisitor {
    boolean visit(
        @Nonnull String blockTypeId, @Nonnull Vector3d blockPosition, double distanceSquared);
  }
}
