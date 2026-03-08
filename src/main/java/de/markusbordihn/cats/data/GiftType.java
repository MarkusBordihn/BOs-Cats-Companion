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

package de.markusbordihn.cats.data;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public enum GiftType {
  FEATHER(
      PersonalityType.PLAYFUL,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Ingredient_Feathers_Light"),
          HappinessLevel.HAPPY, List.of("Ingredient_Feathers_Light", "Ingredient_Feathers_Blue"),
          HappinessLevel.ECSTATIC, List.of("Ingredient_Feathers_Blue", "Ingredient_Feathers_Red"))),
  FLOWER(
      PersonalityType.CUDDLY,
      Map.of(
          HappinessLevel.NEUTRAL,
              List.of("Plant_Flower_Common_White", "Plant_Flower_Common_Yellow"),
          HappinessLevel.HAPPY, List.of("Plant_Flower_Common_Blue", "Plant_Flower_Common_Pink"),
          HappinessLevel.ECSTATIC,
              List.of("Plant_Flower_Orchid_Purple", "Plant_Flower_Orchid_Blue"))),
  FISH(
      PersonalityType.LAZY,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Fish_Minnow_Item"),
          HappinessLevel.HAPPY, List.of("Fish_Minnow_Item", "Fish_Bluegill_Item"),
          HappinessLevel.ECSTATIC, List.of("Fish_Salmon_Item", "Fish_Trout_Rainbow_Item"))),
  CRYSTAL(
      PersonalityType.CURIOUS,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Ingredient_Crystal_White"),
          HappinessLevel.HAPPY, List.of("Ingredient_Crystal_Green", "Ingredient_Crystal_Blue"),
          HappinessLevel.ECSTATIC, List.of("Ingredient_Crystal_Purple", "Ingredient_Crystal_Red"))),
  LEAF(
      PersonalityType.INDEPENDENT,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Plant_Petals_White", "Plant_Petals_Green"),
          HappinessLevel.HAPPY, List.of("Plant_Petals_Blue", "Plant_Petals_Azure"),
          HappinessLevel.ECSTATIC, List.of("Plant_Petals_Storm", "Plant_Petals_Blood"))),
  BERRY(
      PersonalityType.MISCHIEVOUS,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Plant_Fruit_Berries_Red"),
          HappinessLevel.HAPPY, List.of("Plant_Fruit_Berries_Red", "Plant_Fruit_Apple"),
          HappinessLevel.ECSTATIC, List.of("Plant_Fruit_Mango", "Plant_Fruit_Pinkberry"))),
  BONE(
      PersonalityType.BRAVE,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Deco_Bone_Full"),
          HappinessLevel.HAPPY, List.of("Deco_Bone_Full", "Deco_Bone_Spike"),
          HappinessLevel.ECSTATIC, List.of("Deco_Bone_Skulls", "Deco_Bone_Spine"))),
  HIDE(
      PersonalityType.SHY,
      Map.of(
          HappinessLevel.NEUTRAL, List.of("Ingredient_Hide_Soft"),
          HappinessLevel.HAPPY, List.of("Ingredient_Hide_Light", "Ingredient_Hide_Medium"),
          HappinessLevel.ECSTATIC, List.of("Ingredient_Hide_Heavy", "Ingredient_Hide_Scaled")));

  public static final EnumCodec<GiftType> CODEC = new EnumCodec<>(GiftType.class);

  private final PersonalityType affinityPersonality;
  private final Map<HappinessLevel, List<String>> itemsByLevel;

  GiftType(PersonalityType affinityPersonality, Map<HappinessLevel, List<String>> itemsByLevel) {
    this.affinityPersonality = affinityPersonality;
    this.itemsByLevel = itemsByLevel;
  }

  public static GiftType randomForPersonality(PersonalityType personality) {
    GiftType[] values = values();
    if (personality != null && ThreadLocalRandom.current().nextFloat() < 0.4f) {
      for (GiftType gift : values) {
        if (gift.affinityPersonality == personality) {
          return gift;
        }
      }
    }
    return values[ThreadLocalRandom.current().nextInt(values.length)];
  }

  public PersonalityType getAffinityPersonality() {
    return affinityPersonality;
  }

  public String getItemId(HappinessLevel level) {
    List<String> items =
        itemsByLevel.getOrDefault(
            level,
            itemsByLevel.getOrDefault(
                HappinessLevel.HAPPY, itemsByLevel.get(HappinessLevel.NEUTRAL)));
    if (items == null || items.isEmpty()) {
      return null;
    }
    return items.get(ThreadLocalRandom.current().nextInt(items.size()));
  }
}
