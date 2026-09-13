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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.component.CatStateComponent;
import de.markusbordihn.cats.data.CatBehaviorProfile;
import de.markusbordihn.cats.data.CatBehaviorProfileResolver;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.data.CatNeedType;
import de.markusbordihn.cats.data.CatState;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionCatIdleChoice extends BuilderActionBase {

  public static final String BUILDER_ID = "CatIdleChoice";

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
  public BuilderActionCatIdleChoice readConfig(@Nullable JsonElement config) {
    return this;
  }

  @Nonnull
  @Override
  public ActionCatIdleChoice build(BuilderSupport support) {
    return new ActionCatIdleChoice(this);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Picks a personality-weighted idle sub-state for a waiting cat";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Resolves the cat's CatBehaviorProfile and selects a transient NPC sub-state "
        + "(Stretching, Licking, Sitting, Playing) via weighted random draw. "
        + "Corrects stale CatStateComponent if JSON woke the cat without syncing. "
        + "The JSON Timeout for each sub-state handles the return to .Waiting.";
  }

  public static class ActionCatIdleChoice extends ActionBase {

    public ActionCatIdleChoice(BuilderActionBase builder) {
      super(builder);
    }

    @Nonnull
    private static String pickSubState(@Nonnull CatBehaviorProfile profile) {
      float lickingWeight = profile.restWeight();
      float sittingWeight = 0.3f;
      float stretchingWeight = profile.activityWeight() > 0.2f ? profile.activityWeight() : 0f;
      float playingWeight = profile.playWeight() > 0.4f ? profile.playWeight() * 0.7f : 0f;

      float totalWeight = lickingWeight + sittingWeight + stretchingWeight + playingWeight;

      float roll = ThreadLocalRandom.current().nextFloat() * totalWeight;
      float cumulative = lickingWeight;
      if (roll < cumulative) {
        return "Licking";
      }

      cumulative += sittingWeight;
      if (roll < cumulative) {
        return "Sitting";
      }

      cumulative += stretchingWeight;
      if (roll < cumulative) {
        return "Stretching";
      }

      return "Playing";
    }

    @Override
    public boolean canExecute(
        Ref<EntityStore> entityRef,
        ExecutionSupport executionSupport,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      return store.getComponent(entityRef, CatMoodComponent.getComponentType()) != null;
    }

    @Override
    public boolean execute(
        Ref<EntityStore> entityRef,
        ExecutionSupport executionSupport,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      if (executionSupport == null) {
        return true;
      }

      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager == null) {
        return true;
      }

      CatDataEntry catData = catsManager.getCatData(entityRef, store);
      if (catData == null) {
        return true;
      }

      CatMoodComponent moodComponent =
          store.getComponent(entityRef, CatMoodComponent.getComponentType());
      catsManager.updateNeeds(entityRef, store);

      CatStateComponent stateComponent =
          store.getComponent(entityRef, CatStateComponent.getComponentType());
      if (stateComponent != null
          && (stateComponent.getState() == CatState.SLEEPING
              || stateComponent.getState() == CatState.GOING_TO_BED)) {
        catsManager.updateCatState(entityRef, CatState.WAITING, store);
      }

      CatNeedType criticalNeed = catsManager.getCriticalNeed(entityRef, store);

      if (criticalNeed != CatNeedType.NONE) {
        StateSupport stateSupport = executionSupport.getStateSupport();
        String overrideState =
            switch (criticalNeed) {
              case REST -> {
                if (stateSupport.inState("Pet", "GoingToBed")
                    || stateSupport.inState("Pet", "Sleeping")
                    || stateSupport.inState("Pet", "PrepareSleep")) {
                  yield null;
                }
                yield "PrepareSleep";
              }
              case SOCIAL ->
                  (stateSupport.inState("Pet", "Default")
                          || stateSupport.inState("Pet", "PrepareFollow"))
                      ? null
                      : "PrepareFollow";
              case PLAY ->
                  (stateSupport.inState("Pet", "Playing")
                          || stateSupport.inState("Pet", "Searching")
                          || stateSupport.inState("Pet", "PreparePlay"))
                      ? null
                      : "PreparePlay";
              case NONE -> null;
            };
        if (overrideState != null) {
          CatState catStateForOverride =
              switch (overrideState) {
                case "PrepareFollow" -> CatState.FOLLOWING;
                case "PrepareSleep" -> CatState.GOING_TO_BED;
                case "PreparePlay" -> CatState.PLAYING;
                default -> null;
              };
          if (catStateForOverride != null) {
            catsManager.updateCatState(entityRef, catStateForOverride, store);
          }
          executionSupport.getStateSupport().setState(entityRef, "Pet", overrideState, store);
          return true;
        }
      }

      String chosen =
          pickSubState(
              CatBehaviorProfileResolver.resolve(
                  catData.personalityType(),
                  catData.secondaryPersonality(),
                  moodComponent != null ? moodComponent.getLevel() : null));
      executionSupport.getStateSupport().setState(entityRef, "Pet", chosen, store);
      return true;
    }
  }
}
