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
import de.markusbordihn.cats.data.HappinessLevel;
import de.markusbordihn.cats.data.PersonalityType;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatTamingSuccessPage
    extends InteractiveCustomUIPage<CatTamingSuccessPage.TamingSuccessEventData> {

  private static final String KEY_NAME_INPUT = "@CatTamingNameInput";

  private final UUID catUuid;
  private final String catType;
  private final String initialName;
  private final PersonalityType primaryPersonality;
  private final PersonalityType secondaryPersonality;
  private final int happiness;

  public CatTamingSuccessPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull UUID catUuid,
      @Nullable String catType,
      @Nonnull String initialName,
      @Nullable PersonalityType primaryPersonality,
      @Nullable PersonalityType secondaryPersonality,
      int happiness) {
    super(playerRef, CustomPageLifetime.CanDismiss, TamingSuccessEventData.CODEC);
    this.catUuid = catUuid;
    this.catType = catType != null ? catType : "Cat";
    this.initialName = initialName;
    this.primaryPersonality = primaryPersonality;
    this.secondaryPersonality = secondaryPersonality;
    this.happiness = happiness;
  }

  private static String formatEnum(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
    return name.charAt(0) + name.substring(1).toLowerCase().replace('_', ' ');
  }

  private static String formatCatType(String type) {
    if (type == null || type.isEmpty()) {
      return "Cat";
    }
    return type.replaceFirst("^Cats?_", "")
        .replaceFirst("_Tamed$", "")
        .replaceAll("([A-Z])", " $1")
        .trim();
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(Constants.UI_TAMING_SUCCESS);
    commandBuilder.set(
        "#CatTamingSuccessCongrats.Text",
        Message.translation("cats.ui.taming_success.congratulations"));
    commandBuilder.set("#CatTamingSuccessBreed.Text", formatCatType(this.catType));
    commandBuilder.set("#CatTamingSuccessPersonality.Text", resolvePersonalityText());
    commandBuilder.set("#CatTamingSuccessMood.Text", resolveMoodText());
    commandBuilder.set(
        "#CatTamingSuccessNameLabel.Text",
        Message.translation("cats.ui.taming_success.name_label"));
    commandBuilder.set("#CatTamingSuccessNameField.Value", this.initialName);
    commandBuilder.set("#CatTamingSuccessNameField.MaxLength", 32);
    commandBuilder.set(
        "#CatTamingSuccessNameField.PlaceholderText",
        Message.translation("cats.ui.taming_success.placeholder"));
    commandBuilder.set(
        "#CatTamingSuccessTip1.Text", Message.translation("cats.ui.taming_success.tip1"));
    commandBuilder.set(
        "#CatTamingSuccessTip2.Text", Message.translation("cats.ui.taming_success.tip2"));
    commandBuilder.set(
        "#CatTamingSuccessTip3.Text", Message.translation("cats.ui.taming_success.tip3"));
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatTamingSuccessConfirmButton",
        EventData.of(KEY_NAME_INPUT, "#CatTamingSuccessNameField.Value"),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull TamingSuccessEventData data) {
    if (data.nameInput != null) {
      String newName = data.nameInput.trim();
      if (!newName.isEmpty()
          && !newName.equals(this.initialName)
          && CatsManager.getInstance() != null) {
        Ref<EntityStore> catRef = CatsManager.getInstance().getCatByUuid(this.catUuid, store);
        if (catRef != null) {
          CatsManager.getInstance().updateCatName(catRef, newName, store);
        }
      }
    }
    this.close();
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  private String resolvePersonalityText() {
    if (this.primaryPersonality == null) {
      return "";
    }
    if (this.secondaryPersonality != null) {
      return formatEnum(this.primaryPersonality.name())
          + " / "
          + formatEnum(this.secondaryPersonality.name());
    }
    return formatEnum(this.primaryPersonality.name());
  }

  private String resolveMoodText() {
    return formatEnum(HappinessLevel.fromValue(this.happiness).name());
  }

  public static final class TamingSuccessEventData {
    public static final BuilderCodec<TamingSuccessEventData> CODEC =
        ((BuilderCodec.Builder<TamingSuccessEventData>)
                BuilderCodec.builder(TamingSuccessEventData.class, TamingSuccessEventData::new)
                    .append(
                        new KeyedCodec(KEY_NAME_INPUT, Codec.STRING),
                        (data, value) -> data.nameInput = value,
                        data -> data.nameInput)
                    .add())
            .build();

    private String nameInput;
  }
}
