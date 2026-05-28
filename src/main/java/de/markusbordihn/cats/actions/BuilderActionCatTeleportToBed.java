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
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.component.CatBedTargetComponent;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.manager.CatsManager;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class BuilderActionCatTeleportToBed extends BuilderActionBase {

  public static final String BUILDER_ID = "CatTeleportToBed";

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
  public BuilderActionCatTeleportToBed readConfig(JsonElement config) {
    return this;
  }

  @Nonnull
  @Override
  public ActionCatTeleportToBed build(BuilderSupport support) {
    return new ActionCatTeleportToBed(this);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Teleports cat to target bed and sets sleeping state";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Teleports the cat to the bed position stored in CatBedTargetComponent, "
        + "sets CatStateComponent to SLEEPING, and cleans up the target component";
  }

  public static class ActionCatTeleportToBed extends ActionBase {

    public ActionCatTeleportToBed(BuilderActionBase builder) {
      super(builder);
    }

    @Override
    public boolean canExecute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      CatBedTargetComponent bedTarget =
          store.getComponent(entityRef, CatBedTargetComponent.getComponentType());
      return bedTarget != null && bedTarget.hasTarget();
    }

    @Override
    public boolean execute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      CatBedTargetComponent bedTarget =
          store.getComponent(entityRef, CatBedTargetComponent.getComponentType());
      if (bedTarget == null || !bedTarget.hasTarget()) {
        return false;
      }
      Vector3d targetPos = bedTarget.getTargetPosition();

      TransformComponent currentTransform =
          store.getComponent(entityRef, TransformComponent.getComponentType());
      TransformComponent newTransform =
          new TransformComponent(
              new Vector3d(targetPos.x, targetPos.y + 0.5, targetPos.z),
              currentTransform != null ? currentTransform.getRotation() : new Rotation3f(0, 0, 0));
      store.putComponent(entityRef, TransformComponent.getComponentType(), newTransform);

      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager != null) {
        catsManager.updateCatState(entityRef, CatState.SLEEPING, store);
      }

      if (role != null && role.getStateSupport() != null) {
        role.getStateSupport().setState(entityRef, "Pet", "Sleeping", store);
      }

      var bedTargetType = CatBedTargetComponent.getComponentType();
      if (bedTargetType != null && store.getComponent(entityRef, bedTargetType) != null) {
        store.removeComponent(entityRef, bedTargetType);
      }
      return true;
    }
  }
}
