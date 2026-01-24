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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.component.CatOwnerComponent;
import java.util.UUID;
import javax.annotation.Nonnull;

final class CatOwnerCommand extends AbstractWorldCommand {
  @Nonnull private final EntityWrappedArg entityArg;
  @Nonnull private final RequiredArg<String> ownerArg;

  public CatOwnerCommand() {
    super("owner", "Sets the owner of a cat NPC");
    this.entityArg = this.withOptionalArg("entity", "The cat entity to modify", ArgTypes.ENTITY_ID);
    this.ownerArg = this.withRequiredArg("owner", "The new owner (player name)", ArgTypes.STRING);
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> entityRef = this.entityArg.get(store, context);
    String ownerName = this.ownerArg.get(context);
    if (entityRef != null && entityRef.isValid()) {
      context.sendMessage(Message.raw("=== Set Cat Owner ===").color("#FFD700"));
      var uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());
      if (uuidComponent != null) {
        context.sendMessage(Message.raw("Cat UUID: " + uuidComponent.getUuid()).color("#808080"));
      }

      // Set the owner, for now, we'll create a dummy UUID based on the name
      UUID ownerId = UUID.nameUUIDFromBytes(("player:" + ownerName).getBytes());
      CatOwnerComponent ownerComponent = new CatOwnerComponent(ownerId, ownerName);
      store.putComponent(entityRef, Main.getInstance().catOwnerComponentType, ownerComponent);

      context.sendMessage(Message.raw(""));
      context.sendMessage(Message.raw("✓ Owner set to: " + ownerName).color("#00FF00"));
      context.sendMessage(
          Message.raw("The cat now belongs to " + ownerName + "!").color("#FFD700"));
    } else {
      context.sendMessage(Message.raw("No entity in view.").color("#FF0000"));
      context.sendMessage(
          Message.raw("Look at a cat and use: /cat owner <player>").color("#808080"));
    }
  }
}
