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
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.storage.AlarmStore;
import com.hypixel.hytale.server.npc.util.Alarm;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.HappinessSource;
import de.markusbordihn.cats.inventory.InventoryHelper;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.player.PlayerFeedback;
import java.time.Duration;
import java.util.logging.Level;

public class InteractionFeeding {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final float HEALING_AMOUNT_PER_FEEDING = 10.0f;
  private static final float TREAT_HEALING_AMOUNT = 5.0f;
  private static final String TREAT_COOLDOWN_ALARM = "TreatCooldown";
  private static final Duration TREAT_COOLDOWN_DURATION = Duration.ofMinutes(2);
  private static final Duration TREAT_COOLDOWN_FULL_HEALTH_DURATION = Duration.ofMinutes(5);

  public static boolean handle(
      Ref<EntityStore> entityRef,
      Store<EntityStore> store,
      Player player,
      ItemStack heldItem,
      boolean isOwner) {
    String itemName = heldItem != null ? heldItem.getItemId() : null;
    boolean isTreat = Constants.CAT_TREATS_ITEM.equals(itemName);

    AlarmStore alarmStore = null;
    WorldTimeResource worldTime = null;
    if (isTreat) {
      alarmStore = AlarmStore.get(entityRef, store);
      worldTime =
          alarmStore != null ? store.getResource(WorldTimeResource.getResourceType()) : null;
    }

    if (alarmStore != null) {
      Alarm treatAlarm = alarmStore.get(TREAT_COOLDOWN_ALARM);
      if (treatAlarm.isSet() && !treatAlarm.hasPassed(worldTime.getGameTime())) {
        if (player != null) {
          PlayerFeedback.sendMessage(
              store,
              player,
              Message.translation("cats.interactions.feeding.treat.cooldown")
                  .color(Constants.COLOR_INFO));
        }
        return true;
      }
    }

    StateSupport.get(entityRef, store).setState(entityRef, "Feeding", "Default", store);

    if (isOwner) {
      float healAmount = isTreat ? TREAT_HEALING_AMOUNT : HEALING_AMOUNT_PER_FEEDING;
      boolean wasFullHealth = isTreat && isAtFullHealth(entityRef, store);
      healCat(entityRef, store, player, healAmount, isTreat);

      if (alarmStore != null) {
        Duration cooldown =
            wasFullHealth ? TREAT_COOLDOWN_FULL_HEALTH_DURATION : TREAT_COOLDOWN_DURATION;
        Alarm treatAlarm = alarmStore.get(TREAT_COOLDOWN_ALARM);
        treatAlarm.set(entityRef, worldTime.getGameTime().plus(cooldown), store);
      }

      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager != null) {
        catsManager.boostHappiness(
            entityRef, isTreat ? HappinessSource.TREAT : HappinessSource.FEEDING, store);
        catsManager.satisfyNeed(
            entityRef, CatNeedType.SOCIAL, 8f, store);
      }
    }

    InventoryHelper.consumeActiveHotbarItem(player, heldItem);

    return false;
  }

  private static boolean isAtFullHealth(Ref<EntityStore> entityRef, Store<EntityStore> store) {
    EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
    if (statMap == null) {
      return false;
    }

    EntityStatValue healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
    return healthStat != null && healthStat.get() >= healthStat.getMax();
  }

  private static void healCat(
      Ref<EntityStore> entityRef,
      Store<EntityStore> store,
      Player player,
      float healAmount,
      boolean isTreat) {
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
        String msgKey =
            isTreat
                ? "cats.interactions.feeding.treat.full_health"
                : "cats.interactions.feeding.full_health";
        PlayerFeedback.sendMessage(
            store, player, Message.translation(msgKey).color(Constants.COLOR_INFO));
      }
      return;
    }

    float newHealth = statMap.addStatValue(healthIndex, healAmount);
    float healedAmount = newHealth - currentHealth;

    LOGGER.at(Level.FINE).log(
        "Healed cat by %.1f HP (%.1f -> %.1f / %.1f)",
        healedAmount, currentHealth, newHealth, maxHealth);

    if (player != null) {
      String msgKey =
          isTreat ? "cats.interactions.feeding.treat.healed" : "cats.interactions.feeding.healed";
      PlayerFeedback.sendMessage(
          store,
          player,
          Message.translation(msgKey)
              .param("amount", String.valueOf((int) healedAmount))
              .param("health", String.valueOf((int) newHealth))
              .param("maxHealth", String.valueOf((int) maxHealth))
              .color(Constants.COLOR_SUCCESS));
    }
  }
}
