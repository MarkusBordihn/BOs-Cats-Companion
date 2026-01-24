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

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.instructions.Action;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionBase;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionOwner;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionStranger;
import de.markusbordihn.cats.actions.BuilderActionCatInteractionWild;
import de.markusbordihn.cats.commands.CatCommand;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.system.CatOwnershipSystem;
import de.markusbordihn.cats.system.CatStateSyncSystem;
import de.markusbordihn.cats.system.CatStateSystem;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Main extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final Class<? extends BuilderActionCatInteractionBase>[] CAT_INTERACTION_BUILDERS =
      new Class[] {
        BuilderActionCatInteractionWild.class,
        BuilderActionCatInteractionOwner.class,
        BuilderActionCatInteractionStranger.class
      };

  private static Main instance;
  public ComponentType<EntityStore, CatOwnerComponent> catOwnerComponentType;
  public ComponentType<EntityStore, CatStateComponent> catStateComponentType;
  private boolean actionsRegistered = false;

  public Main(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static Main getInstance() {
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
    actionsRegistered = true;
  }

  @Override
  protected void setup() {
    super.setup();
    LOGGER.at(Level.INFO).log("Setting up Cats Plugin...");
    LOGGER.at(Level.INFO).log("Plugin: %s", getManifest().getName());
    LOGGER.at(Level.INFO).log("Version: %s", getManifest().getVersion());
    LOGGER.at(Level.INFO).log("Author: %s", getManifest().getAuthors());
    LOGGER.at(Level.INFO).log("Description: %s", getManifest().getDescription());

    // Register components
    LOGGER.at(Level.INFO).log("Registering cat components...");
    catOwnerComponentType =
        getEntityStoreRegistry().registerComponent(CatOwnerComponent.class, CatOwnerComponent::new);
    catStateComponentType =
        getEntityStoreRegistry().registerComponent(CatStateComponent.class, CatStateComponent::new);

    // Register systems
    LOGGER.at(Level.INFO).log("Registering cat systems...");
    getEntityStoreRegistry().registerSystem(new CatOwnershipSystem(catOwnerComponentType));
    getEntityStoreRegistry().registerSystem(new CatStateSystem(catStateComponentType));
    getEntityStoreRegistry().registerSystem(new CatStateSyncSystem(catStateComponentType));

    // Try to register custom actions early (for client worlds)
    NPCPlugin npcPlugin = NPCPlugin.get();
    if (npcPlugin != null) {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin ...");
      registerCatInteractionActions(npcPlugin);
    } else {
      LOGGER.at(Level.INFO).log("Registering NPC Plugin setup listener...");
      getEventRegistry()
          .registerGlobal(
              PluginSetupEvent.class,
              event -> {
                if (event.getPlugin() instanceof NPCPlugin) {
                  registerCatInteractionActions((NPCPlugin) event.getPlugin());
                }
              });
    }

    // Register commands
    LOGGER.at(Level.INFO).log("Registering commands...");
    this.getCommandRegistry().registerCommand(new CatCommand());
  }

  @Override
  protected void start() {
    super.start();
    LOGGER.at(Level.INFO).log("Starting Cats Plugin...");
  }

  @Override
  protected void shutdown() {
    super.shutdown();
    LOGGER.at(Level.INFO).log("Shutting down Cats Plugin...");
  }
}
