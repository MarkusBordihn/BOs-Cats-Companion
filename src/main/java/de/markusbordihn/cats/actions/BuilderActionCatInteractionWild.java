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

package de.markusbordihn.cats.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.interaction.ItemInteraction;
import javax.annotation.Nonnull;

public class BuilderActionCatInteractionWild extends BuilderActionCatInteractionBase {

  public static final String BUILDER_ID = "CatInteractionWild";

  @Override
  public String getBuilderId() {
    return BUILDER_ID;
  }

  @Nonnull
  @Override
  public ActionCatInteractionWild build(BuilderSupport support) {
    return new ActionCatInteractionWild(this, support);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Handles interactions with wild cats";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Routes held-item interactions for wild cats and ignores empty-hand interaction.";
  }

  public static class ActionCatInteractionWild extends ActionCatInteractionBase {

    public ActionCatInteractionWild(
        BuilderActionCatInteractionBase builder, BuilderSupport support) {
      super(builder);
    }

    @Override
    public boolean canExecute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      return !isCatTamed(entityRef, store);
    }

    @Override
    public boolean execute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      Ref<EntityStore> playerRef = getPlayerRefFromInfoProvider(role, infoProvider);
      Player player =
          playerRef != null ? store.getComponent(playerRef, Player.getComponentType()) : null;
      ItemStack heldItem = getHeldItem(playerRef, store);

      if (heldItem != null && heldItem.isValid()) {
        return ItemInteraction.handle(entityRef, role, store, player, heldItem);
      }
      return false;
    }
  }
}
