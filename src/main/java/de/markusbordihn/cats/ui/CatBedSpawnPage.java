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
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.blocks.CatBed;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.CatType;
import de.markusbordihn.cats.manager.CatsManager;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public final class CatBedSpawnPage
    extends InteractiveCustomUIPage<CatBedSpawnPage.BedSpawnEventData> {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final double BED_EMPTY_RADIUS_SQ = 1.5 * 1.5;

  private static final String KEY_COMMAND = "Command";
  private static final String ACTION_CLOSE = "close";
  private static final String ACTION_EMPTY = "empty";
  private static final String ACTION_SPAWN = "spawn";
  private static final String ACTION_RECOVER = "recover";
  private static final String ACTION_CALL = "call";
  private static final String ACTION_WAKEUP = "wakeup";
  private static final String ACTION_SEPARATOR = ":";

  private Vector3d bedPosition;

  public CatBedSpawnPage(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, BedSpawnEventData.CODEC);
  }

  @Nonnull
  private static String resolveIconPath(@Nonnull CatType catType) {
    String role =
        switch (catType) {
          case KITTEN -> "Cats_Kitten_Calico_Tamed";
          default ->
              catType.getTamedRoleName().isEmpty()
                  ? "Cats_Calico_Tamed"
                  : catType.getTamedRoleName();
        };
    return "UI/Custom/Pages/Memories/npcs/" + role + ".png";
  }

  @Nonnull
  private static Message resolveStatusText(
      @Nonnull CatDataEntry catData, @Nullable CatState catState) {
    if (catData.isSpawned() && catState != null) {
      return Message.translation(catState.getTranslationKey());
    }

    return Message.translation(catData.status().getTranslationKey());
  }

  @Nonnull
  private static String resolveStatusColor(@Nonnull CatDataEntry catData) {
    return switch (catData.status()) {
      case SPAWNED -> Constants.COLOR_SUCCESS;
      case DESPAWNED -> Constants.COLOR_WARNING;
      case IN_CARRIER -> Constants.COLOR_ORANGE;
      default -> Constants.COLOR_GRAY;
    };
  }

  @Nullable
  private static Vector3d findNearestBedPosition(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nullable World world) {
    if (world == null) {
      return null;
    }

    TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
    if (transform == null) {
      return null;
    }

    return CatBed.findNearestBedPosition(world, transform.getPosition());
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(Constants.UI_BED_SPAWN);
    commandBuilder.set("#CatBedSpawnTitle.Text", Message.translation("cats.ui.bed_spawn.title"));
    commandBuilder.set(
        "#CatBedSpawnSubtitle.Text", Message.translation("cats.ui.bed_spawn.subtitle"));
    commandBuilder.set(
        "#CatBedSpawnCloseButton.Text", Message.translation("cats.ui.bed_spawn.close"));
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CatBedSpawnCloseButton",
        EventData.of(KEY_COMMAND, ACTION_CLOSE),
        false);

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      this.close();
      return;
    }

    World world = store.getExternalData().getWorld();
    this.bedPosition = findNearestBedPosition(ref, store, world);
    if (this.bedPosition == null) {
      LOGGER.at(Level.WARNING).log("No cat bed found near player %s", this.playerRef.getUuid());
      commandBuilder.set("#CatBedSpawnEmpty.Visible", true);
      commandBuilder.set(
          "#CatBedSpawnEmpty.Text", Message.translation("cats.ui.bed_spawn.no_bed_found"));
      return;
    }

    UUID playerUuid = this.playerRef.getUuid();
    CatsManager catsManager = CatsManager.getInstance();
    List<CatDataEntry> playerCats = new ArrayList<>();
    if (playerUuid != null && catsManager != null) {
      Collection<CatDataEntry> allCats = catsManager.getCatDataByOwner(playerUuid, store);
      for (CatDataEntry catData : allCats) {
        if (catData.catType() != CatType.UNKNOWN) {
          playerCats.add(catData);
        }
      }
    }

    if (playerCats.isEmpty()) {
      commandBuilder.set("#CatBedSpawnEmpty.Visible", true);
      commandBuilder.set(
          "#CatBedSpawnEmpty.Text", Message.translation("cats.ui.bed_spawn.no_cats"));
    } else {
      playerCats.sort(
          Comparator.comparing(CatDataEntry::isSpawned)
              .thenComparing(CatDataEntry::displayName, String.CASE_INSENSITIVE_ORDER));

      commandBuilder.clear("#CatBedSpawnList");
      commandBuilder.appendInline(
          "#CatBedSpawnList", "Group #CatBedSpawnRows { LayoutMode: Top; }");
      boolean anyInBed = false;
      for (int i = 0; i < playerCats.size(); i++) {
        CatDataEntry catData = playerCats.get(i);
        boolean isSpawned = catData.isSpawned();
        boolean isInCarrier = catData.isInCarrier();
        boolean isInBed = false;
        CatState catState = null;
        if (isSpawned && catsManager != null) {
          UUID catUuid = catData.uuid();
          if (catUuid != null) {
            Ref<EntityStore> catRef = catsManager.getCatByUuid(catUuid, store);
            if (catRef != null && catRef.isValid()) {
              CatStateComponent stateComp =
                  store.getComponent(catRef, CatStateComponent.getComponentType());
              if (stateComp != null) {
                catState = stateComp.getState();
                isInBed = catState.isSleepingState();
              }
            }
          }
        }
        if (isInBed) {
          anyInBed = true;
        }

        String actionCmd =
            isSpawned
                ? (isInBed ? ACTION_WAKEUP : ACTION_CALL)
                : isInCarrier ? ACTION_RECOVER : ACTION_SPAWN;
        String rowSelector = "#CatBedSpawnRows[" + i + "]";
        commandBuilder.append("#CatBedSpawnRows", Constants.UI_BED_SPAWN_ROW);
        commandBuilder.set(
            rowSelector + " #CatBedSpawnRowIcon.AssetPath", resolveIconPath(catData.catType()));
        commandBuilder.set(rowSelector + " #CatBedSpawnRowName.Text", catData.displayName());
        commandBuilder.set(rowSelector + " #CatBedSpawnRowName.Style.RenderBold", isInBed);
        commandBuilder.set(
            rowSelector + " #CatBedSpawnRowStatus.Text", resolveStatusText(catData, catState));
        commandBuilder.set(
            rowSelector + " #CatBedSpawnRowStatus.Style.TextColor", resolveStatusColor(catData));
        commandBuilder.set(
            rowSelector + " #CatBedSpawnRowButton.Text",
            Message.translation(
                isSpawned
                    ? (isInBed
                        ? "cats.ui.bed_spawn.wake_up_button"
                        : "cats.ui.bed_spawn.call_button")
                    : isInCarrier
                        ? "cats.ui.bed_spawn.recover_button"
                        : "cats.ui.bed_spawn.spawn_button"));
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            rowSelector + " #CatBedSpawnRowButton",
            EventData.of(KEY_COMMAND, actionCmd + ACTION_SEPARATOR + catData.uuid()),
            false);
      }
      commandBuilder.set("#CatBedSpawnCount.Text", playerCats.size() + " cats");
      if (anyInBed) {
        commandBuilder.set("#CatBedSpawnEmptyBedButton.Visible", true);
        commandBuilder.set(
            "#CatBedSpawnEmptyBedButton.Text",
            Message.translation("cats.ui.bed_spawn.empty_button"));
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CatBedSpawnEmptyBedButton",
            EventData.of(KEY_COMMAND, ACTION_EMPTY),
            false);
      }
    }
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull BedSpawnEventData data) {
    if (data.command == null || data.command.isBlank() || ACTION_CLOSE.equals(data.command)) {
      this.close();
      return;
    }

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      this.close();
      return;
    }

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      this.sendGenericError();
      this.close();
      return;
    }

    if (ACTION_EMPTY.equals(data.command)) {
      this.handleEmptyBed(ref, catsManager, store);
      this.close();
      return;
    }

    int separatorIndex = data.command.indexOf(ACTION_SEPARATOR);
    if (separatorIndex < 0) {
      this.close();
      return;
    }

    String catUuidString = data.command.substring(separatorIndex + 1);
    UUID catUuid;
    try {
      catUuid = UUID.fromString(catUuidString);
    } catch (IllegalArgumentException e) {
      LOGGER.at(Level.WARNING).log("Invalid cat UUID from bed spawn UI: %s", catUuidString);
      this.close();
      return;
    }

    if (this.bedPosition == null) {
      this.sendGenericError();
      this.close();
      return;
    }

    CatDataEntry catData = catsManager.getCatData(catUuid, store);
    if (catData == null) {
      this.sendGenericError();
      this.close();
      return;
    }

    UUID playerUuid = this.playerRef.getUuid();
    if (playerUuid == null || !playerUuid.equals(catData.ownerUuid())) {
      this.playerRef.sendMessage(
          Message.translation("cats.ui.bed_spawn.not_owner").color(Constants.COLOR_ERROR));
      this.close();
      return;
    }

    String action = data.command.substring(0, separatorIndex);
    switch (action) {
      case ACTION_RECOVER -> {
        CatCarrierRecoveryConfirmPage confirmPage =
            new CatCarrierRecoveryConfirmPage(
                this.playerRef,
                catData.displayName(),
                null,
                () -> this.handleSpawnCat(catData, store, player));
        player.getPageManager().openCustomPage(ref, store, confirmPage);
        return;
      }

      case ACTION_CALL -> this.handleCallCat(catData, catUuid, catsManager, store);
      case ACTION_SPAWN -> this.handleSpawnCat(catData, store, player);
      case ACTION_WAKEUP -> this.handleWakeUpCat(catUuid, catsManager, store);
      default -> LOGGER.at(Level.WARNING).log("Unknown bed spawn action: %s", action);
    }

    this.close();
  }

  private void handleCallCat(
      @Nonnull CatDataEntry catData,
      @Nonnull UUID catUuid,
      @Nonnull CatsManager catsManager,
      @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> catRef = catsManager.getCatByUuid(catUuid, store);
    if (catRef == null || !catRef.isValid()) {
      this.sendGenericError();
      return;
    }

    Vector3d teleportPosition =
        new Vector3d(this.bedPosition.x, this.bedPosition.y + 0.5, this.bedPosition.z);
    TransformComponent currentTransform =
        store.getComponent(catRef, TransformComponent.getComponentType());
    store.putComponent(
        catRef,
        TransformComponent.getComponentType(),
        new TransformComponent(
            teleportPosition,
            currentTransform != null ? currentTransform.getRotation() : new Rotation3f(0, 0, 0)));

    World world = store.getExternalData().getWorld();
    CatActionHelper.goToBed(catRef, store, world);

    String displayName = catData.displayName();
    this.playerRef.sendMessage(
        Message.translation("cats.ui.bed_spawn.call_success")
            .param("catName", displayName)
            .color(Constants.COLOR_SUCCESS));
    LOGGER.at(Level.INFO).log(
        "Player %s called cat %s to bed at (%s)",
        this.playerRef.getUuid(), displayName, this.bedPosition);
  }

  private void handleWakeUpCat(
      @Nonnull UUID catUuid, @Nonnull CatsManager catsManager, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> catRef = catsManager.getCatByUuid(catUuid, store);
    if (catRef == null || !catRef.isValid()) {
      this.sendGenericError();
      return;
    }

    CatActionHelper.leaveBed(catRef, store);
    this.playerRef.sendMessage(
        Message.translation("cats.ui.bed_spawn.empty_success").color(Constants.COLOR_SUCCESS));
  }

  private void handleEmptyBed(
      @Nonnull Ref<EntityStore> playerEntityRef,
      @Nonnull CatsManager catsManager,
      @Nonnull Store<EntityStore> store) {
    UUID playerUuid = this.playerRef.getUuid();
    if (playerUuid == null) {
      return;
    }

    Collection<CatDataEntry> allCats = catsManager.getCatDataByOwner(playerUuid, store);
    if (allCats == null) {
      return;
    }

    for (CatDataEntry catData : allCats) {
      if (!catData.isSpawned() || catData.uuid() == null) {
        continue;
      }

      Ref<EntityStore> catRef = catsManager.getCatByUuid(catData.uuid(), store);
      if (catRef == null || !catRef.isValid()) {
        continue;
      }

      TransformComponent transform =
          store.getComponent(catRef, TransformComponent.getComponentType());
      if (transform == null) {
        continue;
      }

      Vector3d pos = transform.getPosition();
      double dx = pos.x - this.bedPosition.x;
      double dz = pos.z - this.bedPosition.z;
      if (dx * dx + dz * dz <= BED_EMPTY_RADIUS_SQ) {
        CatActionHelper.leaveBed(catRef, store);
      }
    }
    this.playerRef.sendMessage(
        Message.translation("cats.ui.bed_spawn.empty_success").color(Constants.COLOR_SUCCESS));
  }

  private void handleSpawnCat(
      @Nonnull CatDataEntry catData, @Nonnull Store<EntityStore> store, @Nonnull Player player) {
    CatsManager catsManager = CatsManager.getInstance();
    if (catData.isSpawned()) {
      this.playerRef.sendMessage(
          Message.translation("cats.ui.bed_spawn.already_spawned").color(Constants.COLOR_WARNING));
      return;
    }

    if (catsManager != null && catsManager.isCatAliveInWorld(catData.uuid(), store)) {
      Ref<EntityStore> existingCatRef = catsManager.getCatByUuid(catData.uuid(), store);
      if (existingCatRef != null && existingCatRef.isValid()) {
        LOGGER.at(Level.WARNING).log(
            "State mismatch for cat %s: alive in world but DESPAWNED — auto-resolving",
            catData.uuid());
        catsManager.registerCat(existingCatRef, store);
        this.handleCallCat(catData, catData.uuid(), catsManager, store);
        return;
      }
    }

    if (catData.catType() == CatType.UNKNOWN || catData.catType().getTamedRoleName().isEmpty()) {
      this.sendGenericError();
      return;
    }

    NPCPlugin npcPlugin = NPCPlugin.get();
    if (npcPlugin == null) {
      LOGGER.at(Level.WARNING).log("Cannot spawn cat from bed: NPCPlugin is not available");
      this.sendGenericError();
      return;
    }

    String roleName = catData.catType().getRoleName();
    int roleIndex = npcPlugin.getIndex(roleName);
    if (roleIndex < 0) {
      LOGGER.at(Level.WARNING).log("Cannot spawn cat from bed: Role '%s' not found", roleName);
      this.sendGenericError();
      return;
    }

    Vector3d spawnPosition =
        new Vector3d(this.bedPosition.x, this.bedPosition.y + 0.5, this.bedPosition.z);

    try {
      Pair<Ref<EntityStore>, NPCEntity> spawnResult =
          npcPlugin.spawnEntity(
              store, roleIndex, spawnPosition, new Rotation3f(), null, null, null);

      if (spawnResult == null || spawnResult.left() == null || !spawnResult.left().isValid()) {
        LOGGER.at(Level.WARNING).log("Failed to spawn cat from bed: spawnEntity returned invalid");
        this.playerRef.sendMessage(
            Message.translation("cats.ui.bed_spawn.error").color(Constants.COLOR_ERROR));
        return;
      }

      Ref<EntityStore> catRef = spawnResult.left();
      if (catsManager != null) {
        UUID newEntityUuid = catsManager.getUuid(catRef, store);
        if (newEntityUuid != null && !newEntityUuid.equals(catData.uuid())) {
          catsManager.updateCatUuid(catData.uuid(), newEntityUuid, catRef, store);
        }
      }

      if (catData.ownerUuid() != null) {
        store.putComponent(
            catRef,
            CatOwnerComponent.getComponentType(),
            new CatOwnerComponent(catData.ownerUuid(), catData.ownerName()));
      }

      if (catData.name() != null && !catData.name().isEmpty()) {
        DisplayNameSupport.setDisplayName(catRef, catData.name(), store);
      }

      NPCEntity spawnedNpcEntity = spawnResult.right();
      if (spawnedNpcEntity != null) {
        spawnedNpcEntity.setSpawnConfiguration(Integer.MIN_VALUE);
        spawnedNpcEntity.updateSpawnTrackingState(false);
      }

      World world = store.getExternalData().getWorld();
      if (world != null) {
        CatActionHelper.goToBed(catRef, store, world);
      }

      String displayName = catData.displayName();
      this.playerRef.sendMessage(
          Message.translation(
                  catData.isInCarrier()
                      ? "cats.ui.bed_spawn.recover_success"
                      : "cats.ui.bed_spawn.spawn_success")
              .param("catName", displayName)
              .color(Constants.COLOR_SUCCESS));
      LOGGER.at(Level.INFO).log(
          "Player %s spawned cat %s into bed at (%s)",
          this.playerRef.getUuid(), displayName, this.bedPosition);
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log("Failed to spawn cat from bed");
      this.sendGenericError();
    }
  }

  private void sendGenericError() {
    this.playerRef.sendMessage(
        Message.translation("cats.ui.bed_spawn.error").color(Constants.COLOR_ERROR));
  }

  public static final class BedSpawnEventData {
    public static final BuilderCodec<BedSpawnEventData> CODEC =
        ((BuilderCodec.Builder<BedSpawnEventData>)
                BuilderCodec.builder(BedSpawnEventData.class, BedSpawnEventData::new)
                    .append(
                        new KeyedCodec(KEY_COMMAND, Codec.STRING),
                        (eventData, value) -> eventData.command = value,
                        eventData -> eventData.command)
                    .add())
            .build();

    private String command;
  }
}
