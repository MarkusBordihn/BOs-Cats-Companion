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

package de.markusbordihn.cats.blocks;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatBedInfo;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.world.NearbyBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public final class CatBed {
  public static final double DEFAULT_SEARCH_RADIUS = 50.0;
  public static final double BED_OCCUPIED_RADIUS = 1.0;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final double BED_OCCUPIED_RADIUS_SQ = BED_OCCUPIED_RADIUS * BED_OCCUPIED_RADIUS;
  private static final double[] NEAREST_BED_SEARCH_RADII = {
    1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 25.0, 50.0
  };

  private CatBed() {}

  @Nullable
  public static Vector3d findNearestBedPosition(
      @Nonnull World world, @Nonnull Vector3d searchCenter) {
    for (double radius : NEAREST_BED_SEARCH_RADII) {
      List<CatBedInfo> beds = findCatBeds(world, searchCenter, radius);
      if (!beds.isEmpty()) {
        return beds.get(0).getPosition();
      }
    }

    return null;
  }

  @Nonnull
  public static List<CatBedInfo> findCatBeds(
      @Nonnull World world, @Nonnull Vector3d searchCenter, double searchRadius) {
    List<CatBedInfo> beds = new ArrayList<>();
    NearbyBlockEntities.forEachWithinRadius(
        world,
        searchCenter,
        searchRadius,
        (blockTypeId, blockPosition, distanceSquared) -> {
          if (!blockTypeId.contains("Cat_Bed")) {
            return true;
          }

          double distance = Math.sqrt(distanceSquared);
          LOGGER.at(Level.FINE).log(
              "Found cat bed '%s' at (%.1f, %.1f, %.1f) distance %.1f blocks",
              blockTypeId, blockPosition.x, blockPosition.y, blockPosition.z, distance);
          beds.add(new CatBedInfo(blockPosition, distance));
          return true;
        });

    LOGGER.at(Level.FINE).log("Found %d cat beds in range", beds.size());

    beds.sort((first, second) -> Double.compare(first.distance(), second.distance()));
    return beds;
  }

  @Nonnull
  public static List<CatBedInfo> findCatBeds(@Nonnull World world, @Nonnull Vector3d searchCenter) {
    return findCatBeds(world, searchCenter, DEFAULT_SEARCH_RADIUS);
  }

  @Nullable
  public static CatBedInfo findNearestAvailableBed(
      @Nonnull List<CatBedInfo> beds,
      @Nonnull Store<EntityStore> store,
      @Nonnull Vector3d currentCatPos) {
    if (beds.isEmpty()) {
      return null;
    }

    List<Vector3d> restingCatPositions = collectRestingCatPositions(store, currentCatPos);
    for (CatBedInfo bed : beds) {
      if (!isBedOccupied(bed.position(), restingCatPositions)) {
        return bed;
      }
    }

    return null;
  }

  @Nonnull
  private static List<Vector3d> collectRestingCatPositions(
      @Nonnull Store<EntityStore> store, @Nonnull Vector3d excludeCatPos) {
    List<Vector3d> positions = new ArrayList<>();

    store.forEachChunk(
        (ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> buffer) -> {
          for (int i = 0; i < chunk.size(); i++) {
            CatStateComponent stateComponent =
                chunk.getComponent(i, CatStateComponent.getComponentType());
            if (stateComponent == null) {
              continue;
            }

            CatState state = stateComponent.getState();
            if (state == CatState.WANDERING || state == CatState.FOLLOWING) {
              continue;
            }

            TransformComponent transformComponent =
                chunk.getComponent(i, TransformComponent.getComponentType());
            if (transformComponent == null) {
              continue;
            }

            Vector3d catPosition = transformComponent.getPosition();
            if (distanceSquared(catPosition, excludeCatPos) < 0.01) {
              continue;
            }

            positions.add(new Vector3d(catPosition));
          }
        });

    return positions;
  }

  private static boolean isBedOccupied(
      @Nonnull Vector3d bedPos, @Nonnull List<Vector3d> restingCatPositions) {
    for (Vector3d catPosition : restingCatPositions) {
      if (distanceSquared(catPosition, bedPos) <= BED_OCCUPIED_RADIUS_SQ) {
        return true;
      }
    }

    return false;
  }

  public static double distanceSquared(@Nonnull Vector3d a, @Nonnull Vector3d b) {
    double dx = a.x - b.x;
    double dy = a.y - b.y;
    double dz = a.z - b.z;
    return dx * dx + dy * dy + dz * dz;
  }

  public static double distance(@Nonnull Vector3d a, @Nonnull Vector3d b) {
    return Math.sqrt(distanceSquared(a, b));
  }
}
