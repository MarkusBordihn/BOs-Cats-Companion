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

package de.markusbordihn.cats.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nullable;

public class InventoryHelper {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static void consumeActiveHotbarItem(Player player, ItemStack heldItem) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Cannot consume item: player is null");
      return;
    }

    InventoryComponent.Hotbar hotbar = getHotbar(player);
    if (hotbar == null) {
      LOGGER.at(Level.WARNING).log("Cannot consume item: player hotbar is unavailable");
      return;
    }

    short activeSlot = hotbar.getActiveSlot();
    hotbar.getInventory().removeItemStackFromSlot(activeSlot, 1);
    LOGGER.at(Level.FINE).log(
        "Consumed 1x %s from player inventory at slot %d", itemName, activeSlot);
  }

  public static boolean giveItem(Player player, String itemId) {
    if (player == null || itemId == null || itemId.isEmpty()) {
      return false;
    }

    InventoryComponent.Hotbar hotbar = getHotbar(player);
    if (hotbar == null) {
      LOGGER.at(Level.WARNING).log("Cannot give item: player hotbar is unavailable");
      return false;
    }

    ItemStack giftStack = new ItemStack(itemId);
    if (!giftStack.isValid()) {
      LOGGER.at(Level.WARNING).log("Cannot give item: invalid item ID %s", itemId);
      return false;
    }

    hotbar.getInventory().addItemStack(giftStack);
    LOGGER.at(Level.FINE).log("Gave 1x %s to player", itemId);
    return true;
  }

  @Nullable
  private static InventoryComponent.Hotbar getHotbar(Player player) {
    Ref<EntityStore> playerEntityRef = player.getReference();
    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return null;
    }

    return playerEntityRef
        .getStore()
        .getComponent(playerEntityRef, InventoryComponent.Hotbar.getComponentType());
  }
}
