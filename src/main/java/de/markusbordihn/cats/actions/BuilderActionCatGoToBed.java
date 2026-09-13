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
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.blocks.CatBed;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.data.CatBedInfo;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.data.HappinessSource;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class BuilderActionCatGoToBed extends BuilderActionBase {

  public static final String BUILDER_ID = "CatGoToBed";

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
  public BuilderActionCatGoToBed readConfig(@Nullable JsonElement config) {
    return this;
  }

  @Nonnull
  @Override
  public ActionCatGoToBed build(BuilderSupport support) {
    return new ActionCatGoToBed(this);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Finds a bed and sends cat to it, or sleeps on the spot";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Searches for a nearby available cat bed. If found, sets a TransientPath and "
        + "GoingToBed state. If no bed is available, falls back to sleeping on the spot.";
  }

  public static class ActionCatGoToBed extends ActionBase {

    public ActionCatGoToBed(BuilderActionBase builder) {
      super(builder);
    }

    @Override
    public boolean execute(
        Ref<EntityStore> entityRef,
        ExecutionSupport executionSupport,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {

      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager == null || executionSupport == null) {
        return true;
      }

      TransformComponent catTransform =
          store.getComponent(entityRef, TransformComponent.getComponentType());
      if (catTransform == null) {
        catsManager.updateCatState(entityRef, CatState.SLEEPING, store);
        executionSupport.getStateSupport().setState(entityRef, "Pet", "Sleeping", store);
        return true;
      }

      World world = store.getExternalData().getWorld();
      if (world == null) {
        catsManager.updateCatState(entityRef, CatState.SLEEPING, store);
        executionSupport.getStateSupport().setState(entityRef, "Pet", "Sleeping", store);
        return true;
      }

      List<CatBedInfo> beds = CatBed.findCatBeds(world, catTransform.getPosition());
      CatBedInfo availableBed =
          beds.isEmpty()
              ? null
              : CatBed.findNearestAvailableBed(beds, store, catTransform.getPosition());

      if (availableBed != null) {
        NPCEntity npcEntity = store.getComponent(entityRef, NPCEntity.getComponentType());
        if (npcEntity != null) {
          TransientPath path = new TransientPath();
          path.addWaypoint(
              new Vector3d(
                  availableBed.getPosition().x,
                  availableBed.getPosition().y + 0.5,
                  availableBed.getPosition().z),
              new Rotation3f(0, 0, 0));
          npcEntity.getPathManager().setTransientPath(path);
        }
        store.putComponent(
            entityRef,
            CatBedTargetComponent.getComponentType(),
            new CatBedTargetComponent(availableBed.getPosition()));
        catsManager.updateCatState(entityRef, CatState.GOING_TO_BED, store);
        executionSupport.getStateSupport().setState(entityRef, "Pet", "GoingToBed", store);
      } else {
        catsManager.updateCatState(entityRef, CatState.SLEEPING, store);
        catsManager.boostHappiness(entityRef, HappinessSource.SLEEPING, store);
        catsManager.satisfyNeed(entityRef, CatNeedType.REST, 15f, store);
        executionSupport.getStateSupport().setState(entityRef, "Pet", "Sleeping", store);
      }

      return true;
    }
  }
}
