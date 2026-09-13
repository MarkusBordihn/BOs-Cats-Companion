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
import org.joml.Vector3d;

public final class YarnBallGroundRegistry {

  private static final long MAX_AGE_MS = 5 * 60 * 1000L;

  private static final ConcurrentHashMap<UUID, Entry> entries = new ConcurrentHashMap<>();

  private YarnBallGroundRegistry() {}

  public static void register(UUID ownerUuid, Vector3d position) {
    entries.put(ownerUuid, new Entry(position, System.currentTimeMillis()));
  }

  @Nullable
  public static Vector3d claim(UUID ownerUuid) {
    Entry entry = entries.remove(ownerUuid);
    if (entry == null || System.currentTimeMillis() - entry.timestamp > MAX_AGE_MS) {
      return null;
    }

    return entry.position;
  }

  public static void clear(UUID ownerUuid) {
    entries.remove(ownerUuid);
  }

  private record Entry(Vector3d position, long timestamp) {}
}
