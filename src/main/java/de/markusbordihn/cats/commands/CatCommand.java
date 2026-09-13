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
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.NoPermissionException;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.permission.PermissionManager;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CatCommand extends AbstractWorldCommand {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  protected CatCommand(@Nonnull String name, @Nonnull String description) {
    super(name, description);
  }

  protected boolean requiresOp() {
    return false;
  }

  protected void checkPermissionAlways(@Nonnull CommandContext context, @Nonnull String permission)
      throws NoPermissionException {
    PermissionManager.checkPermissionAlways(context, permission);
  }

  protected boolean hasAdminBypass(@Nonnull CommandContext context) {
    return PermissionManager.hasAdminBypass(context);
  }

  protected int getCatLimit(@Nonnull CommandContext context) {
    return PermissionManager.getCatLimit(context);
  }

  protected boolean checkOwnership(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandContext context) {

    if (this.hasAdminBypass(context)) {
      return true;
    }

    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());

    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      return true;
    }

    UUID executingPlayerUuid = this.getExecutingPlayerUuid(context);
    String executingPlayer = this.getExecutingPlayerName(context);
    if (executingPlayerUuid == null && executingPlayer == null) {
      return true;
    }

    UUID ownerUuid = ownerComponent.getOwnerUUID();
    if (ownerUuid != null && ownerUuid.equals(executingPlayerUuid)) {
      return true;
    }

    String ownerName = ownerComponent.getOwnerName();
    if (ownerUuid == null && ownerName != null && ownerName.equals(executingPlayer)) {
      if (executingPlayerUuid != null) {
        ownerComponent.setOwnerId(executingPlayerUuid);
        store.putComponent(entityRef, CatOwnerComponent.getComponentType(), ownerComponent);
        CatsManager catsManager = CatsManager.getInstance();
        if (catsManager != null) {
          catsManager.registerOwner(entityRef, executingPlayerUuid, store);
        }
      }
      return true;
    }

    String catUuid = "unknown";
    UUIDComponent uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());
    if (uuidComponent != null && uuidComponent.getUuid() != null) {
      catUuid = uuidComponent.getUuid().toString();
    }

    LOGGER.at(Level.WARNING).log(
        "SECURITY: Player '%s' (%s) attempted to access cat owned by '%s' (%s) (Cat UUID: %s)",
        executingPlayer, executingPlayerUuid, ownerName, ownerUuid, catUuid);

    String catName = this.getCatDisplayName(entityRef, store);
    context.sendMessage(
        Message.translation("cats.commands.error.not_owner")
            .param("name", catName)
            .param("owner", ownerName)
            .color(Constants.COLOR_ERROR));
    return false;
  }

  @Nullable
  protected String getExecutingPlayerName(@Nonnull CommandContext context) {
    if (!context.isPlayer()) {
      return null;
    }

    return context.sender().getUsername();
  }

  @Nullable
  protected UUID getExecutingPlayerUuid(@Nonnull CommandContext context) {
    if (!context.isPlayer()) {
      return null;
    }

    return context.sender().getUuid();
  }

  @Nonnull
  protected Optional<Ref<EntityStore>> getEntityFromArgument(
      @Nonnull EntityWrappedArg entityArg,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandContext context) {
    try {
      Ref<EntityStore> entityRef = entityArg.get(store, context);
      if (entityRef != null && entityRef.isValid()) {
        return Optional.of(entityRef);
      }
    } catch (Exception e) {
      // Entity not found or invalid argument
    }
    return Optional.empty();
  }

  @Nonnull
  protected String getCatDisplayName(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    Nameplate nameplate = store.getComponent(entityRef, Nameplate.getComponentType());
    if (nameplate != null) {
      String catName = nameplate.getText();
      if (catName != null && !catName.isEmpty()) {
        return catName;
      }
    }

    return "Cat";
  }
}
