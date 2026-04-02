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

package de.markusbordihn.cats.sensors;

import com.google.gson.JsonElement;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.component.CatOwnerComponent;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

public class BuilderSensorOwnerPlayer extends BuilderSensorBase {

  public static final String SENSOR_ID = "CatsOwnerPlayer";

  private final FloatHolder range = new FloatHolder();
  private final BooleanHolder lockOnTarget = new BooleanHolder();

  @Nonnull
  @Override
  public Sensor build(@Nonnull BuilderSupport support) {
    return new SensorOwnerPlayer(this, support);
  }

  @Nonnull
  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  @Override
  public BuilderSensorOwnerPlayer readConfig(@Nonnull JsonElement data) {
    this.requireFloat(
        data,
        "Range",
        this.range,
        null,
        BuilderDescriptorState.Stable,
        "Search radius in blocks to find the owner",
        null);
    this.getBoolean(
        data,
        "LockOnTarget",
        this.lockOnTarget,
        false,
        BuilderDescriptorState.Stable,
        "If true, locks the found owner as the NPC movement target",
        null);
    return this;
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Matches when the cat's owner is within range";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Scans nearby players within Range and returns true only if the cat's owner (by UUID) "
        + "is found. If LockOnTarget is true, locks the owner as the movement target.";
  }

  public float getRange(@Nonnull BuilderSupport builderSupport) {
    return this.range.get(builderSupport.getExecutionContext());
  }

  public boolean isLockOnTarget(@Nonnull BuilderSupport builderSupport) {
    return this.lockOnTarget.get(builderSupport.getExecutionContext());
  }

  public static class SensorOwnerPlayer extends SensorBase {

    private final float range;
    private final boolean lockOnTarget;

    public SensorOwnerPlayer(
        @Nonnull BuilderSensorOwnerPlayer builder, @Nonnull BuilderSupport builderSupport) {
      super(builder);
      this.range = builder.getRange(builderSupport);
      this.lockOnTarget = builder.isLockOnTarget(builderSupport);
    }

    @Override
    public void registerWithSupport(@Nonnull Role role) {
      super.registerWithSupport(role);
      role.getPositionCache().requirePlayerDistanceSorted(this.range);
    }

    @Override
    public boolean matches(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        double dt,
        @Nonnull Store<EntityStore> store) {
      if (!super.matches(entityRef, role, dt, store)) {
        return false;
      }

      CatOwnerComponent ownerComponent =
          store.getComponent(entityRef, CatOwnerComponent.getComponentType());
      if (ownerComponent == null || !ownerComponent.hasOwner()) {
        return false;
      }

      UUID ownerUUID = ownerComponent.getOwnerUUID();
      if (ownerUUID == null) {
        return false;
      }

      AtomicReference<Ref<EntityStore>> foundOwner = new AtomicReference<>(null);
      role.getPositionCache()
          .processPlayersInRange(
              entityRef,
              0,
              this.range,
              false,
              null,
              role,
              (sensorOwnerPlayer, playerRef, lambdaRole, playerStore) -> {
                PlayerRef playerRefComponent =
                    playerStore.getComponent(playerRef, PlayerRef.getComponentType());
                if (playerRefComponent != null && ownerUUID.equals(playerRefComponent.getUuid())) {
                  foundOwner.set(playerRef);
                  return true;
                }
                return false;
              },
              this,
              store,
              store);

      Ref<EntityStore> ownerRef = foundOwner.get();
      if (ownerRef == null) {
        return false;
      }

      if (this.lockOnTarget) {
        role.getMarkedEntitySupport()
            .setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, ownerRef);
      }
      return true;
    }

    @Override
    public InfoProvider getSensorInfo() {
      return null;
    }
  }
}
