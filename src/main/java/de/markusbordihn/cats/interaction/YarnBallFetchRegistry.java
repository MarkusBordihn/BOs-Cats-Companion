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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public final class YarnBallFetchRegistry {

  private static final long MAX_LOCK_AGE_MS = 45_000L;

  private static final ConcurrentHashMap<UUID, Entry> activeFetchers = new ConcurrentHashMap<>();

  private YarnBallFetchRegistry() {}

  public static void register(@Nonnull UUID ownerUuid, @Nonnull Ref<EntityStore> catRef) {
    activeFetchers.put(ownerUuid, new Entry(catRef, System.currentTimeMillis()));
  }

  public static void complete(@Nonnull UUID ownerUuid) {
    activeFetchers.remove(ownerUuid);
  }

  public static boolean isFetching(@Nonnull UUID ownerUuid) {
    Entry entry = activeFetchers.get(ownerUuid);
    if (entry == null) {
      return false;
    }

    if (!entry.catRef.isValid()
        || System.currentTimeMillis() - entry.registeredAt > MAX_LOCK_AGE_MS) {
      activeFetchers.remove(ownerUuid);
      return false;
    }

    return true;
  }

  private record Entry(@Nonnull Ref<EntityStore> catRef, long registeredAt) {}
}
