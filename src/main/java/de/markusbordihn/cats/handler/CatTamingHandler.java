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

package de.markusbordihn.cats.handler;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class CatTamingHandler {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final Set<String> TAMING_ITEMS =
      Set.of(
          // Cooked/prepared fish food
          "Food_Fish_Raw",
          "Food_Fish_Raw_Uncommon",
          "Food_Fish_Raw_Rare",
          "Food_Fish_Raw_Epic",
          "Food_Fish_Raw_Legendary",
          "Food_Fish_Grilled",

          // Common edible fish that cats would eat
          "Fish_Salmon_Item",
          "Fish_Catfish_Item",
          "Fish_Trout_Rainbow_Item",
          "Fish_Pike_Item",
          "Fish_Bluegill_Item",
          "Fish_Minnow_Item");

  // Cat role IDs to identify cat entities
  private static final Set<String> CAT_ROLE_IDS =
      Set.of("Cat", "Cat_Black", "Cat_Calico", "Cat_GrayTabby", "Cat_OrangeTabby", "Cat_Siamese");

  public static void onPlayerInteract(@Nonnull PlayerInteractEvent event) {
    // Check if player is interacting with an entity
    Entity targetEntity = event.getTargetEntity();
    if (targetEntity == null) {
      return;
    }

    Ref<EntityStore> targetRef = event.getTargetRef();
    if (targetRef == null || !targetRef.isValid()) {
      return;
    }

    Player player = event.getPlayer();

    // Get player info for ownership
    var playerRef = player.getPlayerRef();
    if (playerRef == null) {
      return;
    }

    // Get store from target entity ref
    var store = targetRef.getStore();
    if (store == null) {
      return;
    }

    // Check if entity is an NPC
    NPCEntity npcEntity = store.getComponent(targetRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return;
    }

    LOGGER.at(Level.FINE).log("Cat interaction detected");

    // Check if player is holding a fish
    ItemStack itemInHand = event.getItemInHand();
    if (itemInHand == null || !TAMING_ITEMS.contains(itemInHand.getItemId())) {
      LOGGER.at(Level.FINE).log("No taming item in hand");
      return;
    }

    // Player has fish - check if cat is already tamed
    CatOwnerComponent ownerComponent =
        store.getComponent(targetRef, Main.getInstance().catOwnerComponentType);
    if (ownerComponent != null && ownerComponent.hasOwner()) {
      player.sendMessage(
          Message.raw("This cat already has an owner: " + ownerComponent.getOwnerName())
              .color("#FFFF00"));
      event.setCancelled(true);
      return;
    }

    // Get player UUID from PlayerRef
    var playerUUID = playerRef.getUuid();
    var username = playerRef.getUsername();

    if (playerUUID == null || username == null) {
      LOGGER.at(Level.WARNING).log("Player UUID or username not found for taming");
      return;
    }

    // Tame the cat by setting owner component
    if (ownerComponent == null) {
      ownerComponent = new CatOwnerComponent(playerUUID, username);
    } else {
      ownerComponent.setOwner(playerUUID, username);
    }
    store.putComponent(targetRef, Main.getInstance().catOwnerComponentType, ownerComponent);

    // Set cat state to following
    CatStateComponent stateComponent = new CatStateComponent(CatStateComponent.CatState.FOLLOWING);
    store.putComponent(targetRef, Main.getInstance().catStateComponentType, stateComponent);

    // Change NPC state to Pet with Default substate (following behavior)
    npcEntity.getRole().getStateSupport().setState(targetRef, "Pet", "Default", store);

    // Notify player
    player.sendMessage(
        Message.raw("✓ You tamed the cat with " + itemInHand.getItemId() + "!").color("#00FF00"));
    player.sendMessage(Message.raw("🐱 Purr! Your new companion will follow you").color("#FFAA00"));
    player.sendMessage(
        Message.raw("Right-click or use /cat commands to interact").color("#FFFF00"));

    LOGGER.at(Level.INFO).log("Cat tamed by player " + username);

    // Cancel the default interaction to prevent item consumption by default mechanism
    event.setCancelled(true);
  }
}
