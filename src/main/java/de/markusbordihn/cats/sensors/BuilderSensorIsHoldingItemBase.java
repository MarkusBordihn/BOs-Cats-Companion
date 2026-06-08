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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderSensorIsHoldingItemBase extends BuilderSensorBase {

  protected abstract static class SensorIsHoldingItemBase extends SensorBase {

    protected SensorIsHoldingItemBase(BuilderSensorBase builder) {
      super(builder);
    }

    protected abstract boolean matchesActiveItem(@Nonnull ItemStack activeItem);

    protected boolean matchesEmptyHand() {
      return false;
    }

    @Override
    public final boolean matches(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        double dt,
        @Nonnull Store<EntityStore> store) {
      if (!super.matches(entityRef, role, dt, store)) {
        return false;
      }

      Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
      if (playerRef == null) {
        return false;
      }

      Player player = store.getComponent(playerRef, Player.getComponentType());
      if (player == null) {
        return false;
      }

      ItemStack activeItem = InventoryComponent.getItemInHand(store, playerRef);
      if (activeItem == null || activeItem.isEmpty()) {
        return matchesEmptyHand();
      }

      return matchesActiveItem(activeItem);
    }

    @Override
    @Nullable
    public InfoProvider getSensorInfo() {
      return null;
    }
  }
}
