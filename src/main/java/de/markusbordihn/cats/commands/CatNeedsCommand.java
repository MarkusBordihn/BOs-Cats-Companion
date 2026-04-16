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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatNeedsComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatNeedsData;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.ui.CatActionHelper;
import de.markusbordihn.cats.world.storage.CatsDataResource;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;

final class CatNeedsCommand extends CatCommand {

  @Nonnull private final EntityWrappedArg entityArg;
  @Nonnull private final RequiredArg<String> needTypeArg;
  @Nonnull private final RequiredArg<String> valueArg;

  public CatNeedsCommand() {
    super("needs", "[Debug] Set cat need values for testing (op only)");
    this.entityArg = this.withOptionalArg("entity", "The cat entity", ArgTypes.ENTITY_ID);
    this.needTypeArg =
        this.withRequiredArg("type", "Need type: rest, social, play, or all", ArgTypes.STRING);
    this.valueArg = this.withRequiredArg("value", "Need value (0-100)", ArgTypes.STRING);
  }

  @Override
  protected boolean requiresOp() {
    return true;
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {

    Optional<Ref<EntityStore>> entityRefOpt = getEntityFromArgument(this.entityArg, store, context);
    if (entityRefOpt.isEmpty()) {
      context.sendMessage(Message.raw("No cat entity in view.").color(Constants.COLOR_ERROR));
      return;
    }

    String needType = this.needTypeArg.get(context).toLowerCase();
    if (!needType.equals("rest")
        && !needType.equals("social")
        && !needType.equals("play")
        && !needType.equals("all")) {
      context.sendMessage(
          Message.raw("Unknown type. Use: rest, social, play, or all")
              .color(Constants.COLOR_ERROR));
      return;
    }

    float value;
    try {
      value = Float.parseFloat(this.valueArg.get(context));
    } catch (NumberFormatException numberFormatException) {
      context.sendMessage(
          Message.raw("Invalid value - must be a number (0-100).").color(Constants.COLOR_ERROR));
      return;
    }
    value = Math.max(0f, Math.min(100f, value));

    Ref<EntityStore> entityRef = entityRefOpt.get();
    UUID catUuid = CatsManager.getInstance().getUuid(entityRef, store);
    if (catUuid == null) {
      context.sendMessage(Message.raw("Not a cat entity.").color(Constants.COLOR_ERROR));
      return;
    }

    CatsDataResource resource = store.getResource(CatsDataResource.getResourceType());
    if (resource == null) {
      context.sendMessage(
          Message.raw("Cat data resource unavailable.").color(Constants.COLOR_ERROR));
      return;
    }

    CatDataEntry catData = resource.getCat(catUuid);
    if (catData == null) {
      context.sendMessage(Message.raw("Cat data not found.").color(Constants.COLOR_ERROR));
      return;
    }

    CatDataEntry updated =
        switch (needType) {
          case "rest" -> catData.withRestNeed(value);
          case "social" -> catData.withSocialNeed(value);
          case "play" -> catData.withPlayNeed(value);
          default -> catData.withRestNeed(value).withSocialNeed(value).withPlayNeed(value);
        };
    resource.updateCat(catUuid, updated);

    CatNeedsComponent needsComponent =
        store.getComponent(entityRef, CatNeedsComponent.getComponentType());
    CatNeedsData newNeedsData =
        new CatNeedsData(
            updated.restNeed(), updated.socialNeed(), updated.playNeed(), updated.lastNeedUpdate());
    if (needsComponent != null) {
      needsComponent.setData(newNeedsData);
    } else {
      store.putComponent(
          entityRef, CatNeedsComponent.getComponentType(), new CatNeedsComponent(newNeedsData));
    }

    CatActionHelper.stop(entityRef, store);

    context.sendMessage(
        Message.raw(
                String.format(
                    "[Debug] %s needs set - rest=%.0f social=%.0f play=%.0f",
                    getCatDisplayName(entityRef, store),
                    updated.restNeed(),
                    updated.socialNeed(),
                    updated.playNeed()))
            .color(Constants.COLOR_GOLD));
  }
}
