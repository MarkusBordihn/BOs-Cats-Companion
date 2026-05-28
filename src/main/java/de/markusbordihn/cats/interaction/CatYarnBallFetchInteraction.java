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

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatFetchTargetComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.component.CatYarnBallProjectileComponent;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.inventory.InventoryHelper;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class CatYarnBallFetchInteraction extends SimpleInstantInteraction {

  public static final String ID = "Cat_Yarn_Ball_Fetch_From_Projectile";

  @Nonnull
  public static final BuilderCodec<CatYarnBallFetchInteraction> CODEC =
      BuilderCodec.builder(
              CatYarnBallFetchInteraction.class,
              CatYarnBallFetchInteraction::new,
              SimpleInstantInteraction.CODEC)
          .documentation("")
          .build();

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final double FETCH_RANGE = 30.0;
  private static final float FETCH_PROBABILITY_SCALE = 1.5f;

  public CatYarnBallFetchInteraction() {}

  @Nullable
  static Player resolveThrowingPlayer(
      @Nonnull InteractionContext context,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> ownerRef = context.getOwningEntity();
    if (ownerRef != null && ownerRef.isValid()) {
      Player player = commandBuffer.getComponent(ownerRef, Player.getComponentType());
      if (player != null) {
        return player;
      }
    }

    UUID recentUuid = YarnBallThrowRegistry.getRecentThrower(10_000);
    if (recentUuid == null) {
      LOGGER.at(Level.WARNING).log(
          "CatYarnBallFetchInteraction: no owner ref and no recent throw registered");
      return null;
    }

    Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(recentUuid);
    if (playerRef == null || !playerRef.isValid()) {
      LOGGER.at(Level.WARNING).log(
          "CatYarnBallFetchInteraction: could not resolve player ref for UUID %s", recentUuid);
      return null;
    }

    return commandBuffer.getComponent(playerRef, Player.getComponentType());
  }

  @Nullable
  private static Player getPlayer(@Nonnull UUID ownerUuid, @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(ownerUuid);
    if (playerRef == null || !playerRef.isValid()) {
      return null;
    }

    return store.getComponent(playerRef, Player.getComponentType());
  }

  private static boolean startFetch(
      @Nonnull Ref<EntityStore> catRef,
      @Nonnull Vector3d landingPosition,
      @Nonnull UUID ownerUuid,
      @Nullable Ref<EntityStore> projectileRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Store<EntityStore> store) {

    ComponentType<EntityStore, CatFetchTargetComponent> fetchTargetType =
        CatFetchTargetComponent.getComponentType();
    ComponentType<EntityStore, CatStateComponent> catStateType =
        CatStateComponent.getComponentType();
    if (fetchTargetType == null || catStateType == null) {
      LOGGER.at(Level.WARNING).log("startFetch: component types not yet registered, skipping");
      return false;
    }

    NPCEntity npcEntity = store.getComponent(catRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      LOGGER.at(Level.WARNING).log("startFetch: cat has no NPCEntity or Role");
      return false;
    }

    if (YarnBallFetchRegistry.isFetching(ownerUuid)) {
      return false;
    }

    CatStateComponent currentState = store.getComponent(catRef, catStateType);
    CatState previousState = currentState != null ? currentState.getState() : CatState.FOLLOWING;

    CatFetchTargetComponent fetchTarget =
        new CatFetchTargetComponent(landingPosition, ownerUuid, projectileRef);
    fetchTarget.setPreviousState(previousState);

    YarnBallFetchRegistry.register(ownerUuid, catRef);
    commandBuffer.putComponent(catRef, fetchTargetType, fetchTarget);
    commandBuffer.putComponent(catRef, catStateType, new CatStateComponent(CatState.FETCHING));
    npcEntity.getRole().getStateSupport().setState(catRef, "FetchingYarnBall", "Default", store);
    TransientPath path = new TransientPath();
    path.addWaypoint(landingPosition, new Rotation3f(0, 0, 0));
    npcEntity.getPathManager().setTransientPath(null);
    npcEntity.getPathManager().setTransientPath(path);
    return true;
  }

  public static boolean handleLanding(
      @Nonnull UUID ownerUuid,
      @Nonnull Ref<EntityStore> projectileRef,
      @Nonnull Vector3d landingPosition,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Store<EntityStore> store) {
    YarnBallThrowRegistry.complete(ownerUuid, projectileRef);

    Player player = getPlayer(ownerUuid, store);
    if (player == null) {
      LOGGER.at(Level.WARNING).log(
          "handleLanding: could not resolve throwing player %s", ownerUuid);
      return false;
    }

    ComponentType<EntityStore, CatYarnBallProjectileComponent> projectileType =
        CatYarnBallProjectileComponent.getComponentType();
    if (projectileType != null) {
      CatYarnBallProjectileComponent projectileComponent =
          commandBuffer.getComponent(projectileRef, projectileType);
      if (projectileComponent != null) {
        CatYarnBallProjectileComponent updatedProjectile = projectileComponent.clone();
        updatedProjectile.setFetchTriggered(true);
        commandBuffer.putComponent(projectileRef, projectileType, updatedProjectile);
      }
    }

    Ref<EntityStore> playerEntityRef = store.getExternalData().getRefFromUUID(ownerUuid);
    TransformComponent playerTransform =
        playerEntityRef != null
            ? store.getComponent(playerEntityRef, TransformComponent.getComponentType())
            : null;
    Vector3d playerPos = playerTransform != null ? playerTransform.getPosition() : landingPosition;

    Ref<EntityStore> catRef = findNearestFetchCat(ownerUuid, playerPos, store);
    LOGGER.at(Level.INFO).log(
        "handleLanding: owner=%s, catFound=%s, playerPos=%.1f/%.1f/%.1f",
        ownerUuid, catRef != null ? "yes" : "no", playerPos.x, playerPos.y, playerPos.z);
    if (catRef == null) {
      InventoryHelper.giveItem(player, Constants.CAT_YARN_BALL_ITEM);
      YarnBallGroundRegistry.register(ownerUuid, landingPosition);
      if (projectileRef.isValid()) {
        commandBuffer.removeEntity(projectileRef, RemoveReason.REMOVE);
      }
      player
          .getPlayerRef()
          .sendMessage(
              Message.translation("cats.interactions.yarn_ball.no_cat_nearby")
                  .color(Constants.COLOR_SOFT_ORANGE));
      return false;
    }

    if (!startFetch(catRef, landingPosition, ownerUuid, projectileRef, commandBuffer, store)) {
      InventoryHelper.giveItem(player, Constants.CAT_YARN_BALL_ITEM);
      if (projectileRef.isValid()) {
        commandBuffer.removeEntity(projectileRef, RemoveReason.REMOVE);
      }
      return false;
    }

    YarnBallGroundRegistry.clear(ownerUuid);

    String catName = getCatName(catRef, store);
    Message thrownMessage =
        Message.translation("cats.interactions.yarn_ball.thrown").color(Constants.COLOR_PINK);
    if (catName != null) {
      thrownMessage = thrownMessage.param("catName", catName);
    }
    player.getPlayerRef().sendMessage(thrownMessage);
    return true;
  }

  @Nullable
  private static Ref<EntityStore> findNearestFetchCat(
      @Nonnull UUID ownerUuid, @Nonnull Vector3d playerPos, @Nonnull Store<EntityStore> store) {

    CatsManager catsManager = CatsManager.getInstance();
    if (catsManager == null) {
      LOGGER.at(Level.WARNING).log("findNearestFetchCat: CatsManager not available");
      return null;
    }

    Set<Ref<EntityStore>> ownedCats = catsManager.getCatsByOwner(ownerUuid, store);
    Ref<EntityStore> bestCat = null;
    double bestDist = Double.MAX_VALUE;

    for (Ref<EntityStore> catRef : ownedCats) {
      if (isBusy(catRef, store)) {
        continue;
      }

      TransformComponent catTransform =
          store.getComponent(catRef, TransformComponent.getComponentType());
      if (catTransform == null) {
        continue;
      }

      double distToPlayer = distance(catTransform.getPosition(), playerPos);
      if (distToPlayer > FETCH_RANGE) {
        continue;
      }

      CatDataEntry catData = catsManager.getCatData(catRef, store);
      float playModifier =
          (catData != null && catData.personalityType() != null)
              ? catData.personalityType().getPlayModifier()
              : 1.0f;

      float fetchProbability = Math.min(playModifier / FETCH_PROBABILITY_SCALE, 1.0f);
      if (ThreadLocalRandom.current().nextFloat() >= fetchProbability) {
        continue;
      }

      if (distToPlayer < bestDist) {
        bestCat = catRef;
        bestDist = distToPlayer;
      }
    }

    return bestCat;
  }

  private static boolean isBusy(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    CatStateComponent stateComponent =
        store.getComponent(catRef, CatStateComponent.getComponentType());
    if (stateComponent == null) {
      return false;
    }

    CatState state = stateComponent.getState();
    if (state == CatState.FETCHING || state == CatState.ATTACKING || state.isSleepingState()) {
      return true;
    }

    NPCEntity npcEntity = store.getComponent(catRef, NPCEntity.getComponentType());
    if (npcEntity != null && npcEntity.getRole() != null) {
      StateSupport stateSupport = npcEntity.getRole().getStateSupport();
      return stateSupport.inState("Pet", "PrepareSleep")
          || stateSupport.inState("Pet", "Sleeping")
          || stateSupport.inState("Pet", "GoingToBed");
    }

    return false;
  }

  @Nullable
  private static String getCatName(
      @Nonnull Ref<EntityStore> catRef, @Nonnull Store<EntityStore> store) {
    Nameplate nameplate = store.getComponent(catRef, Nameplate.getComponentType());
    if (nameplate != null && nameplate.getText() != null && !nameplate.getText().isEmpty()) {
      return nameplate.getText();
    }

    return null;
  }

  private static double distance(@Nonnull Vector3d a, @Nonnull Vector3d b) {
    double dx = b.x - a.x;
    double dy = b.y - a.y;
    double dz = b.z - a.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  protected void firstRun(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler) {

    CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
    Ref<EntityStore> projectileRef = context.getEntity();
    if (commandBuffer == null || projectileRef == null || !projectileRef.isValid()) {
      return;
    }

    TransformComponent landingTransform =
        commandBuffer.getComponent(projectileRef, TransformComponent.getComponentType());
    if (landingTransform == null) {
      return;
    }

    Vector3d landingPosition = landingTransform.getPosition();
    Store<EntityStore> store = commandBuffer.getStore();

    LOGGER.at(Level.INFO).log(
        "firstRun: projectile interaction triggered, landing=%.1f/%.1f/%.1f, ownerRef=%s",
        landingPosition.x,
        landingPosition.y,
        landingPosition.z,
        context.getOwningEntity() != null ? "present" : "null");

    Player player = resolveThrowingPlayer(context, commandBuffer, store);
    if (player == null) {
      LOGGER.at(Level.WARNING).log("firstRun: could not resolve throwing player");
      return;
    }

    UUID ownerUuid = player.getUuid();
    if (ownerUuid == null) {
      return;
    }

    handleLanding(ownerUuid, projectileRef, landingPosition, commandBuffer, store);
  }
}
