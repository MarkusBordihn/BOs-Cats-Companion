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
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.data.HappinessLevel;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionCatMoodParticles extends BuilderActionBase {

  public static final String BUILDER_ID = "CatMoodParticles";

  private static final double MIN_PARTICLE_INTERVAL = 45.0;
  private static final double MAX_PARTICLE_INTERVAL = 120.0;

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
  public BuilderActionCatMoodParticles readConfig(JsonElement config) {
    return this;
  }

  @Nonnull
  @Override
  public ActionCatMoodParticles build(BuilderSupport support) {
    return new ActionCatMoodParticles(this);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Periodically shows mood particles via transient sub-states";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Accumulates deltaTime per entity and every 45-120s sets a transient mood sub-state "
        + "(MoodEcstatic/Happy/Sad/Miserable). JSON SpawnParticles blocks react to the state "
        + "and reset it via Timeout. NEUTRAL mood shows no particles. Skipped during Default (following) state.";
  }

  public static class ActionCatMoodParticles extends ActionBase {

    private static final HashMap<Ref<EntityStore>, Double> elapsedByEntity = new HashMap<>();

    public ActionCatMoodParticles(BuilderActionBase builder) {
      super(builder);
    }

    @Nullable
    private static String toMoodSubState(@Nonnull HappinessLevel happinessLevel) {
      return switch (happinessLevel) {
        case ECSTATIC -> "MoodEcstatic";
        case HAPPY -> "MoodHappy";
        case SAD -> "MoodSad";
        case MISERABLE -> "MoodMiserable";
        default -> null;
      };
    }

    @Override
    public boolean canExecute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      return store.getComponent(entityRef, CatMoodComponent.getComponentType()) != null;
    }

    @Override
    public boolean execute(
        Ref<EntityStore> entityRef,
        Role role,
        InfoProvider infoProvider,
        double deltaTime,
        Store<EntityStore> store) {
      double elapsedSeconds =
          elapsedByEntity.compute(
              entityRef,
              (key, previous) ->
                  previous == null
                      ? ThreadLocalRandom.current().nextDouble(0, MAX_PARTICLE_INTERVAL)
                      : previous + deltaTime);

      if (elapsedSeconds < MIN_PARTICLE_INTERVAL) {
        return true;
      }

      double particleThreshold =
          ThreadLocalRandom.current().nextDouble(MIN_PARTICLE_INTERVAL, MAX_PARTICLE_INTERVAL);
      if (elapsedSeconds < particleThreshold) {
        return true;
      }

      elapsedByEntity.put(entityRef, 0.0);

      StateSupport stateSupport = role.getStateSupport();
      if (stateSupport.inState("Pet", "Default")
          || stateSupport.inState("Pet", "PrepareSleep")
          || stateSupport.inState("Pet", "PrepareFollow")
          || stateSupport.inState("Pet", "PreparePlay")) {
        return true;
      }

      CatMoodComponent moodComponent =
          store.getComponent(entityRef, CatMoodComponent.getComponentType());
      if (moodComponent == null) {
        return true;
      }

      String moodSubState = toMoodSubState(moodComponent.getLevel());
      if (moodSubState != null) {
        role.getStateSupport().setState(entityRef, "Pet", moodSubState, store);
      }

      return true;
    }
  }
}
