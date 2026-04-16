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

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import de.markusbordihn.cats.actions.BuilderActionCatFetchYarnBall;
import de.markusbordihn.cats.actions.BuilderActionCatGoToBed;
import de.markusbordihn.cats.actions.BuilderActionCatIdleChoice;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionBase;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionOwner;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionStranger;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionWild;
import de.markusbordihn.cats.actions.BuilderActionCatMoodParticles;
import de.markusbordihn.cats.actions.BuilderActionCatSetSleepingState;
import de.markusbordihn.cats.actions.BuilderActionCatTeleportToBed;
import de.markusbordihn.cats.sensors.BuilderSensorCatPersonalityWeight;
import de.markusbordihn.cats.sensors.BuilderSensorIsCatTamed;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingCarrier;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingEmptyHand;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingFood;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingYarnBall;
import de.markusbordihn.cats.sensors.BuilderSensorIsOwner;
import de.markusbordihn.cats.sensors.BuilderSensorOwnerPlayer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NPCSetupHandler {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @SuppressWarnings("unchecked")
  private static final Class<? extends BuilderActionCatInteractionBase>[] CAT_INTERACTION_BUILDERS =
      new Class[] {
        BuilderActionCatInteractionWild.class,
        BuilderActionCatInteractionOwner.class,
        BuilderActionCatInteractionStranger.class
      };

  private static final String[] SENSOR_IDS = {
    BuilderSensorIsCatTamed.SENSOR_ID,
    BuilderSensorIsOwner.SENSOR_ID,
    BuilderSensorIsHoldingCarrier.SENSOR_ID,
    BuilderSensorIsHoldingFood.SENSOR_ID,
    BuilderSensorIsHoldingEmptyHand.SENSOR_ID,
    BuilderSensorIsHoldingYarnBall.SENSOR_ID,
    BuilderSensorCatPersonalityWeight.SENSOR_ID,
    BuilderSensorOwnerPlayer.SENSOR_ID,
  };

  @SuppressWarnings("rawtypes")
  private static final Supplier[] SENSOR_SUPPLIERS = {
    BuilderSensorIsCatTamed::new,
    BuilderSensorIsOwner::new,
    BuilderSensorIsHoldingCarrier::new,
    BuilderSensorIsHoldingFood::new,
    BuilderSensorIsHoldingEmptyHand::new,
    BuilderSensorIsHoldingYarnBall::new,
    BuilderSensorCatPersonalityWeight::new,
    BuilderSensorOwnerPlayer::new,
  };

  private boolean actionsRegistered = false;
  private boolean sensorsRegistered = false;

  public void onPluginSetup(PluginSetupEvent event) {
    if (event.getPlugin() instanceof NPCPlugin npcPlugin) {
      onNpcPluginReady(npcPlugin);
    }
  }

  public void onNpcPluginReady(NPCPlugin npcPlugin) {
    registerCatInteractionActions(npcPlugin);
    registerCatSensors(npcPlugin);
  }

  private void registerCatInteractionActions(NPCPlugin npcPlugin) {
    if (actionsRegistered) {
      LOGGER.at(Level.INFO).log("Custom actions already registered - skipping");
      return;
    }

    LOGGER.at(Level.INFO).log("NPC Plugin setup detected - registering custom actions");

    BuilderFactory<Action> actionFactory = npcPlugin.getBuilderManager().getFactory(Action.class);

    int registeredCount = 0;
    for (Class<? extends BuilderActionCatInteractionBase> builderClass : CAT_INTERACTION_BUILDERS) {
      try {
        BuilderActionCatInteractionBase builder =
            builderClass.getDeclaredConstructor().newInstance();
        String builderId = builder.getBuilderId();
        actionFactory.add(
            builderId,
            () -> {
              try {
                return builderClass.getDeclaredConstructor().newInstance();
              } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to instantiate action builder: " + builderClass.getSimpleName(), e);
              }
            });
        registeredCount++;
        LOGGER.at(Level.INFO).log("Registered action: %s", builderId);
      } catch (Exception e) {
        LOGGER.at(Level.SEVERE).log(
            "Failed to register action builder: %s", builderClass.getSimpleName(), e);
      }
    }

    LOGGER.at(Level.INFO).log("Registered %d cat interaction actions", registeredCount);

    try {
      actionFactory.add(
          BuilderActionCatSetSleepingState.BUILDER_ID, BuilderActionCatSetSleepingState::new);
      LOGGER.at(Level.INFO).log(
          "Registered action: %s", BuilderActionCatSetSleepingState.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatSetSleepingState.BUILDER_ID, e);
    }

    try {
      actionFactory.add(
          BuilderActionCatTeleportToBed.BUILDER_ID, BuilderActionCatTeleportToBed::new);
      LOGGER.at(Level.INFO).log("Registered action: %s", BuilderActionCatTeleportToBed.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatTeleportToBed.BUILDER_ID, e);
    }

    try {
      actionFactory.add(BuilderActionCatGoToBed.BUILDER_ID, BuilderActionCatGoToBed::new);
      LOGGER.at(Level.INFO).log("Registered action: %s", BuilderActionCatGoToBed.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatGoToBed.BUILDER_ID, e);
    }

    try {
      actionFactory.add(
          BuilderActionCatMoodParticles.BUILDER_ID, BuilderActionCatMoodParticles::new);
      LOGGER.at(Level.INFO).log("Registered action: %s", BuilderActionCatMoodParticles.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatMoodParticles.BUILDER_ID, e);
    }

    try {
      actionFactory.add(
          BuilderActionCatFetchYarnBall.BUILDER_ID, BuilderActionCatFetchYarnBall::new);
      LOGGER.at(Level.INFO).log("Registered action: %s", BuilderActionCatFetchYarnBall.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatFetchYarnBall.BUILDER_ID, e);
    }

    try {
      actionFactory.add(BuilderActionCatIdleChoice.BUILDER_ID, BuilderActionCatIdleChoice::new);
      LOGGER.at(Level.INFO).log("Registered action: %s", BuilderActionCatIdleChoice.BUILDER_ID);
    } catch (Exception e) {
      LOGGER.at(Level.SEVERE).log(
          "Failed to register action: %s", BuilderActionCatIdleChoice.BUILDER_ID, e);
    }

    actionsRegistered = true;
  }

  @SuppressWarnings("unchecked")
  private void registerCatSensors(NPCPlugin npcPlugin) {
    if (sensorsRegistered) {
      LOGGER.at(Level.INFO).log("Custom sensors already registered - skipping");
      return;
    }

    LOGGER.at(Level.INFO).log("Registering custom cat sensors...");

    BuilderFactory<Sensor> sensorFactory = npcPlugin.getBuilderManager().getFactory(Sensor.class);

    int registeredCount = 0;
    for (int i = 0; i < SENSOR_IDS.length; i++) {
      String sensorId = SENSOR_IDS[i];
      try {
        sensorFactory.add(sensorId, SENSOR_SUPPLIERS[i]);
        registeredCount++;
        LOGGER.at(Level.INFO).log("Registered sensor: %s", sensorId);
      } catch (Exception e) {
        LOGGER.at(Level.SEVERE).log("Failed to register sensor: %s", sensorId, e);
      }
    }

    sensorsRegistered = true;
    LOGGER.at(Level.INFO).log("Registered %d cat sensors", registeredCount);
  }
}
