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

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.damage.CatDamageFilterSystem;
import java.util.logging.Level;

public class DamageSetupHandler {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private final ComponentRegistryProxy<EntityStore> entityStoreRegistry;
  private boolean registered = false;

  public DamageSetupHandler(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
    this.entityStoreRegistry = entityStoreRegistry;
  }

  public boolean tryRegister() {
    if (this.registered) {
      return true;
    }

    DamageModule damageModule = DamageModule.get();
    if (damageModule == null || damageModule.getFilterDamageGroup() == null) {
      return false;
    }

    this.entityStoreRegistry.registerSystem(new CatDamageFilterSystem());
    this.registered = true;
    LOGGER.at(Level.INFO).log("Cat damage filter system registered");
    return true;
  }

  public void onPluginSetup(PluginSetupEvent event) {
    if (!this.registered && event.getPlugin() instanceof DamageModule) {
      this.entityStoreRegistry.registerSystem(new CatDamageFilterSystem());
      this.registered = true;
      LOGGER.at(Level.INFO).log("Cat damage filter system registered (deferred)");
    }
  }
}
