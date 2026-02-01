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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatState;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class CatStateSyncSystem extends RefSystem<EntityStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, CatStateComponent> componentType;

  public CatStateSyncSystem(ComponentType<EntityStore, CatStateComponent> componentType) {
    this.componentType = componentType;
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return componentType;
  }

  @Override
  public void onEntityAdded(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull AddReason addReason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    CatStateComponent stateComponent = store.getComponent(entityRef, componentType);
    if (stateComponent == null) {
      return;
    }

    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return;
    }

    try {
      var stateSupport = npcEntity.getRole().getStateSupport();
      CatState state = stateComponent.getState();
      switch (state) {
        case SITTING:
          stateSupport.setState(entityRef, "Pet", "Sitting", store);
          LOGGER.at(Level.FINE).log("Synced cat to SITTING substate");
          break;
        case SLEEPING:
          stateSupport.setState(entityRef, "Pet", "Sleeping", store);
          LOGGER.at(Level.FINE).log("Synced cat to SLEEPING substate");
          break;
        case PLAYING:
          stateSupport.setState(entityRef, "Pet", "Playing", store);
          LOGGER.at(Level.FINE).log("Synced cat to PLAYING substate");
          break;
        case SEARCHING:
          stateSupport.setState(entityRef, "Pet", "Searching", store);
          LOGGER.at(Level.FINE).log("Synced cat to SEARCHING substate");
          break;
        case WAITING:
          stateSupport.setState(entityRef, "Pet", "Waiting", store);
          LOGGER.at(Level.FINE).log("Synced cat to WAITING substate");
          break;
        case WANDERING:
          stateSupport.setState(entityRef, "Pet", "Wandering", store);
          LOGGER.at(Level.FINE).log("Synced cat to WANDERING substate");
          break;
        case FOLLOWING:
          stateSupport.setState(entityRef, "Pet", "Default", store);
          LOGGER.at(Level.FINE).log("Synced cat to FOLLOWING (Default) substate");
          break;
        case ATTACKING:
          stateSupport.setState(entityRef, "Pet", "Attacking", store);
          LOGGER.at(Level.FINE).log("Synced cat to ATTACKING substate");
          break;
        default:
          LOGGER.at(Level.WARNING).log("Unknown cat state: " + state);
          break;
      }
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).log("Failed to sync cat state: " + e.getMessage());
    }
  }

  @Override
  public void onEntityRemove(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull RemoveReason removeReason,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    // No cleanup needed when entity is removed
  }
}
