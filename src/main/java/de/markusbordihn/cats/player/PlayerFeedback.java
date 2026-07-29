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

package de.markusbordihn.cats.player;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PlayerFeedback {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private PlayerFeedback() {}

  public static void sendMessage(
      @Nonnull ComponentAccessor<EntityStore> accessor,
      @Nullable Player player,
      @Nonnull Message message) {
    sendMessage(resolve(accessor, player), message);
  }

  public static void sendMessage(@Nullable PlayerRef playerRef, @Nonnull Message message) {
    if (playerRef == null) {
      LOGGER.at(Level.WARNING).log("Cannot send message: PlayerRef is unavailable");
      return;
    }

    playerRef.sendMessage(message);
  }

  @Nullable
  public static PlayerRef resolve(
      @Nonnull ComponentAccessor<EntityStore> accessor, @Nullable Player player) {
    if (player == null) {
      return null;
    }

    Ref<EntityStore> playerEntityRef = player.getReference();
    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return null;
    }

    return accessor.getComponent(playerEntityRef, PlayerRef.getComponentType());
  }
}
