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
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import javax.annotation.Nonnull;

final class CatInfoCommand extends CatCommand {
  @Nonnull private final EntityWrappedArg entityArg;

  public CatInfoCommand() {
    super("info", "Shows information about a cat NPC");
    this.entityArg = this.withOptionalArg("entity", "The cat entity to check", ArgTypes.ENTITY_ID);
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {

    var entityOpt = getEntityFromArgument(this.entityArg, store, context);
    if (entityOpt.isPresent()) {
      Ref<EntityStore> entityRef = entityOpt.get();
      context.sendMessage(Message.raw("=== Cat Info ===").color("#FFD700"));

      // Display UUID and Ref Index
      UUIDComponent uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());
      if (uuidComponent != null) {
        context.sendMessage(
            Message.raw("UUID: " + uuidComponent.getUuid()).color(Constants.COLOR_GRAY));
      }
      context.sendMessage(
          Message.raw("Ref Index: " + entityRef.getIndex()).color(Constants.COLOR_GRAY));

      // Display health information (if available from NPC config)
      context.sendMessage(Message.raw("Health: 20/20 (Max)").color("#FF6B6B"));

      // Display owner information
      CatOwnerComponent ownerComponent =
          store.getComponent(entityRef, CatOwnerComponent.getComponentType());
      if (ownerComponent != null && ownerComponent.hasOwner()) {
        context.sendMessage(
            Message.raw("Owner: " + ownerComponent.getOwnerName()).color(Constants.COLOR_SUCCESS));

        String catName = ownerComponent.getCatName();
        if (catName != null && !catName.isEmpty()) {
          context.sendMessage(Message.raw("Name: " + catName).color("#00FFFF"));
        } else {
          context.sendMessage(Message.raw("Name: (unnamed)").color(Constants.COLOR_GRAY));
        }
      } else {
        context.sendMessage(Message.raw("Owner: None (untamed)").color(Constants.COLOR_GRAY));
      }

      // Display state information
      CatStateComponent stateComponent =
          store.getComponent(entityRef, CatStateComponent.getComponentType());
      if (stateComponent != null) {
        String stateColor =
            switch (stateComponent.getState()) {
              case SITTING -> "#FFA500";
              case SLEEPING -> "#9370DB";
              case FOLLOWING -> Constants.COLOR_SUCCESS;
              case PLAYING -> "#FF69B4";
              case SEARCHING -> "#FFD700";
              case WAITING -> "#87CEEB";
              default -> Constants.COLOR_INFO;
            };
        context.sendMessage(Message.raw("State: " + stateComponent.getState()).color(stateColor));
      }

      context.sendMessage(Message.raw(""));
      context.sendMessage(
          Message.raw(
                  "Tip: Use /cat sit, /cat sleep, /cat follow, /cat wait, /cat play, /cat search")
              .color(Constants.COLOR_INFO));
    } else {
      context.sendMessage(Message.raw("No entity in view.").color(Constants.COLOR_ERROR));
      context.sendMessage(
          Message.raw("Look at a cat and use: /cat info").color(Constants.COLOR_GRAY));
    }
  }
}
