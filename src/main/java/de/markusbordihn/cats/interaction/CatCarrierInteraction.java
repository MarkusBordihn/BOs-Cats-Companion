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

import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.codec.codecs.UUIDBinaryCodec;
import com.hypixel.hytale.codec.codecs.simple.StringCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.metadata.CapturedNPCMetadata;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.hypixel.hytale.server.npc.storage.AlarmStore;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatType;
import de.markusbordihn.cats.manager.CatsManager;
import de.markusbordihn.cats.player.PlayerFeedback;
import de.markusbordihn.cats.ui.CatActionWheelPage;
import it.unimi.dsi.fastutil.Pair;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class CatCarrierInteraction {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final UUIDBinaryCodec UUID_CODEC = new UUIDBinaryCodec();
  private static final StringCodec STRING_CODEC = new StringCodec();

  private static final String META_CAT_UUID = "CatUUID";
  private static final String META_CAT_NAME = "CatName";
  private static final String FULL_CARRIER_ICON = "Icons/ItemsGenerated/Cat_Carrier_Full.png";

  private CatCarrierInteraction() {}

  public static boolean handleCapture(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull Player player,
      @Nonnull ItemStack heldItem) {

    Ref<EntityStore> playerEntityRef = player.getReference();
    PlayerRef playerRef =
        playerEntityRef != null
            ? store.getComponent(playerEntityRef, PlayerRef.getComponentType())
            : null;

    UUID storedCatUuid = heldItem.getFromMetadataOrNull(META_CAT_UUID, UUID_CODEC);
    if (storedCatUuid != null) {
      String storedName = heldItem.getFromMetadataOrNull(META_CAT_NAME, STRING_CODEC);
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.already_occupied")
              .param("catName", storedName != null ? storedName : "a cat")
              .param("catUuid", storedCatUuid.toString())
              .color(Constants.COLOR_WARNING));
      return true;
    }

    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());
    if (ownerComponent == null || !ownerComponent.hasOwner()) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.not_tamed")
              .color(Constants.COLOR_WARNING));
      return true;
    }

    UUID playerUuid = playerRef != null ? playerRef.getUuid() : null;
    if (playerUuid == null || !playerUuid.equals(ownerComponent.getOwnerUUID())) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.not_owner").color(Constants.COLOR_ERROR));
      return true;
    }

    CatActionWheelPage.closeIfOpen(player);
    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      return true;
    }

    UUID catUuid = catsManager.getUuid(entityRef, store);
    if (catUuid == null) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      return true;
    }

    Nameplate nameplate = store.getComponent(entityRef, Nameplate.getComponentType());
    String catName = nameplate != null ? nameplate.getText() : null;
    String displayName = catName != null && !catName.isEmpty() ? catName : "Cat";

    ItemStack updatedItem = heldItem.withMetadata(META_CAT_UUID, UUID_CODEC, catUuid);
    if (catName != null && !catName.isEmpty()) {
      updatedItem = updatedItem.withMetadata(META_CAT_NAME, STRING_CODEC, catName);
    }

    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());

    try {
      CapturedNPCMetadata capturedMeta =
          FarmingUtil.generateCapturedNPCMetadata(store, entityRef, displayName);
      if (capturedMeta == null) {
        capturedMeta = new CapturedNPCMetadata();
        capturedMeta.setNpcNameKey(displayName);
      }
      capturedMeta.setFullItemIcon(FULL_CARRIER_ICON);
      AlarmStore alarmStore = AlarmStore.get(entityRef, store);
      if (alarmStore != null) {
        capturedMeta.setAlarmStore(alarmStore);
      }

      updatedItem = updatedItem.withMetadata(CapturedNPCMetadata.KEYED_CODEC, capturedMeta);
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log(
          "Failed to set CapturedNPCMetadata on carrier, capture will proceed without icon swap");
    }

    catsManager.storeCatInCarrier(entityRef, store);

    if (npcEntity != null) {
      npcEntity.setDespawning(true);
      npcEntity.setDespawnRemainingSeconds(0.0f);
      npcEntity.updateSpawnTrackingState(false);
    }

    updateHeldItem(store, playerEntityRef, heldItem, updatedItem);

    PlayerFeedback.sendMessage(
        playerRef,
        Message.translation("cats.interactions.carrier.captured")
            .param("catName", displayName)
            .param("catUuid", catUuid.toString())
            .color(Constants.COLOR_SUCCESS));

    LOGGER.at(Level.INFO).log(
        "Player %s captured cat %s (UUID: %s) into carrier", playerUuid, displayName, catUuid);

    return true;
  }

  public static boolean handleRelease(
      @Nonnull Store<EntityStore> store, @Nonnull Player player, @Nonnull ItemStack heldItem) {
    return handleRelease(store, player, heldItem, null);
  }

  public static boolean handleRelease(
      @Nonnull Store<EntityStore> store,
      @Nonnull Player player,
      @Nonnull ItemStack heldItem,
      @Nullable Vector3d targetPos) {

    Ref<EntityStore> playerEntityRef = player.getReference();
    PlayerRef playerRef =
        playerEntityRef != null
            ? store.getComponent(playerEntityRef, PlayerRef.getComponentType())
            : null;

    UUID storedCatUuid = heldItem.getFromMetadataOrNull(META_CAT_UUID, UUID_CODEC);
    if (storedCatUuid == null) {
      LOGGER.at(Level.FINE).log("handleRelease: carrier is empty (no CatUUID metadata)");
      return false;
    }

    String storedName = heldItem.getFromMetadataOrNull(META_CAT_NAME, STRING_CODEC);
    LOGGER.at(Level.INFO).log(
        "handleRelease: releasing cat %s (UUID: %s)", storedName, storedCatUuid);

    UUID playerUuid = playerRef != null ? playerRef.getUuid() : null;
    if (playerUuid == null) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      return true;
    }

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      return true;
    }

    CatDataEntry catData = catsManager.getCatData(storedCatUuid, store);
    if (catData == null) {
      LOGGER.at(Level.WARNING).log("Cannot release cat: no data found for UUID %s", storedCatUuid);
      CatDataEntry respawnedCat =
          catsManager.getCatDataByOwner(playerUuid, store).stream()
              .filter(
                  entry ->
                      entry.isSpawned() && storedName != null && storedName.equals(entry.name()))
              .findFirst()
              .orElse(null);
      if (respawnedCat != null) {
        updateHeldItem(store, playerEntityRef, heldItem, heldItem.withMetadata(null));
        PlayerFeedback.sendMessage(
            playerRef,
            Message.translation("cats.interactions.carrier.cat_already_in_world")
                .param("catName", storedName)
                .color(Constants.COLOR_WARNING));
        LOGGER.at(Level.INFO).log(
            "Carrier stale-UUID cleared for player %s: cat '%s' was already re-spawned with new UUID",
            playerUuid, storedName);
      } else {
        PlayerFeedback.sendMessage(
            playerRef,
            Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      }
      return true;
    }

    if (catData.ownerUuid() != null && !playerUuid.equals(catData.ownerUuid())) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.not_owner").color(Constants.COLOR_ERROR));
      return true;
    }

    if (catData.isInCarrier()) {
      Ref<EntityStore> existingCatRef = catsManager.getCatByUuid(storedCatUuid, store);
      if (existingCatRef != null && existingCatRef.isValid()) {
        PlayerFeedback.sendMessage(
            playerRef,
            Message.translation("cats.interactions.carrier.busy").color(Constants.COLOR_WARNING));
        LOGGER.at(Level.WARNING).log(
            "Release requested for cat %s while the previous entity ref is still valid; retry later",
            storedCatUuid);
        return true;
      }
    }

    Vector3d spawnPos = resolveSpawnPosition(store, playerEntityRef, targetPos);
    if (spawnPos == null) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.error").color(Constants.COLOR_ERROR));
      return true;
    }

    CapturedNPCMetadata capturedMeta =
        heldItem.getFromMetadataOrNull(CapturedNPCMetadata.KEY, CapturedNPCMetadata.CODEC);
    AlarmStore alarmStore = capturedMeta != null ? capturedMeta.getAlarmStore() : null;

    if (!spawnCatFromData(catData, spawnPos, store, alarmStore)) {
      PlayerFeedback.sendMessage(
          playerRef,
          Message.translation("cats.interactions.carrier.release_failed")
              .color(Constants.COLOR_ERROR));
      return true;
    }

    updateHeldItem(store, playerEntityRef, heldItem, heldItem.withMetadata(null));

    String displayName = storedName != null && !storedName.isEmpty() ? storedName : "Cat";
    PlayerFeedback.sendMessage(
        playerRef,
        Message.translation("cats.interactions.carrier.released")
            .param("catName", displayName)
            .color(Constants.COLOR_SUCCESS));

    LOGGER.at(Level.INFO).log(
        "Player %s released cat %s from carrier at (%s)", playerUuid, displayName, spawnPos);

    return true;
  }

  public static boolean hasStoredCat(@Nullable ItemStack heldItem) {
    if (heldItem == null) {
      return false;
    }

    return heldItem.getFromMetadataOrNull(META_CAT_UUID, UUID_CODEC) != null;
  }

  @Nullable
  private static Vector3d resolveSpawnPosition(
      @Nonnull Store<EntityStore> store,
      @Nullable Ref<EntityStore> playerEntityRef,
      @Nullable Vector3d targetPos) {
    if (targetPos != null) {
      return new Vector3d(targetPos.x + 0.5, targetPos.y + 1.0, targetPos.z + 0.5);
    }

    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return null;
    }

    TransformComponent playerTransform =
        store.getComponent(playerEntityRef, TransformComponent.getComponentType());
    if (playerTransform == null) {
      return null;
    }

    return new Vector3d(playerTransform.getPosition())
        .add(Math.random() * 4 - 2, 0, Math.random() * 4 - 2);
  }

  private static boolean spawnCatFromData(
      @Nonnull CatDataEntry catData,
      @Nonnull Vector3d position,
      @Nonnull Store<EntityStore> store,
      @Nullable AlarmStore alarmStore) {

    if (catData.catType() == CatType.UNKNOWN || catData.catType().getTamedRoleName().isEmpty()) {
      return false;
    }

    try {
      NPCPlugin npcPlugin = NPCPlugin.get();
      if (npcPlugin == null) {
        LOGGER.at(Level.WARNING).log("Cannot spawn cat from carrier: NPCPlugin is not available");
        return false;
      }

      String roleName = catData.catType().getRoleName();
      int roleIndex = npcPlugin.getIndex(roleName);
      if (roleIndex < 0) {
        LOGGER.at(Level.WARNING).log(
            "Cannot spawn cat from carrier: Role '%s' not found", roleName);
        return false;
      }

      Rotation3f rotation = new Rotation3f();
      Pair<Ref<EntityStore>, NPCEntity> spawnResult =
          npcPlugin.spawnEntity(
              store,
              roleIndex,
              position,
              rotation,
              null,
              alarmStore == null
                  ? null
                  : (spawnedNpc, holder, spawnStore) ->
                      holder.putComponent(AlarmStore.getComponentType(), alarmStore),
              null);

      if (spawnResult == null || spawnResult.left() == null || !spawnResult.left().isValid()) {
        LOGGER.at(Level.WARNING).log(
            "Failed to spawn cat from carrier: spawnEntity returned null or invalid");
        return false;
      }

      Ref<EntityStore> catRef = spawnResult.left();
      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager == null) {
        LOGGER.at(Level.WARNING).log("Cannot restore cat data after spawn: CatsManager is null");
        return true;
      }

      UUID newEntityUuid = catsManager.getUuid(catRef, store);
      if (newEntityUuid != null && !newEntityUuid.equals(catData.uuid())) {
        catsManager.updateCatUuid(catData.uuid(), newEntityUuid, catRef, store);
      }

      if (catData.ownerUuid() != null) {
        CatOwnerComponent ownerComponent =
            new CatOwnerComponent(catData.ownerUuid(), catData.ownerName());
        store.putComponent(catRef, CatOwnerComponent.getComponentType(), ownerComponent);
      }

      if (catData.name() != null && !catData.name().isEmpty()) {
        DisplayNameSupport.setDisplayName(catRef, catData.name(), store);
      }

      if (catData.state() != null) {
        CatStateComponent stateComponent = new CatStateComponent(catData.state());
        store.putComponent(catRef, CatStateComponent.getComponentType(), stateComponent);
      }

      // Disable spawn tracking to prevent the engine's despawn system.
      NPCEntity spawnedNpcEntity = spawnResult.right();
      if (spawnedNpcEntity != null) {
        spawnedNpcEntity.setSpawnConfiguration(Integer.MIN_VALUE);
        spawnedNpcEntity.updateSpawnTrackingState(false);
      }

      return true;
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log("Failed to spawn cat from carrier");
      return false;
    }
  }

  private static void updateHeldItem(
      @Nonnull Store<EntityStore> store,
      @Nullable Ref<EntityStore> playerEntityRef,
      @Nonnull ItemStack oldItem,
      @Nonnull ItemStack newItem) {
    InventoryComponent.Hotbar hotbar =
        playerEntityRef != null
            ? store.getComponent(playerEntityRef, InventoryComponent.Hotbar.getComponentType())
            : null;
    if (hotbar == null) {
      LOGGER.at(Level.WARNING).log("Cannot update held item: player hotbar is unavailable");
      return;
    }

    short activeSlot = hotbar.getActiveSlot();
    try {
      hotbar.getInventory().replaceItemStackInSlot(activeSlot, oldItem, newItem);
      LOGGER.at(Level.INFO).log("Updated carrier in hotbar slot %d", activeSlot);
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log(
          "replaceItemStackInSlot failed, falling back to remove+add");
      hotbar.getInventory().removeItemStackFromSlot(activeSlot, 1);
      hotbar.getInventory().addItemStackToSlot(activeSlot, newItem);
    }
  }
}
