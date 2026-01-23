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
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatOwnerComponent;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class CatOwnershipSystem extends RefSystem<EntityStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, CatOwnerComponent> componentType;

  public CatOwnershipSystem(ComponentType<EntityStore, CatOwnerComponent> componentType) {
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
    LOGGER.at(Level.FINE).log("Cat entity added to world");
  }

  @Override
  public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull RemoveReason reason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {}

  public boolean isOwner(
      @Nonnull ComponentAccessor<EntityStore> accessor,
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID playerId) {
    CatOwnerComponent ownerComponent = accessor.getComponent(catRef, componentType);
    return ownerComponent != null
        && ownerComponent.hasOwner()
        && playerId.equals(ownerComponent.getOwnerId());
  }

  public String getOwnerName(
      @Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> catRef) {
    CatOwnerComponent ownerComponent = accessor.getComponent(catRef, componentType);
    return ownerComponent != null ? ownerComponent.getOwnerName() : null;
  }

  public void setOwner(
      @Nonnull CommandBuffer<EntityStore> buffer,
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull UUID ownerId,
      @Nonnull String ownerName) {
    CatOwnerComponent component = new CatOwnerComponent(ownerId, ownerName);
    buffer.putComponent(catRef, componentType, component);
    LOGGER.at(Level.INFO).log("Cat ownership set to %s", ownerName);
  }
}
