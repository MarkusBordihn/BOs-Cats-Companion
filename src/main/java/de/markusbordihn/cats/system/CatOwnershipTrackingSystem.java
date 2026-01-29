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

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatOwnerComponent;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class CatOwnershipTrackingSystem extends RefSystem<EntityStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, CatOwnerComponent> componentType;

  public CatOwnershipTrackingSystem(ComponentType<EntityStore, CatOwnerComponent> componentType) {
    this.componentType = componentType;
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return componentType;
  }

  @Override
  public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AddReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    // Nothing to do on add
  }

  @Override
  public void onEntityRemove(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull RemoveReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    CatOwnerComponent ownerComponent = store.getComponent(catRef, componentType);
    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      return;
    }

    UUID ownerId = ownerComponent.getOwnerId();
    if (ownerId == null) {
      return;
    }

    UUIDComponent catUuidComponent = store.getComponent(catRef, UUIDComponent.getComponentType());
    if (catUuidComponent == null || catUuidComponent.getUuid() == null) {
      LOGGER.at(Level.WARNING).log("Cat being removed has no UUID, cannot update owner tracking");
      return;
    }
    LOGGER.at(Level.FINE).log(
        "Cat %s removed from owner %s (reason: %s) - PlayerCatsComponent not updated",
        catUuidComponent.getUuid(), ownerId, reason);
  }
}
