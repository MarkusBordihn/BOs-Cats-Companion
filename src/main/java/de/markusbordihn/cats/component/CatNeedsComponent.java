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
import com.hypixel.hytale.codec.codecs.simple.FloatCodec;
import com.hypixel.hytale.codec.codecs.simple.LongCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Cats;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.CatNeedsData;
import javax.annotation.Nonnull;

public class CatNeedsComponent implements Component<EntityStore> {

  public static final String ID = "CatNeeds";

  public static final String REST_NEED_TAG = "RestNeed";
  public static final String SOCIAL_NEED_TAG = "SocialNeed";
  public static final String PLAY_NEED_TAG = "PlayNeed";
  public static final String LAST_NEED_UPDATE_TAG = "LastNeedUpdate";

  private static final FloatCodec FLOAT_CODEC = new FloatCodec();
  private static final LongCodec LONG_CODEC = new LongCodec();

  @Nonnull
  public static final BuilderCodec<CatNeedsComponent> CODEC =
      BuilderCodec.builder(CatNeedsComponent.class, CatNeedsComponent::new)
          .append(
              new KeyedCodec<>(REST_NEED_TAG, FLOAT_CODEC),
              (component, value) -> component.data = component.data.withRestNeed(value),
              component -> component.data.restNeed())
          .documentation("The rest need of the cat (0-100).")
          .add()
          .append(
              new KeyedCodec<>(SOCIAL_NEED_TAG, FLOAT_CODEC),
              (component, value) -> component.data = component.data.withSocialNeed(value),
              component -> component.data.socialNeed())
          .documentation("The social need of the cat (0-100).")
          .add()
          .append(
              new KeyedCodec<>(PLAY_NEED_TAG, FLOAT_CODEC),
              (component, value) -> component.data = component.data.withPlayNeed(value),
              component -> component.data.playNeed())
          .documentation("The play need of the cat (0-100).")
          .add()
          .append(
              new KeyedCodec<>(LAST_NEED_UPDATE_TAG, LONG_CODEC),
              (component, value) -> component.data = component.data.withLastNeedUpdate(value),
              component -> component.data.lastNeedUpdate())
          .documentation("Timestamp of the last needs update.")
          .add()
          .build();

  @Nonnull private CatNeedsData data;

  public CatNeedsComponent() {
    this.data = CatNeedsData.defaultNeeds();
  }

  public CatNeedsComponent(@Nonnull CatNeedsData data) {
    this.data = data;
  }

  public static ComponentType<EntityStore, CatNeedsComponent> getComponentType() {
    return Cats.getInstance().catNeedsComponentType;
  }

  @Nonnull
  public CatNeedsData getData() {
    return data;
  }

  public void setData(@Nonnull CatNeedsData data) {
    this.data = data;
  }

  public float getRestNeed() {
    return data.restNeed();
  }

  public float getSocialNeed() {
    return data.socialNeed();
  }

  public float getPlayNeed() {
    return data.playNeed();
  }

  public long getLastNeedUpdate() {
    return data.lastNeedUpdate();
  }

  @Nonnull
  public CatNeedType getCriticalNeed() {
    return data.getCriticalNeed();
  }

  @Nonnull
  public CatNeedType getHighestNeed() {
    return data.getHighestNeed();
  }

  @Override
  @Nonnull
  public CatNeedsComponent clone() {
    return new CatNeedsComponent(this.data);
  }
}
