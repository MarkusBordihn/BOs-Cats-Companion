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

package de.markusbordihn.cats.sensors;

import com.google.gson.JsonElement;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import de.markusbordihn.cats.component.CatMoodComponent;
import de.markusbordihn.cats.data.CatBehaviorProfile;
import de.markusbordihn.cats.data.CatBehaviorProfileResolver;
import de.markusbordihn.cats.data.CatDataEntry;
import de.markusbordihn.cats.manager.CatsManager;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class BuilderSensorCatPersonalityWeight extends BuilderSensorBase {

  public static final String SENSOR_ID = "CatPersonalityWeight";

  private final StringHolder weight = new StringHolder();
  private final FloatHolder threshold = new FloatHolder();

  @Nonnull
  @Override
  public Sensor build(@Nonnull BuilderSupport support) {
    return new SensorCatPersonalityWeight(this, support);
  }

  @Nonnull
  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  @Override
  public BuilderSensorCatPersonalityWeight readConfig(@Nonnull JsonElement data) {
    this.requireString(
        data,
        "Weight",
        this.weight,
        null,
        BuilderDescriptorState.Stable,
        "Which profile weight to test: play, rest, activity, or social",
        null);
    this.requireFloat(
        data,
        "Threshold",
        this.threshold,
        null,
        BuilderDescriptorState.Stable,
        "Minimum weight value (0.0-1.0) the cat must meet for the sensor to match",
        null);
    return this;
  }

  @Nonnull
  public String getWeight(@Nonnull BuilderSupport builderSupport) {
    return this.weight.get(builderSupport.getExecutionContext());
  }

  public float getThreshold(@Nonnull BuilderSupport builderSupport) {
    return this.threshold.get(builderSupport.getExecutionContext());
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Matches when the cat's personality weight for a dimension exceeds a threshold";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Resolves the cat's CatBehaviorProfile and probabilistically checks whether "
        + "the specified weight dimension (play/rest/activity/social) exceeds the configured "
        + "threshold, using a small random band [0.8, 1.2] to add natural variance.";
  }

  public static class SensorCatPersonalityWeight extends SensorBase {
    private final String weightField;
    private final float threshold;

    public SensorCatPersonalityWeight(
        @Nonnull BuilderSensorCatPersonalityWeight builderSensorCatPersonalityWeight,
        @Nonnull BuilderSupport builderSupport) {
      super(builderSensorCatPersonalityWeight);
      this.weightField = builderSensorCatPersonalityWeight.getWeight(builderSupport);
      this.threshold = builderSensorCatPersonalityWeight.getThreshold(builderSupport);
    }

    @Override
    public boolean matches(
        @Nonnull Ref<EntityStore> entityRef,
        @Nonnull Role role,
        double dt,
        @Nonnull Store<EntityStore> store) {
      if (!super.matches(entityRef, role, dt, store)) {
        return false;
      }

      CatsManager catsManager = CatsManager.getInstance();
      if (catsManager == null) {
        return false;
      }

      CatDataEntry catData = catsManager.getCatData(entityRef, store);
      if (catData == null) {
        return false;
      }

      CatMoodComponent moodComponent =
          store.getComponent(entityRef, CatMoodComponent.getComponentType());

      CatBehaviorProfile profile =
          CatBehaviorProfileResolver.resolve(
              catData.personalityType(),
              catData.secondaryPersonality(),
              moodComponent != null ? moodComponent.getLevel() : null);

      float profileWeight =
          switch (weightField.toLowerCase()) {
            case "play" -> profile.playWeight();
            case "rest" -> profile.restWeight();
            case "social" -> profile.socialWeight();
            default -> profile.activityWeight();
          };

      float randomBand = 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f;
      return profileWeight > threshold * randomBand;
    }

    @Override
    public InfoProvider getSensorInfo() {
      return null;
    }
  }
}
