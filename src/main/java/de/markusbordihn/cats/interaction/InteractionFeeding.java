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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.inventory.InventoryHelper;
import java.util.logging.Level;

public class InteractionFeeding {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final float HEALING_AMOUNT_PER_FEEDING = 10.0f;

  public static boolean handle(
      Ref<EntityStore> entityRef,
      Role role,
      Store<EntityStore> store,
      Player player,
      ItemStack heldItem,
      boolean isOwner) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    String interactionType = isOwner ? "FEEDING: By Owner" : "FEEDING: By Stranger";
    InteractionLogger.logInteraction(interactionType, entityRef, role, store, player, itemName);

    role.getStateSupport().setState(entityRef, "Feeding", "Default", store);

    if (isOwner) {
      healCat(entityRef, store, player);
    }

    InventoryHelper.consumeActiveHotbarItem(player, heldItem);

    return false;
  }

  private static void healCat(Ref<EntityStore> entityRef, Store<EntityStore> store, Player player) {
    EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
    if (statMap == null) {
      LOGGER.at(Level.FINE).log("Cannot heal cat: EntityStatMap not found");
      return;
    }

    int healthIndex = DefaultEntityStatTypes.getHealth();
    EntityStatValue healthStat = statMap.get(healthIndex);
    if (healthStat == null) {
      LOGGER.at(Level.FINE).log("Cannot heal cat: health stat not found");
      return;
    }

    float currentHealth = healthStat.get();
    float maxHealth = healthStat.getMax();

    if (currentHealth >= maxHealth) {
      if (player != null) {
        player.sendMessage(
            Message.translation("cats.interactions.feeding.full_health")
                .color(Constants.COLOR_INFO));
      }
      return;
    }

    float newHealth = statMap.addStatValue(healthIndex, HEALING_AMOUNT_PER_FEEDING);
    float healedAmount = newHealth - currentHealth;

    LOGGER.at(Level.FINE).log(
        "Healed cat by %.1f HP (%.1f -> %.1f / %.1f)",
        healedAmount, currentHealth, newHealth, maxHealth);

    if (player != null) {
      player.sendMessage(
          Message.translation("cats.interactions.feeding.healed")
              .param("amount", String.valueOf((int) healedAmount))
              .param("health", String.valueOf((int) newHealth))
              .param("maxHealth", String.valueOf((int) maxHealth))
              .color(Constants.COLOR_SUCCESS));
    }
  }
}
