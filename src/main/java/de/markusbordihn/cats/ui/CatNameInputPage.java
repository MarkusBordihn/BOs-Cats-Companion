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

package de.markusbordihn.cats.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatNameInputPage
    extends InteractiveCustomUIPage<CatNameInputPage.NameInputEventData> {

  private static final String KEY_ACTION = "Action";
  private static final String KEY_NAME_INPUT = "@CatNameInput";
  private static final String ACTION_CANCEL = "Cancel";

  private final Ref<EntityStore> catRef;
  private final String initialValue;
  private final Runnable cancelCallback;
  private final Consumer<String> submitCallback;
  private boolean handled;

  public CatNameInputPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> catRef,
      @Nullable String initialValue,
      @Nullable Runnable cancelCallback,
      @Nullable Consumer<String> submitCallback) {
    super(playerRef, CustomPageLifetime.CanDismiss, NameInputEventData.CODEC);
    this.catRef = catRef;
    this.initialValue = initialValue != null ? initialValue : "";
    this.cancelCallback = cancelCallback;
    this.submitCallback = submitCallback;
    this.handled = false;
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(Constants.UI_NAME_INPUT);
    commandBuilder.set("#CatNameInputField.Value", this.initialValue);
    commandBuilder.set("#CatNameInputField.MaxLength", 32);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatNameApplyButton",
        EventData.of(KEY_NAME_INPUT, "#CatNameInputField.Value"),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatNameCancelButton",
        EventData.of(KEY_ACTION, ACTION_CANCEL),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull NameInputEventData data) {
    this.handled = true;
    if (ACTION_CANCEL.equalsIgnoreCase(data.action)) {
      this.close();
      if (this.cancelCallback != null) {
        this.cancelCallback.run();
      }
      return;
    }

    String newName = data.nameInput != null ? data.nameInput.trim() : "";
    if (!newName.isEmpty() && CatsManager.getInstance() != null) {
      CatsManager.getInstance().updateCatName(this.catRef, newName, store);
    }
    this.close();
    if (this.submitCallback != null) {
      this.submitCallback.accept(newName);
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    if (!this.handled) {
      this.handled = true;
      if (this.cancelCallback != null) {
        this.cancelCallback.run();
      }
    }
  }

  public static final class NameInputEventData {
    public static final BuilderCodec<NameInputEventData> CODEC =
        ((BuilderCodec.Builder<NameInputEventData>)
                ((BuilderCodec.Builder<NameInputEventData>)
                        BuilderCodec.builder(NameInputEventData.class, NameInputEventData::new)
                            .append(
                                new KeyedCodec(KEY_ACTION, Codec.STRING),
                                (data, value) -> data.action = value,
                                data -> data.action)
                            .add())
                    .append(
                        new KeyedCodec(KEY_NAME_INPUT, Codec.STRING),
                        (data, value) -> data.nameInput = value,
                        data -> data.nameInput)
                    .add())
            .build();

    private String action;
    private String nameInput;
  }
}
