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

package de.markusbordihn.cats.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.Optional;
import javax.annotation.Nonnull;

final class CatReleaseCommand extends CatCommand {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final EntityWrappedArg entityArg;

  public CatReleaseCommand() {
    super("release", "Releases a cat back to the wild, removing your ownership");
    this.entityArg = this.withOptionalArg("entity", "The cat entity", ArgTypes.ENTITY_ID);
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    Optional<Ref<EntityStore>> entityRefOpt = getEntityFromArgument(this.entityArg, store, context);
    if (entityRefOpt.isEmpty()) {
      context.sendMessage(Message.translation("cats.commands.error.no_cat").color("#FF0000"));
      return;
    }

    Ref<EntityStore> entityRef = entityRefOpt.get();

    // Check ownership before allowing release
    if (!checkOwnership(entityRef, store, context)) {
      return;
    }

    // Get owner info
    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());
    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      context.sendMessage(Message.raw("This cat is already wild!").color("#FFAA00"));
      return;
    }

    String catName = getCatDisplayName(entityRef, store);
    store.removeComponent(entityRef, CatOwnerComponent.getComponentType());

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager != null) {
      catsManager.unregisterOwner(entityRef, store);
      catsManager.updateCatState(entityRef, CatState.WANDERING, store);
    }

    context.sendMessage(
        Message.raw("✓ " + catName + " has been released back to the wild!").color("#00FF00"));
    context.sendMessage(
        Message.raw("The cat will now wander freely and can be tamed by any player.")
            .color("#FFFF00"));
  }
}
