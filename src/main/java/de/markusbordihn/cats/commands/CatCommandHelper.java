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
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatOwnerComponent;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CatCommandHelper {

  default boolean checkOwnership(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandContext context) {
    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());

    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      return true;
    }

    String executingPlayer = getExecutingPlayerName(context);
    if (executingPlayer == null) {
      return true;
    }

    String ownerName = ownerComponent.getOwnerName();
    if (ownerName != null && ownerName.equals(executingPlayer)) {
      return true;
    }

    context.sendMessage(
        Message.translation("cats.commands.error.not_owner")
            .param("owner", ownerName)
            .color("#FF0000"));
    return false;
  }

  @Nullable
  default String getExecutingPlayerName(@Nonnull CommandContext context) {
    if (!context.isPlayer()) {
      return null;
    }
    return context.sender().getDisplayName();
  }

  @Nonnull
  default Optional<Ref<EntityStore>> getEntityFromArgument(
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
  default String getCatDisplayName(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());

    if (ownerComponent != null) {
      String catName = ownerComponent.getCatName();
      if (catName != null && !catName.isEmpty()) {
        return catName;
      }
    }

    return "Cat";
  }
}
