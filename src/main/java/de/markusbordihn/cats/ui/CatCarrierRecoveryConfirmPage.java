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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.markusbordihn.cats.Constants;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatCarrierRecoveryConfirmPage
    extends InteractiveCustomUIPage<CatCarrierRecoveryConfirmPage.ConfirmEventData> {

  private static final String KEY_ACTION = "Action";
  private static final String ACTION_CONFIRM = "Confirm";
  private static final String ACTION_CANCEL = "Cancel";

  private final String catName;
  private final Runnable cancelCallback;
  private final Runnable confirmCallback;
  private boolean handled;

  public CatCarrierRecoveryConfirmPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull String catName,
      @Nullable Runnable cancelCallback,
      @Nullable Runnable confirmCallback) {
    super(playerRef, CustomPageLifetime.CanDismiss, ConfirmEventData.CODEC);
    this.catName = catName;
    this.cancelCallback = cancelCallback;
    this.confirmCallback = confirmCallback;
    this.handled = false;
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(Constants.UI_CARRIER_RECOVERY_CONFIRM);
    commandBuilder.set(
        "#CatCarrierRecoveryConfirmTitle.Text",
        Message.translation("cats.ui.carrier_recovery.title"));
    commandBuilder.set(
        "#CatCarrierRecoveryConfirmSubtitle.Text",
        Message.translation("cats.ui.carrier_recovery.subtitle").param("catName", this.catName));
    commandBuilder.set(
        "#CatCarrierRecoveryConfirmWarning.Text",
        Message.translation("cats.ui.carrier_recovery.warning"));
    commandBuilder.set(
        "#CatCarrierRecoveryConfirmCancelButton.Text",
        Message.translation("cats.ui.carrier_recovery.cancel"));
    commandBuilder.set(
        "#CatCarrierRecoveryConfirmApplyButton.Text",
        Message.translation("cats.ui.carrier_recovery.confirm"));
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatCarrierRecoveryConfirmCancelButton",
        EventData.of(KEY_ACTION, ACTION_CANCEL),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatCarrierRecoveryConfirmApplyButton",
        EventData.of(KEY_ACTION, ACTION_CONFIRM),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull ConfirmEventData data) {
    this.handled = true;
    this.close();
    if (ACTION_CONFIRM.equalsIgnoreCase(data.action)) {
      if (this.confirmCallback != null) {
        this.confirmCallback.run();
      }
      return;
    }

    if (this.cancelCallback != null) {
      this.cancelCallback.run();
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

  public static final class ConfirmEventData {
    public static final BuilderCodec<ConfirmEventData> CODEC =
        ((BuilderCodec.Builder<ConfirmEventData>)
                BuilderCodec.builder(ConfirmEventData.class, ConfirmEventData::new)
                    .append(
                        new KeyedCodec(KEY_ACTION, Codec.STRING),
                        (data, value) -> data.action = value,
                        data -> data.action)
                    .add())
            .build();

    private String action;
  }
}
