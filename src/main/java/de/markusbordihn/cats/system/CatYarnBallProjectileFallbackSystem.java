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

package de.markusbordihn.cats.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatYarnBallProjectileComponent;
import de.markusbordihn.cats.interaction.CatYarnBallFetchInteraction;
import java.util.UUID;
import javax.annotation.Nonnull;

public class CatYarnBallProjectileFallbackSystem extends EntityTickingSystem<EntityStore> {

  private static final long FETCH_FALLBACK_DELAY_MS = 5_000L;
  private static final long PROJECTILE_DESPAWN_TIMEOUT_MS = 60_000L;

  private final ComponentType<EntityStore, CatYarnBallProjectileComponent> projectileType;

  public CatYarnBallProjectileFallbackSystem(
      @Nonnull ComponentType<EntityStore, CatYarnBallProjectileComponent> projectileType) {
    this.projectileType = projectileType;
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return this.projectileType;
  }

  @Override
  public void tick(
      float deltaTime,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> chunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    CatYarnBallProjectileComponent projectileData = chunk.getComponent(index, this.projectileType);
    if (projectileData == null) {
      return;
    }

    Ref<EntityStore> projectileRef = chunk.getReferenceTo(index);
    if (projectileRef == null || !projectileRef.isValid()) {
      return;
    }

    if (projectileData.isRemoveRequested()) {
      commandBuffer.removeEntity(projectileRef, RemoveReason.REMOVE);
      return;
    }

    long ageMs = System.currentTimeMillis() - projectileData.getSpawnTimeMs();
    if (ageMs >= PROJECTILE_DESPAWN_TIMEOUT_MS) {
      commandBuffer.removeEntity(projectileRef, RemoveReason.REMOVE);
      return;
    }

    if (projectileData.isFetchTriggered() || ageMs < FETCH_FALLBACK_DELAY_MS) {
      return;
    }

    UUID ownerUuid = projectileData.getOwnerUuid();
    if (ownerUuid == null) {
      return;
    }

    TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
    if (transform == null) {
      return;
    }

    CatYarnBallFetchInteraction.handleLanding(
        ownerUuid, projectileRef, transform.getPosition(), commandBuffer, store);
  }
}
