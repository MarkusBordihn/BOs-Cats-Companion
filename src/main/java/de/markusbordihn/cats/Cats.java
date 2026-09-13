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
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.WorldNPCSpawn;
import de.markusbordihn.cats.commands.CatCommands;
import de.markusbordihn.cats.compat.LuckPermsCompat;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.component.CatFetchTargetComponent;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.component.CatNeedsComponent;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.component.CatTamingProgressComponent;
import de.markusbordihn.cats.component.CatTargetComponent;
import de.markusbordihn.cats.component.CatYarnBallProjectileComponent;
import de.markusbordihn.cats.config.GeneralConfig;
import de.markusbordihn.cats.config.ProtectionConfig;
import de.markusbordihn.cats.config.SpawnConfig;
import de.markusbordihn.cats.handler.DamageSetupHandler;
import de.markusbordihn.cats.handler.NPCSetupHandler;
import de.markusbordihn.cats.interaction.CatYarnBallFetchInteraction;
import de.markusbordihn.cats.interaction.CatYarnBallSpawnInteraction;
import de.markusbordihn.cats.interaction.CatYarnBallThrowInteraction;
import de.markusbordihn.cats.interaction.UseCatCarrierInteraction;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.manager.CatsNamesManager;
import de.markusbordihn.cats.permission.PermissionManager;
import de.markusbordihn.cats.spawn.CatSpawnConfigSystem;
import de.markusbordihn.cats.system.CatStateSyncSystem;
import de.markusbordihn.cats.system.CatStateSystem;
import de.markusbordihn.cats.system.CatYarnBallProjectileFallbackSystem;
import de.markusbordihn.cats.ui.CatBedSpawnPage;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Cats extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static ComponentType<EntityStore, CatTargetComponent> catTargetComponentType;
  private static Cats instance;
  public ComponentType<EntityStore, CatOwnerComponent> catOwnerComponentType;
  public ComponentType<EntityStore, CatStateComponent> catStateComponentType;
  public ComponentType<EntityStore, CatBedTargetComponent> catBedTargetComponentType;
  public ComponentType<EntityStore, CatFetchTargetComponent> catFetchTargetComponentType;
  public ComponentType<EntityStore, CatTamingProgressComponent> catTamingProgressComponentType;
  public ComponentType<EntityStore, CatMoodComponent> catMoodComponentType;
  public ComponentType<EntityStore, CatNeedsComponent> catNeedsComponentType;
  public ComponentType<EntityStore, CatYarnBallProjectileComponent>
      catYarnBallProjectileComponentType;
  public ResourceType<EntityStore, CatsDataResource> catsDataResourceType;
  private CatCommands catCommands;

  public Cats(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static Cats getInstance() {
    return instance;
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
    this.catOwnerComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatOwnerComponent.class, CatOwnerComponent.ID, CatOwnerComponent.CODEC);
    this.catStateComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatStateComponent.class, CatStateComponent.ID, CatStateComponent.CODEC);
    this.catBedTargetComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatBedTargetComponent.class, CatBedTargetComponent.ID, CatBedTargetComponent.CODEC);
    this.catFetchTargetComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatFetchTargetComponent.class,
                CatFetchTargetComponent.ID,
                CatFetchTargetComponent.CODEC);
    this.catTamingProgressComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatTamingProgressComponent.class,
                CatTamingProgressComponent.ID,
                CatTamingProgressComponent.CODEC);
    catTargetComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatTargetComponent.class, CatTargetComponent.ID, CatTargetComponent.CODEC);
    this.catMoodComponentType =
        getEntityStoreRegistry()
            .registerComponent(CatMoodComponent.class, CatMoodComponent.ID, CatMoodComponent.CODEC);
    this.catNeedsComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatNeedsComponent.class, CatNeedsComponent.ID, CatNeedsComponent.CODEC);
    catYarnBallProjectileComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                CatYarnBallProjectileComponent.class,
                CatYarnBallProjectileComponent.ID,
                CatYarnBallProjectileComponent.CODEC);
    this.catsDataResourceType =
        getEntityStoreRegistry()
            .registerResource(CatsDataResource.class, CatsDataResource.ID, CatsDataResource.CODEC);

    LOGGER.at(Level.INFO).log("Registering systems...");
    getEntityStoreRegistry().registerSystem(new CatStateSystem(this.catStateComponentType));
    getEntityStoreRegistry().registerSystem(new CatStateSyncSystem(this.catStateComponentType));
    getEntityStoreRegistry().registerSystem(new CatsManager(this.catStateComponentType));
    getEntityStoreRegistry()
        .registerSystem(
            new CatYarnBallProjectileFallbackSystem(catYarnBallProjectileComponentType));

    LOGGER.at(Level.INFO).log("Registering cat damage filter system...");
    DamageSetupHandler damageSetupHandler = new DamageSetupHandler(getEntityStoreRegistry());
    if (!damageSetupHandler.tryRegister()) {
      LOGGER.at(Level.INFO).log(
          "DamageModule not ready yet, deferring damage filter registration...");
      getEventRegistry().registerGlobal(PluginSetupEvent.class, damageSetupHandler::onPluginSetup);
    }

    this.getCodecRegistry(Interaction.CODEC)
        .register(
            UseCatCarrierInteraction.ID,
            UseCatCarrierInteraction.class,
            UseCatCarrierInteraction.CODEC);

    this.getCodecRegistry(Interaction.CODEC)
        .register(
            CatYarnBallFetchInteraction.ID,
            CatYarnBallFetchInteraction.class,
            CatYarnBallFetchInteraction.CODEC);

    this.getCodecRegistry(Interaction.CODEC)
        .register(
            CatYarnBallSpawnInteraction.ID,
            CatYarnBallSpawnInteraction.class,
            CatYarnBallSpawnInteraction.CODEC);

    this.getCodecRegistry(Interaction.CODEC)
        .register(
            CatYarnBallThrowInteraction.ID,
            CatYarnBallThrowInteraction.class,
            CatYarnBallThrowInteraction.CODEC);

    LOGGER.at(Level.INFO).log("Initializing configuration...");
    GeneralConfig.initialize();
    SpawnConfig.initialize();
    ProtectionConfig.initialize();
    CatsNamesManager.initialize();

    NPCSetupHandler npcSetupHandler = new NPCSetupHandler();
    if (NPCPlugin.get() instanceof NPCPlugin npcPlugin) {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin ...");
      npcSetupHandler.onNpcPluginReady(npcPlugin);
    } else {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin setup listener...");
      getEventRegistry().registerGlobal(PluginSetupEvent.class, npcSetupHandler::onPluginSetup);
    }

    LOGGER.at(Level.INFO).log("Registering commands...");
    this.catCommands = new CatCommands();
    this.getCommandRegistry().registerCommand(this.catCommands);

    LOGGER.at(Level.INFO).log("Registering spawn config system...");
    getEventRegistry()
        .register(
            LoadedAssetsEvent.class,
            WorldNPCSpawn.class,
            CatSpawnConfigSystem::onWorldNPCSpawnsLoaded);
    OpenCustomUIInteraction.registerSimple(
        this, CatBedSpawnPage.class, "CatBedSpawn", CatBedSpawnPage::new);
  }

  @Override
  protected void start() {
    super.start();
    LOGGER.at(Level.INFO).log("Starting Cats Plugin...");

    if (this.catCommands != null) {
      PermissionManager.initializeDefaultPermissions(this.catCommands.buildPlayerPermissionNodes());
    }

    Universe.get().getUniverseReady().thenRun(LuckPermsCompat::detect);
  }

  @Override
  protected void shutdown() {
    super.shutdown();
    LOGGER.at(Level.INFO).log("Shutting down Cats Plugin...");
  }
}
