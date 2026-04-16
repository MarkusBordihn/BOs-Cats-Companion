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
import com.hypixel.hytale.codec.codecs.simple.DoubleCodec;
import com.hypixel.hytale.codec.codecs.simple.LongCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Cats;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CatFetchTargetComponent implements Component<EntityStore> {

  public static final String ID = "CatFetchTarget";

  private static final String BALL_X_TAG = "BallX";
  private static final String BALL_Y_TAG = "BallY";
  private static final String BALL_Z_TAG = "BallZ";
  private static final String OWNER_UUID_TAG = "OwnerUUID";
  private static final String RETURNING_TAG = "ReturningToPlayer";
  private static final String HAS_TARGET_TAG = "HasTarget";
  private static final String FETCH_START_TAG = "FetchStartMs";
  private static final String PICKUP_TIME_TAG = "PickupTimeMs";
  private static final String LAST_PATH_TAG = "LastPathMs";

  @Nonnull
  public static final BuilderCodec<CatFetchTargetComponent> CODEC =
      BuilderCodec.builder(CatFetchTargetComponent.class, CatFetchTargetComponent::new)
          .append(
              new KeyedCodec<>(HAS_TARGET_TAG, new BooleanCodec()),
              (comp, hasTarget) -> comp.hasTarget = hasTarget,
              comp -> comp.hasTarget)
          .documentation("Whether this component has an active fetch target")
          .add()
          .append(
              new KeyedCodec<>(BALL_X_TAG, new DoubleCodec()),
              (comp, ballX) -> comp.ballX = ballX,
              comp -> comp.ballX)
          .documentation("X coordinate where the yarn ball landed")
          .add()
          .append(
              new KeyedCodec<>(BALL_Y_TAG, new DoubleCodec()),
              (comp, ballY) -> comp.ballY = ballY,
              comp -> comp.ballY)
          .documentation("Y coordinate where the yarn ball landed")
          .add()
          .append(
              new KeyedCodec<>(BALL_Z_TAG, new DoubleCodec()),
              (comp, ballZ) -> comp.ballZ = ballZ,
              comp -> comp.ballZ)
          .documentation("Z coordinate where the yarn ball landed")
          .add()
          .append(
              new KeyedCodec<>(OWNER_UUID_TAG, new UUIDBinaryCodec()),
              (comp, ownerUuid) -> comp.ownerUuid = ownerUuid,
              comp -> comp.ownerUuid)
          .documentation("UUID of the player who threw the yarn ball")
          .add()
          .append(
              new KeyedCodec<>(RETURNING_TAG, new BooleanCodec()),
              (comp, returningToPlayer) -> comp.returningToPlayer = returningToPlayer,
              comp -> comp.returningToPlayer)
          .documentation("True when the cat has picked up the ball and is returning to the player")
          .add()
          .append(
              new KeyedCodec<>(FETCH_START_TAG, new LongCodec()),
              (comp, fetchStartMs) -> comp.fetchStartMs = fetchStartMs,
              comp -> comp.fetchStartMs)
          .documentation("Timestamp when fetch started")
          .add()
          .append(
              new KeyedCodec<>(PICKUP_TIME_TAG, new LongCodec()),
              (comp, pickupTimeMs) -> comp.pickupTimeMs = pickupTimeMs,
              comp -> comp.pickupTimeMs)
          .documentation("Timestamp when cat arrived at ball for pickup delay")
          .add()
          .append(
              new KeyedCodec<>(LAST_PATH_TAG, new LongCodec()),
              (comp, lastPathMs) -> comp.lastPathMs = lastPathMs,
              comp -> comp.lastPathMs)
          .documentation("Timestamp of last path recalculation")
          .add()
          .build();

  private double ballX;
  private double ballY;
  private double ballZ;
  @Nullable private UUID ownerUuid;
  private boolean returningToPlayer;
  private boolean hasTarget;
  private long fetchStartMs;
  private long pickupTimeMs;
  private long lastPathMs;

  public CatFetchTargetComponent() {
    this.hasTarget = false;
  }

  public CatFetchTargetComponent(@Nonnull Vector3d ballPosition, @Nonnull UUID ownerUuid) {
    this.ballX = ballPosition.x;
    this.ballY = ballPosition.y;
    this.ballZ = ballPosition.z;
    this.ownerUuid = ownerUuid;
    this.returningToPlayer = false;
    this.hasTarget = true;
    this.fetchStartMs = System.currentTimeMillis();
  }

  @Nullable
  public static ComponentType<EntityStore, CatFetchTargetComponent> getComponentType() {
    return Cats.getInstance().catFetchTargetComponentType;
  }

  @Nonnull
  public CatFetchTargetComponent asReturning() {
    CatFetchTargetComponent updated = clone();
    updated.returningToPlayer = true;
    return updated;
  }

  @Nonnull
  public Vector3d getBallPosition() {
    return new Vector3d(ballX, ballY, ballZ);
  }

  @Nullable
  public UUID getOwnerUuid() {
    return ownerUuid;
  }

  public boolean isReturningToPlayer() {
    return returningToPlayer;
  }

  public boolean hasTarget() {
    return hasTarget;
  }

  public long getFetchStartMs() {
    return fetchStartMs;
  }

  public long getPickupTimeMs() {
    return pickupTimeMs;
  }

  public void setPickupTimeMs(long pickupTimeMs) {
    this.pickupTimeMs = pickupTimeMs;
  }

  public long getLastPathMs() {
    return lastPathMs;
  }

  public void setLastPathMs(long lastPathMs) {
    this.lastPathMs = lastPathMs;
  }

  public void markReturning() {
    this.returningToPlayer = true;
    this.pickupTimeMs = 0;
    this.lastPathMs = 0;
  }

  @Override
  @Nonnull
  public CatFetchTargetComponent clone() {
    CatFetchTargetComponent clone = new CatFetchTargetComponent();
    clone.ballX = this.ballX;
    clone.ballY = this.ballY;
    clone.ballZ = this.ballZ;
    clone.ownerUuid = this.ownerUuid;
    clone.returningToPlayer = this.returningToPlayer;
    clone.hasTarget = this.hasTarget;
    clone.fetchStartMs = this.fetchStartMs;
    clone.pickupTimeMs = this.pickupTimeMs;
    clone.lastPathMs = this.lastPathMs;
    return clone;
  }
}
