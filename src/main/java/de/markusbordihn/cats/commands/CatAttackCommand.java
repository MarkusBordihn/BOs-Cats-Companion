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

package de.markusbordihn.cats.commands;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.Constants;
import de.markusbordihn.cats.component.CatOwnerComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

final class CatAttackCommand extends CatCommand {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final double ATTACK_RANGE = 15.0;

  @Nonnull private final EntityWrappedArg targetArg;

  public CatAttackCommand() {
    super("attack", "Commands your nearby cats to attack a target");
    this.targetArg = this.withOptionalArg("target", "The entity to attack", ArgTypes.ENTITY_ID);
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    var targetRefOpt = getEntityFromArgument(this.targetArg, store, context);
    if (targetRefOpt.isEmpty()) {
      context.sendMessage(
          Message.translation("cats.commands.attack.no_target").color(Constants.COLOR_ERROR));
      return;
    }

    Ref<EntityStore> targetRef = targetRefOpt.get();
    String executingPlayer = getExecutingPlayerName(context);
    if (executingPlayer == null) {
      context.sendMessage(
          Message.translation("cats.commands.error.player_only").color(Constants.COLOR_ERROR));
      return;
    }

    UUID playerUuid = context.sender().getUuid();
    if (playerUuid == null) {
      context.sendMessage(
          Message.translation("cats.commands.error.player_only").color(Constants.COLOR_ERROR));
      return;
    }

    TransformComponent targetTransform =
        store.getComponent(targetRef, TransformComponent.getComponentType());
    if (targetTransform == null) {
      context.sendMessage(
          Message.translation("cats.commands.attack.invalid_target").color(Constants.COLOR_ERROR));
      return;
    }

    Vector3d targetPos = targetTransform.getPosition();
    List<Ref<EntityStore>> nearbyCats = findNearbyCats(store, targetPos, playerUuid);
    if (nearbyCats.isEmpty()) {
      context.sendMessage(
          Message.translation("cats.commands.attack.no_cats_nearby")
              .color(Constants.COLOR_WARNING));
      return;
    }

    Ref<EntityStore> selectedCat = selectBestCat(store, nearbyCats, targetPos);
    if (selectedCat == null) {
      context.sendMessage(
          Message.translation("cats.commands.attack.no_available_cats")
              .color(Constants.COLOR_WARNING));
      return;
    }

    // Get target name
    String targetName = getEntityDisplayName(targetRef, store);

    // Update state (component + persistent data)
    CatsManager.getInstance().updateCatState(selectedCat, CatState.ATTACKING, store);

    NPCEntity npcEntity = store.getComponent(selectedCat, NPCEntity.getComponentType());
    if (npcEntity != null && npcEntity.getRole() != null) {
      npcEntity.getRole().getStateSupport().setState(selectedCat, "Pet", "Attacking", store);
      npcEntity.getRole().getMarkedEntitySupport().setMarkedEntity("LockedTarget", targetRef);

      // Set path to target for initial movement
      TransientPath path = new TransientPath();
      path.addWaypoint(new Vector3d(targetPos.x, targetPos.y, targetPos.z), new Vector3f(0, 0, 0));
      npcEntity.getPathManager().setTransientPath(path);
    }

    String catName = getCatDisplayName(selectedCat, store);
    context.sendMessage(
        Message.translation("cats.commands.attack.success")
            .param("name", catName)
            .param("target", targetName)
            .color(Constants.COLOR_SUCCESS));

    LOGGER.at(Level.INFO).log(
        "Player %s commanded cat %s to attack %s", executingPlayer, catName, targetName);
  }

  private List<Ref<EntityStore>> findNearbyCats(
      @Nonnull Store<EntityStore> store,
      @Nonnull Vector3d targetPos,
      @Nonnull java.util.UUID ownerUuid) {
    List<Ref<EntityStore>> nearbyCats = new ArrayList<>();

    for (Ref<EntityStore> entityRef : CatsManager.getInstance().getCatsByOwner(ownerUuid, store)) {
      TransformComponent catTransform =
          store.getComponent(entityRef, TransformComponent.getComponentType());
      if (catTransform != null) {
        Vector3d catPos = catTransform.getPosition();
        double distance = calculateDistance(catPos, targetPos);

        if (distance <= ATTACK_RANGE) {
          nearbyCats.add(entityRef);
        }
      }
    }

    return nearbyCats;
  }

  private Ref<EntityStore> selectBestCat(
      @Nonnull Store<EntityStore> store,
      @Nonnull List<Ref<EntityStore>> cats,
      @Nonnull Vector3d targetPos) {
    Ref<EntityStore> closestFollowing = null;
    Ref<EntityStore> closestAvailable = null;
    double minFollowingDistance = Double.MAX_VALUE;
    double minAvailableDistance = Double.MAX_VALUE;

    for (Ref<EntityStore> catRef : cats) {
      CatStateComponent stateComponent =
          store.getComponent(catRef, CatStateComponent.getComponentType());
      CatState state = stateComponent != null ? stateComponent.getState() : CatState.WANDERING;

      // Skip WAITING and SLEEPING cats
      if (state == CatState.WAITING || state == CatState.SLEEPING) {
        continue;
      }

      TransformComponent catTransform =
          store.getComponent(catRef, TransformComponent.getComponentType());
      if (catTransform == null) {
        continue;
      }

      double distance = calculateDistance(catTransform.getPosition(), targetPos);

      // Prefer FOLLOWING cats
      if (state == CatState.FOLLOWING) {
        if (distance < minFollowingDistance) {
          minFollowingDistance = distance;
          closestFollowing = catRef;
        }
      } else {
        if (distance < minAvailableDistance) {
          minAvailableDistance = distance;
          closestAvailable = catRef;
        }
      }
    }

    return closestFollowing != null ? closestFollowing : closestAvailable;
  }

  private double calculateDistance(@Nonnull Vector3d pos1, @Nonnull Vector3d pos2) {
    double dx = pos2.x - pos1.x;
    double dy = pos2.y - pos1.y;
    double dz = pos2.z - pos1.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Nonnull
  private String getEntityDisplayName(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity != null && npcEntity.getRole() != null) {
      String name = npcEntity.getRole().getRoleName();
      if (name != null && !name.isEmpty()) {
        return name;
      }
    }

    CatOwnerComponent ownerComponent =
        store.getComponent(entityRef, CatOwnerComponent.getComponentType());
    if (ownerComponent != null) {
      com.hypixel.hytale.server.core.entity.nameplate.Nameplate nameplate =
          store.getComponent(
              entityRef,
              com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType());
      String catName = nameplate != null ? nameplate.getText() : null;
      if (catName != null && !catName.isEmpty()) {
        return catName;
      }
    }

    return "Entity";
  }
}
