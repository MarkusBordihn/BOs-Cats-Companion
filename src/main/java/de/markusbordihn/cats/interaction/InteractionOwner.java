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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.Alarm;
import java.time.Duration;

public class InteractionOwner {

  private static final String PET_COOLDOWN_ALARM = "PetCooldown";
  private static final Duration PET_COOLDOWN_DURATION = Duration.ofMinutes(5);

  public static boolean handle(
      Ref<EntityStore> entityRef, Role role, Store<EntityStore> store, Player player) {

    // Get the NPC entity to access alarm store
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null) {
      return false;
    }

    // Get or create the petting cooldown alarm
    Alarm petAlarm = npcEntity.getAlarmStore().get(npcEntity, PET_COOLDOWN_ALARM);

    // Get current world time for alarm check
    WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());

    // Check if petting cooldown is active (alarm is set and has not passed)
    if (petAlarm.isSet() && !petAlarm.hasPassed(worldTimeResource.getGameTime())) {
      // Send message to player that cat doesn't want petting right now
      player.sendMessage(
          Message.translation("cats.interactions.owner.petting.cooldown").color("#AAAAAA"));
      return true; // Interaction handled, prevent further processing
    }

    InteractionLogger.logInteraction(
        "OWNER: Petting Interaction", entityRef, role, store, player, null);

    // Trigger Petting animation state (purring, happy animation)
    role.getStateSupport().setState(entityRef, "Petting", "Default", store);

    // Set petting cooldown alarm (current time + cooldown duration)
    petAlarm.set(entityRef, worldTimeResource.getGameTime().plus(PET_COOLDOWN_DURATION), store);

    return false;
  }
}
