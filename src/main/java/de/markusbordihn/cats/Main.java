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
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.commands.CatCommand;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.handler.CatTamingHandler;
import de.markusbordihn.cats.system.CatOwnershipSystem;
import de.markusbordihn.cats.system.CatStateSyncSystem;
import de.markusbordihn.cats.system.CatStateSystem;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Main extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static Main instance;

  public ComponentType<EntityStore, CatOwnerComponent> catOwnerComponentType;
  public ComponentType<EntityStore, CatStateComponent> catStateComponentType;

  public Main(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static Main getInstance() {
    return instance;
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

    // Register commands
    LOGGER.at(Level.INFO).log("Registering commands...");
    this.getCommandRegistry().registerCommand(new CatCommand());

    // Register event handlers
    LOGGER.at(Level.INFO).log("Registering event handlers...");
    getEventRegistry()
        .registerGlobal(PlayerInteractEvent.class, CatTamingHandler::onPlayerInteract);

    LOGGER.at(Level.INFO).log("Cat systems ready - components handle persistence automatically");
  }

  @Override
  protected void start() {
    super.start();
    LOGGER.at(Level.INFO).log("Starting Cats Plugin...");
    LOGGER.at(Level.INFO).log("Cats Plugin started successfully!");
  }

  @Override
  protected void shutdown() {
    super.shutdown();
    LOGGER.at(Level.INFO).log("Shutting down Cats Plugin...");
    LOGGER.at(Level.INFO).log("Cats Plugin shutdown complete - components saved automatically");
  }
}
