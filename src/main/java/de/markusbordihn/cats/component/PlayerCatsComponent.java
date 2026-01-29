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

package de.markusbordihn.cats.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.UUIDBinaryCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Main;
import de.markusbordihn.cats.data.PlayerCatsData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PlayerCatsComponent implements Component<EntityStore> {

  public static final String OWNED_CAT_UUIDS_TAG = "OwnedCatUUIDs";

  @Nonnull
  public static final BuilderCodec<PlayerCatsComponent> CODEC =
      BuilderCodec.builder(PlayerCatsComponent.class, PlayerCatsComponent::new)
          .append(
              new KeyedCodec<>(
                  OWNED_CAT_UUIDS_TAG, new SetCodec<>(new UUIDBinaryCodec(), HashSet::new, false)),
              (component, value) -> component.data = PlayerCatsData.fromSet(value),
              component -> component.data.getOwnedCatUUIDs())
          .documentation("Set of UUIDs of cats owned by this player.")
          .add()
          .build();

  @Nonnull private PlayerCatsData data;

  public PlayerCatsComponent() {
    this.data = PlayerCatsData.empty();
  }

  public PlayerCatsComponent(@Nonnull PlayerCatsData data) {
    this.data = data;
  }

  public static ComponentType<EntityStore, PlayerCatsComponent> getComponentType() {
    return Main.getInstance().playerCatsComponentType;
  }

  @Nonnull
  public PlayerCatsData getData() {
    return data;
  }

  public void setData(@Nonnull PlayerCatsData data) {
    this.data = data;
  }

  public int getCatCount() {
    return data.getCatCount();
  }

  public boolean hasCat(@Nonnull UUID catUuid) {
    return data.hasCat(catUuid);
  }

  public void addCat(@Nonnull UUID catUuid) {
    this.data = data.withAddedCat(catUuid);
  }

  public void removeCat(@Nonnull UUID catUuid) {
    this.data = data.withRemovedCat(catUuid);
  }

  @Nonnull
  public Set<UUID> getOwnedCats() {
    return data.getOwnedCats();
  }

  @Override
  @Nonnull
  public PlayerCatsComponent clone() {
    PlayerCatsComponent cloned = new PlayerCatsComponent();
    cloned.data = this.data;
    return cloned;
  }
}
