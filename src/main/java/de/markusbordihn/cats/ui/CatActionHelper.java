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

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import de.markusbordihn.cats.blocks.CatBed;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatBedInfo;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CatActionHelper {

  private static final double POUNCE_RANGE = 15.0;

  private CatActionHelper() {}

  public static boolean follow(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }
    var stateSupport = npcEntity.getRole().getStateSupport();

    CatsManager.getInstance().updateCatState(entityRef, CatState.FOLLOWING, store);
    if (stateSupport.inState("Pet", "Default")) {
      stateSupport.setState(entityRef, "Pet", "Playing", store);
    }
    stateSupport.setState(entityRef, "Pet", "Default", store);
    return true;
  }

  public static boolean stop(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.WAITING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Waiting", store);
    return true;
  }

  public static boolean leaveBed(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.WAITING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Waiting", store);
    TransformComponent transform =
        store.getComponent(entityRef, TransformComponent.getComponentType());
    if (transform != null) {
      Vector3d pos = transform.getPosition();
      TransientPath path = new TransientPath();
      path.addWaypoint(new Vector3d(pos.x + 2.0, pos.y, pos.z), new Vector3f(0, 0, 0));
      npcEntity.getPathManager().setTransientPath(path);
    }
    return true;
  }

  public static boolean sit(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.SITTING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Sitting", store);
    return true;
  }

  public static boolean sleep(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.SLEEPING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Sleeping", store);
    return true;
  }

  public static boolean play(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.PLAYING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Playing", store);
    return true;
  }

  public static boolean wander(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }

    CatsManager.getInstance().updateCatState(entityRef, CatState.WANDERING, store);
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "Wandering", store);
    return true;
  }

  public static boolean goToBed(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull World world) {
    TransformComponent catTransform =
        store.getComponent(entityRef, TransformComponent.getComponentType());
    if (catTransform == null) {
      return false;
    }
    List<CatBedInfo> beds = CatBed.findCatBeds(world, catTransform.getPosition());
    if (beds.isEmpty()) {
      return false;
    }
    CatBedInfo availableBed =
        CatBed.findNearestAvailableBed(beds, store, catTransform.getPosition());
    if (availableBed == null) {
      return false;
    }
    NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
    if (npcEntity == null || npcEntity.getRole() == null) {
      return false;
    }
    TransientPath path = new TransientPath();
    path.addWaypoint(
        new Vector3d(
            availableBed.getPosition().x,
            availableBed.getPosition().y + 0.5,
            availableBed.getPosition().z),
        new Vector3f(0, 0, 0));
    npcEntity.getPathManager().setTransientPath(path);
    store.putComponent(
        entityRef,
        CatBedTargetComponent.getComponentType(),
        new CatBedTargetComponent(availableBed.getPosition()));
    store.putComponent(
        entityRef,
        CatStateComponent.getComponentType(),
        new CatStateComponent(CatState.GOING_TO_BED));
    npcEntity.getRole().getStateSupport().setState(entityRef, "Pet", "GoingToBed", store);
    return true;
  }

  public static boolean hasBedAvailable(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull World world) {
    TransformComponent catTransform =
        store.getComponent(entityRef, TransformComponent.getComponentType());
    if (catTransform == null) {
      return false;
    }
    List<CatBedInfo> beds = CatBed.findCatBeds(world, catTransform.getPosition());
    if (beds.isEmpty()) {
      return false;
    }
    return CatBed.findNearestAvailableBed(beds, store, catTransform.getPosition()) != null;
  }

  @Nullable
  public static Ref<EntityStore> pounce(
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull UUID ownerUuid) {
    TransformComponent targetTransform =
        store.getComponent(targetRef, TransformComponent.getComponentType());
    if (targetTransform == null) {
      return null;
    }
    List<Ref<EntityStore>> nearbyCats =
        findNearbyCats(store, targetTransform.getPosition(), ownerUuid);
    if (nearbyCats.isEmpty()) {
      return null;
    }
    Ref<EntityStore> selectedCat = selectBestCat(store, nearbyCats, targetTransform.getPosition());
    if (selectedCat == null) {
      return null;
    }
    CatsManager.getInstance().updateCatState(selectedCat, CatState.ATTACKING, store);
    NPCEntity npcEntity = store.getComponent(selectedCat, NPCEntity.getComponentType());
    if (npcEntity != null && npcEntity.getRole() != null) {
      npcEntity.getRole().getStateSupport().setState(selectedCat, "Pet", "Attacking", store);
      npcEntity.getRole().getMarkedEntitySupport().setMarkedEntity("LockedTarget", targetRef);
      TransientPath path = new TransientPath();
      path.addWaypoint(
          new Vector3d(
              targetTransform.getPosition().x,
              targetTransform.getPosition().y,
              targetTransform.getPosition().z),
          new Vector3f(0, 0, 0));
      npcEntity.getPathManager().setTransientPath(path);
    }
    return selectedCat;
  }

  private static List<Ref<EntityStore>> findNearbyCats(
      @Nonnull Store<EntityStore> store, @Nonnull Vector3d targetPos, @Nonnull UUID ownerUuid) {
    List<Ref<EntityStore>> nearbyCats = new ArrayList<>();
    for (Ref<EntityStore> entityRef : CatsManager.getInstance().getCatsByOwner(ownerUuid, store)) {
      TransformComponent catTransform =
          store.getComponent(entityRef, TransformComponent.getComponentType());
      if (catTransform != null && distance(catTransform.getPosition(), targetPos) <= POUNCE_RANGE) {
        nearbyCats.add(entityRef);
      }
    }
    return nearbyCats;
  }

  @Nullable
  private static Ref<EntityStore> selectBestCat(
      @Nonnull Store<EntityStore> store,
      @Nonnull List<Ref<EntityStore>> cats,
      @Nonnull Vector3d targetPos) {
    Ref<EntityStore> closestFollowing = null;
    Ref<EntityStore> closestAvailable = null;
    double minFollowingDist = Double.MAX_VALUE;
    double minAvailableDist = Double.MAX_VALUE;
    for (Ref<EntityStore> catRef : cats) {
      CatStateComponent stateComp =
          store.getComponent(catRef, CatStateComponent.getComponentType());
      CatState state = stateComp != null ? stateComp.getState() : CatState.WANDERING;
      if (state == CatState.WAITING || state == CatState.SLEEPING) {
        continue;
      }
      TransformComponent transform =
          store.getComponent(catRef, TransformComponent.getComponentType());
      if (transform == null) {
        continue;
      }
      double d = distance(transform.getPosition(), targetPos);
      if (state == CatState.FOLLOWING) {
        if (d < minFollowingDist) {
          minFollowingDist = d;
          closestFollowing = catRef;
        }
      } else {
        if (d < minAvailableDist) {
          minAvailableDist = d;
          closestAvailable = catRef;
        }
      }
    }
    return closestFollowing != null ? closestFollowing : closestAvailable;
  }

  private static double distance(@Nonnull Vector3d a, @Nonnull Vector3d b) {
    double dx = b.x - a.x;
    double dy = b.y - a.y;
    double dz = b.z - a.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
}
