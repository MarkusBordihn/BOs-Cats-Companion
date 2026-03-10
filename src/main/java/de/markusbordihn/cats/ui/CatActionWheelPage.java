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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.HappinessLevel;
import de.markusbordihn.cats.interaction.InteractionOwner;
import de.markusbordihn.cats.manager.CatsManager;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatActionWheelPage
    extends InteractiveCustomUIPage<CatActionWheelPage.WheelEventData> {

  private static final String[] SLOT_IDS = {
    "follow_stop", "pet", "play", "sleep_wakeup", "wander_return", "bed_leave", "rename", ""
  };
  private static final String KEY_CMD = "CommandId";

  private final Ref<EntityStore> catRef;
  private final Player player;
  private final Ref<EntityStore> playerEntityRef;
  private final World world;
  private final boolean hasBed;
  private final CatState currentState;

  public CatActionWheelPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull Player player,
      @Nonnull Ref<EntityStore> playerEntityRef,
      @Nonnull World world,
      boolean hasBed,
      @Nullable CatState currentState) {
    super(playerRef, CustomPageLifetime.CanDismiss, WheelEventData.CODEC);
    this.catRef = catRef;
    this.player = player;
    this.playerEntityRef = playerEntityRef;
    this.world = world;
    this.hasBed = hasBed;
    this.currentState = currentState != null ? currentState : CatState.WANDERING;
  }

  public static CatActionWheelPage create(
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull Player player,
      @Nonnull Ref<EntityStore> playerEntityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull World world) {
    CatStateComponent stateComponent =
        store.getComponent(catRef, CatStateComponent.getComponentType());
    return new CatActionWheelPage(
        playerRef,
        catRef,
        player,
        playerEntityRef,
        world,
        CatActionHelper.hasBedAvailable(catRef, store, world),
        stateComponent != null ? stateComponent.getState() : CatState.WANDERING);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(Constants.UI_ACTION_WHEEL);
    commandBuilder.set("#CatsActionMenuWheel.Visible", true);
    commandBuilder.set("#CatsActionMenuTitle.Text", getCatDisplayName(store));
    commandBuilder.set("#CatsActionMenuSubtitle.Text", resolveMoodText(store));
    commandBuilder.set("#CatsActionWheelStopLabel.Text", Message.translation("cats.ui.wheel.stop"));
    commandBuilder.set(
        "#CatsActionWheelStatusHeader.Text", Message.translation("cats.ui.wheel.status_header"));
    commandBuilder.set("#CatsActionMenuCenterText.Text", resolveStateText());

    buildCommandButtons(commandBuilder, eventBuilder);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatsActionWheelCenterButton",
        EventData.of(KEY_CMD, "stop"),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull WheelEventData data) {
    if (data.commandId == null || data.commandId.isBlank()) {
      this.close();
      return;
    }

    switch (data.commandId) {
      case "stop" -> {
        CatActionHelper.stop(this.catRef, store);
        this.close();
      }
      case "follow_stop" -> {
        if (this.currentState == CatState.FOLLOWING) {
          CatActionHelper.sit(this.catRef, store);
        } else {
          CatActionHelper.follow(this.catRef, store);
        }
        this.close();
      }
      case "pet" -> {
        NPCEntity npc = store.getComponent(this.catRef, NPCEntity.getComponentType());
        if (npc != null && npc.getRole() != null) {
          InteractionOwner.pet(this.catRef, npc.getRole(), store, this.player);
        }
        this.close();
      }
      case "play" -> {
        CatActionHelper.play(this.catRef, store);
        this.close();
      }
      case "sleep_wakeup" -> {
        if (this.currentState == CatState.SLEEPING) {
          CatActionHelper.stop(this.catRef, store);
        } else {
          CatActionHelper.sleep(this.catRef, store);
        }
        this.close();
      }
      case "wander_return" -> {
        if (this.currentState == CatState.WANDERING) {
          CatActionHelper.stop(this.catRef, store);
        } else {
          CatActionHelper.wander(this.catRef, store);
        }
        this.close();
      }
      case "bed_leave" -> {
        if (this.currentState == CatState.GOING_TO_BED || this.currentState == CatState.SLEEPING) {
          CatActionHelper.leaveBed(this.catRef, store);
        } else {
          CatActionHelper.goToBed(this.catRef, store, this.world);
        }
        this.close();
      }
      case "rename" -> {
        String currentName = getCatDisplayName(store);
        CatNameInputPage namePage =
            new CatNameInputPage(this.playerRef, this.catRef, currentName, null, null);
        this.player.getPageManager().openCustomPage(this.playerEntityRef, store, namePage);
      }
      default -> this.close();
    }
  }

  private void buildCommandButtons(
      @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
    for (int i = 0; i < 8; i++) {
      String buttonId = "#CatsActionWheelButton" + i;
      String labelId = "#CatsActionWheelLabel" + i;

      // Slot 7 always hidden (unused)
      if (i == 7) {
        commandBuilder.set(buttonId + ".Visible", false);
        commandBuilder.set(labelId + ".Visible", false);
        continue;
      }

      // Slot 5 (bed) shown but non-interactive when no bed is nearby
      if (i == 5 && !this.hasBed) {
        commandBuilder.set(buttonId + ".Visible", true);
        commandBuilder.set(buttonId + ".Text", "");
        commandBuilder.set(labelId + ".Visible", true);
        commandBuilder.set(
            labelId + ".Text", Message.translation("cats.ui.wheel.slot.bed_unavailable"));
        continue;
      }

      commandBuilder.set(buttonId + ".Visible", true);
      commandBuilder.set(buttonId + ".Text", "");
      commandBuilder.set(labelId + ".Visible", true);
      commandBuilder.set(labelId + ".Text", resolveSlotLabel(i));
      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating, buttonId, EventData.of(KEY_CMD, SLOT_IDS[i]), false);
    }
  }

  @Nonnull
  private Message resolveSlotLabel(int slot) {
    return switch (slot) {
      case 0 ->
          this.currentState == CatState.FOLLOWING
              ? Message.translation("cats.ui.wheel.slot.stop")
              : Message.translation("cats.ui.wheel.slot.follow");
      case 1 -> Message.translation("cats.ui.wheel.slot.pet");
      case 2 -> Message.translation("cats.ui.wheel.slot.play");
      case 3 ->
          this.currentState == CatState.SLEEPING
              ? Message.translation("cats.ui.wheel.slot.wakeup")
              : Message.translation("cats.ui.wheel.slot.sleep");
      case 4 ->
          this.currentState == CatState.WANDERING
              ? Message.translation("cats.ui.wheel.slot.return")
              : Message.translation("cats.ui.wheel.slot.wander");
      case 5 ->
          (this.currentState == CatState.GOING_TO_BED || this.currentState == CatState.SLEEPING)
              ? Message.translation("cats.ui.wheel.slot.leave_bed")
              : Message.translation("cats.ui.wheel.slot.bed");
      case 6 -> Message.translation("cats.ui.wheel.slot.rename");
      default -> Message.raw("");
    };
  }

  @Nonnull
  private Message resolveStateText() {
    return switch (this.currentState) {
      case FOLLOWING -> Message.translation("cats.ui.state.following");
      case SITTING -> Message.translation("cats.ui.state.sitting");
      case SLEEPING -> Message.translation("cats.ui.state.sleeping");
      case PLAYING -> Message.translation("cats.ui.state.playing");
      case WANDERING -> Message.translation("cats.ui.state.wandering");
      case GOING_TO_BED -> Message.translation("cats.ui.state.going_to_bed");
      case WAITING -> Message.translation("cats.ui.state.waiting");
      case ATTACKING -> Message.translation("cats.ui.state.pouncing");
      case SEARCHING -> Message.translation("cats.ui.state.searching");
    };
  }

  @Nonnull
  private Message resolveMoodText(@Nonnull Store<EntityStore> store) {
    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      return Message.raw("");
    }
    HappinessLevel level = catsManager.getHappinessLevel(this.catRef, store);
    if (level == null) {
      return Message.raw("");
    }
    return switch (level) {
      case MISERABLE -> Message.translation("cats.ui.mood.miserable");
      case SAD -> Message.translation("cats.ui.mood.sad");
      case NEUTRAL -> Message.translation("cats.ui.mood.neutral");
      case HAPPY -> Message.translation("cats.ui.mood.happy");
      case ECSTATIC -> Message.translation("cats.ui.mood.ecstatic");
    };
  }

  @Nonnull
  private String getCatDisplayName(@Nonnull Store<EntityStore> store) {
    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      return "Cat";
    }
    String name = catsManager.getCatDisplayName(this.catRef, store);
    return name != null ? name : "Cat";
  }

  public static final class WheelEventData {
    public static final BuilderCodec<WheelEventData> CODEC =
        ((BuilderCodec.Builder<WheelEventData>)
                BuilderCodec.builder(WheelEventData.class, WheelEventData::new)
                    .append(
                        new KeyedCodec(KEY_CMD, Codec.STRING),
                        (data, value) -> data.commandId = value,
                        data -> data.commandId)
                    .add())
            .build();

    private String commandId;
  }
}
