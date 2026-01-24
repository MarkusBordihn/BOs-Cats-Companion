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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import java.util.logging.Level;

public class InteractionTaming {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static boolean handle(
      Ref<EntityStore> entityRef,
      Role role,
      Store<EntityStore> store,
      Player player,
      ItemStack heldItem) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    InteractionLogger.logInteraction(
        "WILD CAT: Taming Attempt", entityRef, role, store, player, itemName);

    handleSuccessfulTaming(entityRef, role, store, player, heldItem);
    return false;
  }

  private static void handleSuccessfulTaming(
      Ref<EntityStore> entityRef,
      Role role,
      Store<EntityStore> store,
      Player player,
      ItemStack heldItem) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - player is null");
      return;
    }

    var playerRef = player.getPlayerRef();
    if (playerRef == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - player ref is null");
      return;
    }

    var playerUUID = playerRef.getUuid();
    var username = playerRef.getUsername();
    if (playerUUID == null || username == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - UUID or username not found");
      return;
    }

    CatOwnerComponent ownerComponent = new CatOwnerComponent(playerUUID, username);
    store.putComponent(entityRef, Main.getInstance().catOwnerComponentType, ownerComponent);

    CatStateComponent stateComponent = new CatStateComponent(CatStateComponent.CatState.FOLLOWING);
    store.putComponent(entityRef, Main.getInstance().catStateComponentType, stateComponent);

    // Trigger Taming animation state (auto-transitions to Pet state after 3 seconds)
    role.getStateSupport().setState(entityRef, "Taming", "Default", store);

    // Notify player
    player.sendMessage(
        Message.translation("cats.interactions.taming.success")
            .param("item", itemName)
            .color("#00FF00"));
    player.sendMessage(Message.translation("cats.interactions.taming.companion").color("#FFAA00"));
    player.sendMessage(Message.translation("cats.interactions.taming.help").color("#FFFF00"));

    // Consume item from inventory
    consumeItemFromInventory(player, heldItem);

    LOGGER.at(Level.INFO).log(
        "Cat successfully tamed by player %s with item %s", username, itemName);
  }

  /** Consume one item from player's active hotbar slot */
  private static void consumeItemFromInventory(Player player, ItemStack heldItem) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    if (player == null) {
      return;
    }

    Inventory inventory = player.getInventory();
    if (inventory == null) {
      LOGGER.at(Level.WARNING).log("Cannot consume item - player inventory is null");
      return;
    }

    // TODO: Implement item consumption
    // inventory.consumeActiveHotbarItem(1);
    LOGGER.at(Level.FINE).log("Item consumption not yet implemented: %s", itemName);
  }
}
