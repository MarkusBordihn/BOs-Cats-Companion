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
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.CatType;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.Optional;
import java.util.logging.Level;
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
      context.sendMessage(
          Message.translation("cats.commands.error.no_cat").color(Constants.COLOR_ERROR));
      return;
    }

    Ref<EntityStore> entityRef = entityRefOpt.get();

    if (!checkOwnership(entityRef, store, context)) {
      return;
    }

    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());
    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      context.sendMessage(
          Message.translation("cats.commands.release.already_wild").color(Constants.COLOR_WARNING));
      return;
    }

    String catName = getCatDisplayName(entityRef, store);
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      context.sendMessage(
          Message.translation("cats.commands.error.no_cat").color(Constants.COLOR_ERROR));
      return;
    }

    Role currentRole = npcEntity.getRole();
    CatType catType = CatType.fromRoleName(currentRole.getRoleName());
    String wildRoleName = catType.getWildRoleName();
    NPCPlugin npcPlugin = NPCPlugin.get();
    int wildRoleIndex =
        npcPlugin != null && !wildRoleName.isEmpty() ? npcPlugin.getIndex(wildRoleName) : -1;
    if (wildRoleIndex < 0) {
      LOGGER.at(Level.WARNING).log(
          "Failed to release cat %s: no wild role found for current role %s",
          catName, currentRole.getRoleName());
      context.sendMessage(
          Message.translation("cats.commands.error.no_cat").color(Constants.COLOR_ERROR));
      return;
    }

    RoleChangeSystem.requestRoleChange(
        entityRef, currentRole, wildRoleIndex, true, null, null, store);

    store.removeComponent(entityRef, CatOwnerComponent.getComponentType());
    var bedTargetType = CatBedTargetComponent.getComponentType();
    if (bedTargetType != null && store.getComponent(entityRef, bedTargetType) != null) {
      store.removeComponent(entityRef, bedTargetType);
    }
    DisplayNameSupport.setDisplayName(entityRef, null, true, store);

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager != null) {
      catsManager.updateCatState(entityRef, CatState.WANDERING, store);
      catsManager.removeCatData(entityRef, store);
    }

    context.sendMessage(
        Message.translation("cats.commands.release.success")
            .param("name", catName)
            .color(Constants.COLOR_SUCCESS));
    context.sendMessage(
        Message.translation("cats.commands.release.info").color(Constants.COLOR_INFO));
  }
}
