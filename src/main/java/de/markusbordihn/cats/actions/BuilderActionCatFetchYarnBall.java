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

package de.markusbordihn.cats.actions;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatFetchTargetComponent;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.HappinessSource;
import de.markusbordihn.cats.interaction.ItemReturnFeedbackHandler;
import de.markusbordihn.cats.interaction.YarnBallFetchRegistry;
import de.markusbordihn.cats.interaction.YarnBallGroundRegistry;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionCatFetchYarnBall extends BuilderActionBase {

  public static final String BUILDER_ID = "CatFetchYarnBall";
  private static final double BALL_ARRIVAL_THRESHOLD = 2.0;
  private static final double DELIVERY_THRESHOLD = 2.0;
  private static final long FETCH_TIMEOUT_MS = 30_000L;
  private static final long PICKUP_DELAY_MS = 1_000L;
  private static final long FETCH_PATH_INTERVAL_MS = 2_000L;

  public String getBuilderId() {
    return BUILDER_ID;
  }

  @Nonnull
  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  @Override
  public BuilderActionCatFetchYarnBall readConfig(@Nullable JsonElement config) {
    return this;
  }

  @Nonnull
  @Override
  public ActionCatFetchYarnBall build(BuilderSupport support) {
    return new ActionCatFetchYarnBall(this);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Drives yarn ball fetch: cat walks to ball then returns it to the owner";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Phase 0: cat walks to the thrown ball's landing position. "
        + "Phase 1: cat walks back to the owner and delivers the yarn ball.";
  }

  public static class ActionCatFetchYarnBall extends ActionBase {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ActionCatFetchYarnBall(@Nonnull BuilderActionBase builder) {
      super(builder);
    }

    private static void setTransientPath(@Nonnull NPCEntity npcEntity, @Nonnull Vector3d target) {
      npcEntity.getPathManager().setTransientPath(null);
      TransientPath path = new TransientPath();
      path.addWaypoint(target, new Vector3f(0, 0, 0));
      npcEntity.getPathManager().setTransientPath(path);
    }

    private static void clearTransientPath(@Nonnull NPCEntity npcEntity) {
      npcEntity.getPathManager().setTransientPath(null);
    }

    @Nullable
    private static Vector3d getPlayerPosition(
        @Nullable UUID ownerUuid, @Nonnull Store<EntityStore> store) {
      if (ownerUuid == null) {
        return null;
      }

      Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(ownerUuid);
      if (playerRef == null || !playerRef.isValid()) {
        return null;
      }

      TransformComponent transform =
          store.getComponent(playerRef, TransformComponent.getComponentType());
      return transform != null ? transform.getPosition() : null;
    }

    @Nullable
    private static Player getPlayer(@Nullable UUID ownerUuid, @Nonnull Store<EntityStore> store) {
      if (ownerUuid == null) {
        return null;
      }

      Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(ownerUuid);
      if (playerRef == null || !playerRef.isValid()) {
        return null;
      }

      return store.getComponent(playerRef, Player.getComponentType());
    }

    private static double distance(@Nonnull Vector3d a, @Nonnull Vector3d b) {
      double dx = b.x - a.x;
      double dy = b.y - a.y;
      double dz = b.z - a.z;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean canExecute(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        @Nonnull InfoProvider infoProvider,
        double deltaTime,
        @Nonnull Store<EntityStore> store) {
      CatFetchTargetComponent fetchTarget =
          store.getComponent(entityRef, CatFetchTargetComponent.getComponentType());
      return fetchTarget != null && fetchTarget.hasTarget();
    }

    @Override
    public boolean execute(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        @Nonnull InfoProvider infoProvider,
        double deltaTime,
        @Nonnull Store<EntityStore> store) {

      CatFetchTargetComponent fetchTarget =
          store.getComponent(entityRef, CatFetchTargetComponent.getComponentType());
      if (fetchTarget == null || !fetchTarget.hasTarget()) {
        returnToDefaultState(entityRef, role, store);
        return false;
      }

      TransformComponent catTransform =
          store.getComponent(entityRef, TransformComponent.getComponentType());
      if (catTransform == null) {
        cleanUp(entityRef, fetchTarget.getOwnerUuid(), role, store, null);
        return false;
      }

      long now = System.currentTimeMillis();
      if (now - fetchTarget.getFetchStartMs() > FETCH_TIMEOUT_MS) {
        LOGGER.at(Level.WARNING).log("FetchYarnBall: timeout after %d ms", FETCH_TIMEOUT_MS);
        Player player = getPlayer(fetchTarget.getOwnerUuid(), store);
        cleanUp(entityRef, fetchTarget.getOwnerUuid(), role, store, player);
        return false;
      }

      if (!fetchTarget.isReturningToPlayer()) {
        NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
        if (npcEntity == null) {
          cleanUp(entityRef, fetchTarget.getOwnerUuid(), role, store, null);
          return false;
        }

        handleGoToBallPhase(entityRef, fetchTarget, catTransform, npcEntity, role, store);
      } else {
        handleReturnToPlayerPhase(entityRef, fetchTarget, catTransform, role, store);
      }

      return false;
    }

    private void handleGoToBallPhase(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull CatFetchTargetComponent fetchTarget,
        @Nonnull TransformComponent catTransform,
        @Nonnull NPCEntity npcEntity,
        @Nonnull Role role,
        @Nonnull Store<EntityStore> store) {

      Vector3d ballPos = fetchTarget.getBallPosition();
      if (distance(catTransform.getPosition(), ballPos) >= BALL_ARRIVAL_THRESHOLD) {
        long now = System.currentTimeMillis();
        long lastPath = fetchTarget.getLastPathMs();
        if (lastPath <= 0 || now - lastPath >= FETCH_PATH_INTERVAL_MS) {
          setTransientPath(npcEntity, ballPos);
          CatFetchTargetComponent updated = fetchTarget.clone();
          updated.setLastPathMs(now);
          store.putComponent(entityRef, CatFetchTargetComponent.getComponentType(), updated);
        }
        return;
      }

      long pickupTime = fetchTarget.getPickupTimeMs();
      if (pickupTime <= 0) {
        LOGGER.at(Level.INFO).log("FetchYarnBall: arrived at ball, starting pickup delay");
        clearTransientPath(npcEntity);
        CatFetchTargetComponent updated = fetchTarget.clone();
        updated.setPickupTimeMs(System.currentTimeMillis());
        store.putComponent(entityRef, CatFetchTargetComponent.getComponentType(), updated);
        return;
      }
      if (System.currentTimeMillis() - pickupTime < PICKUP_DELAY_MS) {
        return;
      }

      LOGGER.at(Level.INFO).log("FetchYarnBall: pickup complete, transitioning to return phase");
      clearTransientPath(npcEntity);

      CatsManager pickupManager = CatsManager.getInstance();
      if (pickupManager != null) {
        pickupManager.boostHappiness(entityRef, HappinessSource.FETCHING, store);
        pickupManager.satisfyNeed(entityRef, CatNeedType.PLAY, 15f, store);
      }

      UUID ownerUuid = fetchTarget.getOwnerUuid();
      Vector3d playerPos = getPlayerPosition(ownerUuid, store);
      if (playerPos == null) {
        LOGGER.at(Level.WARNING).log(
            "FetchYarnBall: owner %s not found after pickup, dropping ball", ownerUuid);
        YarnBallGroundRegistry.register(ownerUuid, catTransform.getPosition());
        cleanUp(entityRef, ownerUuid, role, store, null);
        return;
      }

      store.putComponent(
          entityRef, CatFetchTargetComponent.getComponentType(), fetchTarget.asReturning());
      role.getStateSupport().setState(entityRef, "ReturningBallToOwner", "Default", store);

      LOGGER.at(Level.INFO).log(
          "FetchYarnBall: returning to owner %s at %.1f/%.1f/%.1f (Seek will handle movement)",
          ownerUuid, playerPos.x, playerPos.y, playerPos.z);
    }

    private void handleReturnToPlayerPhase(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull CatFetchTargetComponent fetchTarget,
        @Nonnull TransformComponent catTransform,
        @Nonnull Role role,
        @Nonnull Store<EntityStore> store) {

      UUID ownerUuid = fetchTarget.getOwnerUuid();
      Vector3d playerPos = getPlayerPosition(ownerUuid, store);

      if (playerPos == null) {
        LOGGER.at(Level.WARNING).log(
            "FetchYarnBall: owner %s offline during return, dropping ball", ownerUuid);
        YarnBallGroundRegistry.register(ownerUuid, catTransform.getPosition());
        cleanUp(entityRef, ownerUuid, role, store, null);
        return;
      }

      double dist = distance(catTransform.getPosition(), playerPos);
      if (dist < DELIVERY_THRESHOLD) {
        LOGGER.at(Level.INFO).log("FetchYarnBall: delivered ball to owner %s", ownerUuid);
        CatsManager deliveryManager = CatsManager.getInstance();
        if (deliveryManager != null) {
          deliveryManager.boostHappiness(entityRef, HappinessSource.FETCHING, store);
          deliveryManager.satisfyNeed(entityRef, CatNeedType.PLAY, 10f, store);
          deliveryManager.satisfyNeed(entityRef, CatNeedType.SOCIAL, 15f, store);
        }
        cleanUp(entityRef, ownerUuid, role, store, getPlayer(ownerUuid, store));
        return;
      }

      LOGGER.at(Level.FINE).log(
          "FetchYarnBall: returning to owner, dist=%.1f (Seek handling movement)", dist);
    }

    private void cleanUp(
        @Nonnull Ref<EntityStore> entityRef,
        @Nullable UUID ownerUuid,
        @Nonnull Role role,
        @Nonnull Store<EntityStore> store,
        @Nullable Player player) {

      if (player != null) {
        CatsManager catsManager = CatsManager.getInstance();
        ItemReturnFeedbackHandler.deliver(
            player,
            Constants.CAT_YARN_BALL_ITEM,
            "cats.interactions.yarn_ball.returned",
            catsManager != null ? catsManager.getCatDisplayName(entityRef, store) : null,
            Constants.COLOR_PINK);
      } else if (ownerUuid != null) {
        LOGGER.at(Level.WARNING).log(
            "FetchYarnBall: player offline, ball lost for owner %s", ownerUuid);
      }

      if (ownerUuid != null) {
        YarnBallFetchRegistry.complete(ownerUuid);
      }
      store.removeComponent(entityRef, CatFetchTargetComponent.getComponentType());
      resetState(entityRef, role, store);
    }

    private void returnToDefaultState(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        @Nonnull Store<EntityStore> store) {
      CatFetchTargetComponent fetchTarget =
          store.getComponent(entityRef, CatFetchTargetComponent.getComponentType());
      if (fetchTarget != null) {
        UUID ownerUuid = fetchTarget.getOwnerUuid();
        if (ownerUuid != null) {
          YarnBallFetchRegistry.complete(ownerUuid);
        }
        store.removeComponent(entityRef, CatFetchTargetComponent.getComponentType());
      }
      resetState(entityRef, role, store);
    }

    private void resetState(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        @Nonnull Store<EntityStore> store) {
      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager != null) {
        catsManager.updateCatState(entityRef, CatState.FOLLOWING, store);
      }
      role.getStateSupport().setState(entityRef, "Pet", "Default", store);
    }
  }
}
