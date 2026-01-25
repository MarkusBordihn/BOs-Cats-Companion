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

package de.markusbordihn.cats.interaction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import java.util.logging.Level;

public class InteractionLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static void logInteraction(
      String interactionType,
      Ref<EntityStore> entityRef,
      Role role,
      Store<EntityStore> store,
      Player player,
      String itemName) {
    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());
    CatStateComponent stateComponent =
        store.getComponent(entityRef, CatStateComponent.getComponentType());

    LOGGER.at(Level.FINE).log("=== CAT INTERACTION: %s ===", interactionType);
    LOGGER.at(Level.FINE).log("Entity: %s", entityRef);
    LOGGER.at(Level.FINE).log("Role: %s", role.getRoleName());

    if (player != null) {
      LOGGER.at(Level.FINE).log(
          "Player: %s (UUID: %s)", player.getPlayerRef(), player.getPlayerRef().getUuid());
    } else {
      LOGGER.at(Level.FINE).log("Player: NULL");
    }

    LOGGER.at(Level.FINE).log("Item used: %s", itemName != null ? itemName : "EMPTY HAND");

    if (ownerComponent != null && ownerComponent.hasOwner()) {
      LOGGER.at(Level.FINE).log("Owner: %s", ownerComponent.getOwnerName());
      LOGGER.at(Level.FINE).log("Cat Name: %s", ownerComponent.getCatName());
    } else {
      LOGGER.at(Level.FINE).log("Owner: WILD CAT");
    }

    if (stateComponent != null) {
      LOGGER.at(Level.FINE).log("State: %s", stateComponent.getState());
    }
  }
}
