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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.permissions.PermissionHolder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.inventory.InventoryHelper;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.manager.CatsNamesManager;
import de.markusbordihn.cats.permission.PermissionManager;
import java.util.UUID;
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

    var playerEntityRef = playerRef.getReference();
    if (playerEntityRef == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - player entity ref is null");
      return;
    }

    var playerUUID = playerRef.getUuid();
    var username = playerRef.getUsername();
    if (playerUUID == null || username == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - UUID or username not found");
      return;
    }

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - CatsManager is null");
      player.sendMessage(
          Message.translation("cats.interactions.taming.error_system")
              .color(Constants.COLOR_ERROR));
      return;
    }

    int currentCatCount = catsManager.getCatCountByOwner(playerUUID, store);
    int catLimit = getCatLimit(player);
    if (catLimit >= 0 && currentCatCount >= catLimit) {
      player.sendMessage(
          Message.translation("cats.interactions.taming.limit_reached")
              .param("current", String.valueOf(currentCatCount))
              .param("limit", String.valueOf(catLimit))
              .color(Constants.COLOR_ERROR));
      return;
    }

    UUIDComponent catUuidComponent =
        store.getComponent(entityRef, UUIDComponent.getComponentType());
    UUID catUuid = catUuidComponent != null ? catUuidComponent.getUuid() : null;
    if (catUuid == null) {
      LOGGER.at(Level.WARNING).log("Cannot tame cat - cat UUID not found");
      player.sendMessage(
          Message.translation("cats.interactions.taming.error_no_uuid")
              .color(Constants.COLOR_ERROR));
      return;
    }

    String catName = CatsNamesManager.getRandomName();

    // Use centralized ownership assignment
    catsManager.assignOwner(entityRef, playerUUID, username, catName, "Taming", store);

    player.sendMessage(
        Message.translation("cats.interactions.taming.success")
            .param("item", itemName)
            .param("catName", catName)
            .color(Constants.COLOR_SUCCESS));
    player.sendMessage(
        Message.translation("cats.interactions.taming.companion").color(Constants.COLOR_WARNING));
    player.sendMessage(
        Message.translation("cats.interactions.taming.help")
            .param("catName", catName)
            .color(Constants.COLOR_INFO));

    if (catLimit >= 0) {
      player.sendMessage(
          Message.translation("cats.interactions.taming.count")
              .param("current", String.valueOf(currentCatCount + 1))
              .param("limit", String.valueOf(catLimit))
              .color(Constants.COLOR_GRAY));
    }

    InventoryHelper.consumeActiveHotbarItem(player, heldItem);

    LOGGER.at(Level.INFO).log(
        "Cat successfully tamed by player %s with item %s (cats: %d/%d)",
        username, itemName, currentCatCount + 1, catLimit);
  }

  private static int getCatLimit(Player player) {
    if (!(player instanceof PermissionHolder permissionHolder)) {
      return Constants.DEFAULT_CAT_LIMIT;
    }
    return PermissionManager.getCatLimit(permissionHolder);
  }
}
