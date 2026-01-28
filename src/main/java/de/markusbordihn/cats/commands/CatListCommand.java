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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

final class CatListCommand extends CatCommand {

  public CatListCommand() {
    super("list", "Lists all your cats with name, UUID and position");
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    if (!context.isPlayer()) {
      context.sendMessage(Message.raw("This command can only be used by players").color("#FF0000"));
      return;
    }

    UUID playerUuid = context.sender().getUuid();
    if (playerUuid == null) {
      context.sendMessage(Message.raw("Unable to get player UUID").color("#FF0000"));
      return;
    }

    CatsManager catsManager = Main.getInstance().catsManager;
    if (catsManager == null) {
      context.sendMessage(Message.raw("Cats manager not available").color("#FF0000"));
      return;
    }

    Set<Ref<EntityStore>> playerCats = catsManager.getCatsByOwner(playerUuid);

    if (playerCats.isEmpty()) {
      context.sendMessage(Message.raw("=== Your Cats ===").color("#FFD700"));
      context.sendMessage(Message.raw("You don't have any cats yet.").color("#808080"));
      context.sendMessage(
          Message.raw("Tip: Tame a wild cat by giving it raw fish!").color("#FFFF00"));
      return;
    }

    context.sendMessage(
        Message.raw("=== Your Cats (" + playerCats.size() + ") ===").color("#FFD700"));

    int index = 1;
    for (Ref<EntityStore> catRef : playerCats) {
      if (!catRef.isValid()) {
        continue;
      }

      String catName = "Unnamed Cat";
      CatOwnerComponent ownerComponent =
          store.getComponent(catRef, CatOwnerComponent.getComponentType());
      if (ownerComponent != null) {
        String name = ownerComponent.getCatName();
        if (name != null && !name.isEmpty()) {
          catName = name;
        }
      }

      String catUuid = "Unknown";
      UUIDComponent uuidComponent = store.getComponent(catRef, UUIDComponent.getComponentType());
      if (uuidComponent != null && uuidComponent.getUuid() != null) {
        catUuid = uuidComponent.getUuid().toString();
      }

      String position = "Unknown";
      TransformComponent transformComponent =
          store.getComponent(catRef, TransformComponent.getComponentType());
      if (transformComponent != null) {
        Vector3d pos = transformComponent.getPosition();
        position = String.format("%.1f, %.1f, %.1f", pos.x, pos.y, pos.z);
      }

      context.sendMessage(Message.raw(""));
      context.sendMessage(Message.raw(index + ". " + catName).color("#00FFFF"));
      context.sendMessage(Message.raw("   UUID: " + catUuid).color("#808080"));
      context.sendMessage(Message.raw("   Position: " + position).color("#FFAA00"));

      index++;
    }

    context.sendMessage(Message.raw(""));
    context.sendMessage(
        Message.raw("Tip: Use /cat info while looking at a cat for more details").color("#FFFF00"));
  }
}
