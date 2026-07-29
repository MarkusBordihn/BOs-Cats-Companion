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

package de.markusbordihn.cats.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.component.CatYarnBallProjectileComponent;
import java.util.UUID;
import javax.annotation.Nonnull;

public class CatYarnBallSpawnInteraction extends SimpleInstantInteraction {

  public static final String ID = "Cat_Yarn_Ball_Projectile_Spawn_Register";

  @Nonnull
  public static final BuilderCodec<CatYarnBallSpawnInteraction> CODEC =
      BuilderCodec.builder(
              CatYarnBallSpawnInteraction.class,
              CatYarnBallSpawnInteraction::new,
              SimpleInstantInteraction.CODEC)
          .documentation("")
          .build();

  public CatYarnBallSpawnInteraction() {}

  @Override
  protected void firstRun(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler) {

    CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
    Ref<EntityStore> projectileRef = context.getEntity();
    if (commandBuffer == null || projectileRef == null || !projectileRef.isValid()) {
      return;
    }

    Store<EntityStore> store = commandBuffer.getStore();
    UUID ownerUuid =
        CatYarnBallFetchInteraction.resolveThrowingPlayerUuid(context, commandBuffer, store);
    if (ownerUuid == null) {
      return;
    }

    YarnBallThrowRegistry.registerProjectile(ownerUuid, projectileRef);
    commandBuffer.putComponent(
        projectileRef,
        CatYarnBallProjectileComponent.getComponentType(),
        new CatYarnBallProjectileComponent(ownerUuid));
  }
}
