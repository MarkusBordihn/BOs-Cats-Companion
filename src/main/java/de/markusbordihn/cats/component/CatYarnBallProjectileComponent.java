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

package de.markusbordihn.cats.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.UUIDBinaryCodec;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.codec.codecs.simple.LongCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Cats;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CatYarnBallProjectileComponent implements Component<EntityStore> {

  public static final String ID = "CatYarnBallProjectile";
  private static final String OWNER_UUID_TAG = "OwnerUUID";
  private static final String SPAWN_TIME_TAG = "SpawnTimeMs";
  private static final String FETCH_TRIGGERED_TAG = "FetchTriggered";
  private static final String REMOVE_REQUESTED_TAG = "RemoveRequested";

  @Nonnull
  public static final BuilderCodec<CatYarnBallProjectileComponent> CODEC =
      BuilderCodec.builder(
              CatYarnBallProjectileComponent.class, CatYarnBallProjectileComponent::new)
          .append(
              new KeyedCodec<>(OWNER_UUID_TAG, new UUIDBinaryCodec()),
              (comp, ownerUuid) -> comp.ownerUuid = ownerUuid,
              comp -> comp.ownerUuid)
          .documentation("UUID of the player who threw this yarn ball projectile")
          .add()
          .append(
              new KeyedCodec<>(SPAWN_TIME_TAG, new LongCodec()),
              (comp, spawnTimeMs) -> comp.spawnTimeMs = spawnTimeMs,
              comp -> comp.spawnTimeMs)
          .documentation("Timestamp when the yarn ball projectile was spawned")
          .add()
          .append(
              new KeyedCodec<>(FETCH_TRIGGERED_TAG, new BooleanCodec()),
              (comp, fetchTriggered) -> comp.fetchTriggered = fetchTriggered,
              comp -> comp.fetchTriggered)
          .documentation("Whether this projectile already triggered a fetch assignment")
          .add()
          .append(
              new KeyedCodec<>(REMOVE_REQUESTED_TAG, new BooleanCodec()),
              (comp, removeRequested) -> comp.removeRequested = removeRequested,
              comp -> comp.removeRequested)
          .documentation("Whether this projectile should be removed on the next safe tick")
          .add()
          .build();

  @Nullable private UUID ownerUuid;
  private long spawnTimeMs;
  private boolean fetchTriggered;
  private boolean removeRequested;

  public CatYarnBallProjectileComponent() {}

  public CatYarnBallProjectileComponent(@Nonnull UUID ownerUuid) {
    this.ownerUuid = ownerUuid;
    this.spawnTimeMs = System.currentTimeMillis();
  }

  @Nullable
  public static ComponentType<EntityStore, CatYarnBallProjectileComponent> getComponentType() {
    return Cats.getInstance().catYarnBallProjectileComponentType;
  }

  @Nullable
  public UUID getOwnerUuid() {
    return this.ownerUuid;
  }

  public long getSpawnTimeMs() {
    return this.spawnTimeMs;
  }

  public boolean isFetchTriggered() {
    return this.fetchTriggered;
  }

  public void setFetchTriggered(boolean fetchTriggered) {
    this.fetchTriggered = fetchTriggered;
  }

  public boolean isRemoveRequested() {
    return this.removeRequested;
  }

  public void setRemoveRequested(boolean removeRequested) {
    this.removeRequested = removeRequested;
  }

  @Override
  @Nonnull
  public CatYarnBallProjectileComponent clone() {
    CatYarnBallProjectileComponent clone = new CatYarnBallProjectileComponent();
    clone.ownerUuid = this.ownerUuid;
    clone.spawnTimeMs = this.spawnTimeMs;
    clone.fetchTriggered = this.fetchTriggered;
    clone.removeRequested = this.removeRequested;
    return clone;
  }
}
