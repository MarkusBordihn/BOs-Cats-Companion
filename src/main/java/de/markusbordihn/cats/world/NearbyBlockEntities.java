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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;

public final class NearbyBlockEntities {

  private NearbyBlockEntities() {}

  public static void forEachWithinRadius(
      @Nonnull World world,
      @Nonnull Vector3d center,
      double radius,
      @Nonnull BlockEntityVisitor visitor) {
    double radiusSq = radius * radius;
    int centerChunkX = (int) Math.floor(center.x) >> ChunkUtil.BITS;
    int centerChunkZ = (int) Math.floor(center.z) >> ChunkUtil.BITS;
    int chunkRadius = (int) Math.ceil(radius / ChunkUtil.SIZE) + 1;

    for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
      for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
        int chunkX = centerChunkX + dx;
        int chunkZ = centerChunkZ + dz;
        WorldChunk worldChunk =
            world.getChunkIfInMemory(
                ChunkUtil.indexChunkFromBlock(chunkX << ChunkUtil.BITS, chunkZ << ChunkUtil.BITS));
        if (worldChunk == null) {
          continue;
        }

        BlockComponentChunk blockComponentChunk = worldChunk.getBlockComponentChunk();
        if (blockComponentChunk == null) {
          continue;
        }

        Int2ReferenceMap<Ref<ChunkStore>> entityRefs = blockComponentChunk.getEntityReferences();
        if (entityRefs == null || entityRefs.isEmpty()) {
          continue;
        }

        for (Entry<Ref<ChunkStore>> entry : entityRefs.int2ReferenceEntrySet()) {
          int blockIndex = entry.getIntKey();
          int worldX = (chunkX << ChunkUtil.BITS) + ChunkUtil.xFromBlockInColumn(blockIndex);
          int worldY = ChunkUtil.yFromBlockInColumn(blockIndex);
          int worldZ = (chunkZ << ChunkUtil.BITS) + ChunkUtil.zFromBlockInColumn(blockIndex);

          BlockType blockType = worldChunk.getBlockType(new Vector3i(worldX, worldY, worldZ));
          if (blockType == null || blockType.getId() == null) {
            continue;
          }

          Vector3d blockPosition = new Vector3d(worldX + 0.5, worldY, worldZ + 0.5);
          double distanceSq = blockPosition.distanceSquared(center);
          if (distanceSq > radiusSq) {
            continue;
          }

          if (!visitor.visit(blockType.getId(), blockPosition, distanceSq)) {
            return;
          }
        }
      }
    }
  }

  @FunctionalInterface
  public interface BlockEntityVisitor {
    boolean visit(
        @Nonnull String blockTypeId, @Nonnull Vector3d blockPosition, double distanceSquared);
  }
}
