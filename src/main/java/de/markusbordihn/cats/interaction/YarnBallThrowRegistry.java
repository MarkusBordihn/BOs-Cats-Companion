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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public final class YarnBallThrowRegistry {

  private static final ConcurrentHashMap<UUID, Long> pendingThrows = new ConcurrentHashMap<>();

  private YarnBallThrowRegistry() {}

  public static void register(UUID playerUuid) {
    pendingThrows.put(playerUuid, System.currentTimeMillis());
  }

  public static void complete(UUID playerUuid) {
    pendingThrows.remove(playerUuid);
  }

  @Nullable
  public static UUID getRecentThrower(long maxAgeMs) {
    long now = System.currentTimeMillis();
    for (var entry : pendingThrows.entrySet()) {
      if (now - entry.getValue() <= maxAgeMs) {
        return entry.getKey();
      }
    }
    return null;
  }
}
