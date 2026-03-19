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

package de.markusbordihn.cats;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.spawning.assets.spawns.config.WorldNPCSpawn;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionBase;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionOwner;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionStranger;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionWild;
import de.markusbordihn.cats.actions.BuilderActionCatSetSleepingState;
import de.markusbordihn.cats.actions.BuilderActionCatTeleportToBed;
import de.markusbordihn.cats.commands.CatCommands;
import de.markusbordihn.cats.compat.LuckPermsCompat;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.component.CatTamingProgressComponent;
import de.markusbordihn.cats.component.CatTargetComponent;
import de.markusbordihn.cats.config.GeneralConfig;
import de.markusbordihn.cats.config.ProtectionConfig;
import de.markusbordihn.cats.config.SpawnConfig;
import de.markusbordihn.cats.damage.CatDamageFilterSystem;
import de.markusbordihn.cats.interaction.CatCarrierInteraction;
import de.markusbordihn.cats.interaction.UseCatCarrierInteraction;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.manager.CatsNamesManager;
import de.markusbordihn.cats.permission.PermissionManager;
import de.markusbordihn.cats.sensors.BuilderSensorIsCatTamed;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingCarrier;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingEmptyHand;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingFood;
import de.markusbordihn.cats.sensors.BuilderSensorIsHoldingYarnBall;
import de.markusbordihn.cats.sensors.BuilderSensorIsOwner;
import de.markusbordihn.cats.spawn.CatSpawnConfigSystem;
import de.markusbordihn.cats.system.CatStateSyncSystem;
import de.markusbordihn.cats.system.CatStateSystem;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.function.Supplier;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Cats extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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
  };

  @SuppressWarnings("rawtypes")
  private static final Supplier[] SENSOR_SUPPLIERS = {
    BuilderSensorIsCatTamed::new,
    BuilderSensorIsOwner::new,
    BuilderSensorIsHoldingCarrier::new,
    BuilderSensorIsHoldingFood::new,
    BuilderSensorIsHoldingEmptyHand::new,
    BuilderSensorIsHoldingYarnBall::new,
  };

  public static ComponentType<EntityStore, CatTargetComponent> catTargetComponentType;
  private static Cats instance;
  public ComponentType<EntityStore, CatOwnerComponent> catOwnerComponentType;
  public ComponentType<EntityStore, CatStateComponent> catStateComponentType;
  public ComponentType<EntityStore, CatBedTargetComponent> catBedTargetComponentType;
  public ComponentType<EntityStore, CatTamingProgressComponent> catTamingProgressComponentType;
  public ComponentType<EntityStore, CatMoodComponent> catMoodComponentType;
  public ResourceType<EntityStore, CatsDataResource> catsDataResourceType;
  private boolean actionsRegistered = false;
  private boolean sensorsRegistered = false;
  private boolean damageFilterRegistered = false;
  private CatCommands catCommands;

  public Cats(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static Cats getInstance() {
    return instance;
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

  @Override
  protected void setup() {
    super.setup();
    LOGGER.at(Level.INFO).log(
        "Setting up %s (v%s by %s): %s",
        getManifest().getName(),
        getManifest().getVersion(),
        getManifest().getAuthors(),
        getManifest().getDescription());

    LOGGER.at(Level.INFO).log("Registering components and resources...");
    catOwnerComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatOwnerComponent.class, CatOwnerComponent.ID, CatOwnerComponent.CODEC);
    catStateComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatStateComponent.class, CatStateComponent.ID, CatStateComponent.CODEC);
    catBedTargetComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatBedTargetComponent.class, CatBedTargetComponent.ID, CatBedTargetComponent.CODEC);
    catTamingProgressComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatTamingProgressComponent.class,
                CatTamingProgressComponent.ID,
                CatTamingProgressComponent.CODEC);
    catTargetComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatTargetComponent.class, CatTargetComponent.ID, CatTargetComponent.CODEC);
    catMoodComponentType =
        getEntityStoreRegistry()
            .registerComponent(CatMoodComponent.class, CatMoodComponent.ID, CatMoodComponent.CODEC);
    catsDataResourceType =
        getEntityStoreRegistry()
            .registerResource(CatsDataResource.class, CatsDataResource.ID, CatsDataResource.CODEC);

    LOGGER.at(Level.INFO).log("Registering systems...");
    getEntityStoreRegistry().registerSystem(new CatStateSystem(catStateComponentType));
    getEntityStoreRegistry().registerSystem(new CatStateSyncSystem(catStateComponentType));
    getEntityStoreRegistry().registerSystem(new CatsManager(catStateComponentType));

    LOGGER.at(Level.INFO).log("Registering cat damage filter system...");
    DamageModule damageModule = DamageModule.get();
    if (damageModule != null && damageModule.getFilterDamageGroup() != null) {
      getEntityStoreRegistry().registerSystem(new CatDamageFilterSystem());
      damageFilterRegistered = true;
    } else if (getEventRegistry() != null) {
      LOGGER.at(Level.INFO).log(
          "DamageModule not ready yet, deferring damage filter registration...");
      getEventRegistry()
          .registerGlobal(
              PluginSetupEvent.class,
              event -> {
                if (!damageFilterRegistered && event.getPlugin() instanceof DamageModule) {
                  getEntityStoreRegistry().registerSystem(new CatDamageFilterSystem());
                  damageFilterRegistered = true;
                  LOGGER.at(Level.INFO).log("Cat damage filter system registered (deferred)");
                }
              });
    } else {
      LOGGER.at(Level.WARNING).log(
          "Cannot register damage filter system: event registry not available");
    }

    this.getCodecRegistry(Interaction.CODEC)
        .register("UseCatCarrier", UseCatCarrierInteraction.class, UseCatCarrierInteraction.CODEC);

    LOGGER.at(Level.INFO).log("Initializing configuration...");
    GeneralConfig.initialize();
    SpawnConfig.initialize();
    ProtectionConfig.initialize();
    CatsNamesManager.initialize();

    if (NPCPlugin.get() instanceof NPCPlugin npcPlugin) {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin ...");
      registerCatInteractionActions(npcPlugin);
      registerCatSensors(npcPlugin);
    } else if (getEventRegistry() != null) {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin setup listener...");
      getEventRegistry()
          .registerGlobal(
              PluginSetupEvent.class,
              event -> {
                if (event.getPlugin() instanceof NPCPlugin npcPlugin) {
                  registerCatInteractionActions(npcPlugin);
                  registerCatSensors(npcPlugin);
                }
              });
    } else {
      LOGGER.at(Level.SEVERE).log(
          "Event registry is not available, cannot register NPC Plugin setup listener");
    }

    LOGGER.at(Level.INFO).log("Registering commands...");
    catCommands = new CatCommands();
    this.getCommandRegistry().registerCommand(catCommands);

    LOGGER.at(Level.INFO).log("Registering spawn config system...");
    if (getEventRegistry() != null) {
      getEventRegistry()
          .register(
              LoadedAssetsEvent.class,
              WorldNPCSpawn.class,
              CatSpawnConfigSystem::onWorldNPCSpawnsLoaded);
    } else {
      LOGGER.at(Level.WARNING).log(
          "Event registry not available, spawn config modifications will not be applied");
    }

    if (getEventRegistry() != null) {
      getEventRegistry()
          .registerGlobal(PlayerInteractEvent.class, CatCarrierInteraction::onPlayerInteract);
    }
  }

  @Override
  protected void start() {
    super.start();
    LOGGER.at(Level.INFO).log("Starting Cats Plugin...");

    if (catCommands != null) {
      PermissionManager.initializeDefaultPermissions(catCommands.buildPlayerPermissionNodes());
    }

    // Detect LuckPerms after Universe is ready
    Universe.get().getUniverseReady().thenRun(LuckPermsCompat::detect);
  }

  @Override
  protected void shutdown() {
    super.shutdown();
    LOGGER.at(Level.INFO).log("Shutting down Cats Plugin...");
  }
}
