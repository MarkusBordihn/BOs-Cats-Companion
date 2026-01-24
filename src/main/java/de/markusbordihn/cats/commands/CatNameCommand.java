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
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.component.CatOwnerComponent;
import javax.annotation.Nonnull;

final class CatNameCommand extends AbstractWorldCommand {
  @Nonnull private final EntityWrappedArg entityArg;
  @Nonnull private final RequiredArg<String> nameArg;

  public CatNameCommand() {
    super("name", "Names your cat");
    this.entityArg = this.withOptionalArg("entity", "The cat entity", ArgTypes.ENTITY_ID);
    this.nameArg = this.withRequiredArg("name", "The name for your cat", ArgTypes.STRING);
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> entityRef = this.entityArg.get(store, context);
    String catName = this.nameArg.get(context);

    if (entityRef != null && entityRef.isValid()) {
      CatOwnerComponent ownerComponent =
          store.getComponent(entityRef, Main.getInstance().catOwnerComponentType);

      if (ownerComponent != null) {
        ownerComponent.setCatName(catName);
        store.putComponent(entityRef, Main.getInstance().catOwnerComponentType, ownerComponent);

        // Set Nameplate for nametag
        Nameplate nameplate = store.ensureAndGetComponent(entityRef, Nameplate.getComponentType());
        nameplate.setText(catName);

        context.sendMessage(
            Message.translation("cats.commands.name.success")
                .param("name", catName)
                .color("#00FF00"));
      } else {
        context.sendMessage(Message.translation("cats.commands.error.not_owned").color("#FF0000"));
      }
    } else {
      context.sendMessage(Message.translation("cats.commands.error.no_cat").color("#FF0000"));
    }
  }
}
