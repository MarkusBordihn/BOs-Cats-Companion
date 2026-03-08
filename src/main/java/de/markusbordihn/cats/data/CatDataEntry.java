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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.UUIDBinaryCodec;
import com.hypixel.hytale.codec.codecs.simple.StringCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public record CatDataEntry(
    @Nonnull UUID uuid,
    @Nullable UUID ownerUuid,
    @Nullable String ownerName,
    @Nonnull CatType catType,
    @Nullable String name,
    @Nonnull CatState state,
    @Nullable Vector3i position,
    @Nonnull CatStatus status,
    @Nullable PersonalityType personalityType,
    @Nullable PersonalityType secondaryPersonality,
    int personalityProgress,
    int happiness,
    long lastMoodUpdate,
    long lastGiftTime,
    int totalGifts) {

  private static final String UUID_TAG = "UUID";
  private static final String OWNER_UUID_TAG = "OwnerUUID";
  private static final String OWNER_NAME_TAG = "OwnerName";
  private static final String TYPE_TAG = "Type";
  private static final String NAME_TAG = "Name";
  private static final String STATE_TAG = "State";
  private static final String POSITION_TAG = "Position";
  private static final String STATUS_TAG = "Status";
  private static final String PERSONALITY_TAG = "Personality";
  private static final String SECONDARY_PERSONALITY_TAG = "SecondaryPersonality";
  private static final String PERSONALITY_PROGRESS_TAG = "PersonalityProgress";
  private static final String HAPPINESS_TAG = "Happiness";
  private static final String LAST_MOOD_UPDATE_TAG = "LastMoodUpdate";
  private static final String LAST_GIFT_TIME_TAG = "LastGiftTime";
  private static final String TOTAL_GIFTS_TAG = "TotalGifts";
  private static final UUIDBinaryCodec UUID_CODEC = new UUIDBinaryCodec();
  private static final StringCodec STRING_CODEC = new StringCodec();
  public static final Codec<CatDataEntry> CODEC =
      new Codec<>() {
        @Override
        public CatDataEntry decode(@Nonnull BsonValue bson, @Nonnull ExtraInfo info) {
          BsonDocument doc = bson.asDocument();
          return new CatDataEntry(
              UUID_CODEC.decode(doc.get(UUID_TAG), info),
              doc.containsKey(OWNER_UUID_TAG)
                  ? UUID_CODEC.decode(doc.get(OWNER_UUID_TAG), info)
                  : null,
              doc.containsKey(OWNER_NAME_TAG)
                  ? STRING_CODEC.decode(doc.get(OWNER_NAME_TAG), info)
                  : null,
              CatType.CODEC.decode(doc.get(TYPE_TAG), info),
              doc.containsKey(NAME_TAG) ? STRING_CODEC.decode(doc.get(NAME_TAG), info) : null,
              CatState.CODEC.decode(doc.get(STATE_TAG), info),
              doc.containsKey(POSITION_TAG)
                  ? Vector3i.CODEC.decode(doc.get(POSITION_TAG), info)
                  : null,
              CatStatus.CODEC.decode(doc.get(STATUS_TAG), info),
              doc.containsKey(PERSONALITY_TAG)
                  ? PersonalityType.CODEC.decode(doc.get(PERSONALITY_TAG), info)
                  : null,
              doc.containsKey(SECONDARY_PERSONALITY_TAG)
                  ? PersonalityType.CODEC.decode(doc.get(SECONDARY_PERSONALITY_TAG), info)
                  : null,
              doc.containsKey(PERSONALITY_PROGRESS_TAG)
                  ? doc.getInt32(PERSONALITY_PROGRESS_TAG).getValue()
                  : 0,
              doc.containsKey(HAPPINESS_TAG)
                  ? doc.getInt32(HAPPINESS_TAG).getValue()
                  : MoodData.DEFAULT_HAPPINESS,
              doc.containsKey(LAST_MOOD_UPDATE_TAG)
                  ? doc.getInt64(LAST_MOOD_UPDATE_TAG).getValue()
                  : 0L,
              doc.containsKey(LAST_GIFT_TIME_TAG)
                  ? doc.getInt64(LAST_GIFT_TIME_TAG).getValue()
                  : 0L,
              doc.containsKey(TOTAL_GIFTS_TAG) ? doc.getInt32(TOTAL_GIFTS_TAG).getValue() : 0);
        }

        @Override
        public BsonValue encode(@Nonnull CatDataEntry entry, @Nonnull ExtraInfo info) {
          BsonDocument doc = new BsonDocument();
          doc.put(UUID_TAG, UUID_CODEC.encode(entry.uuid, info));
          if (entry.ownerUuid != null)
            doc.put(OWNER_UUID_TAG, UUID_CODEC.encode(entry.ownerUuid, info));
          if (entry.ownerName != null)
            doc.put(OWNER_NAME_TAG, STRING_CODEC.encode(entry.ownerName, info));
          doc.put(TYPE_TAG, CatType.CODEC.encode(entry.catType, info));
          if (entry.name != null) doc.put(NAME_TAG, STRING_CODEC.encode(entry.name, info));
          doc.put(STATE_TAG, CatState.CODEC.encode(entry.state, info));
          if (entry.position != null)
            doc.put(POSITION_TAG, Vector3i.CODEC.encode(entry.position, info));
          doc.put(STATUS_TAG, CatStatus.CODEC.encode(entry.status, info));
          if (entry.personalityType != null)
            doc.put(PERSONALITY_TAG, PersonalityType.CODEC.encode(entry.personalityType, info));
          if (entry.secondaryPersonality != null)
            doc.put(
                SECONDARY_PERSONALITY_TAG,
                PersonalityType.CODEC.encode(entry.secondaryPersonality, info));
          if (entry.personalityProgress != 0)
            doc.put(PERSONALITY_PROGRESS_TAG, new org.bson.BsonInt32(entry.personalityProgress));
          if (entry.happiness != MoodData.DEFAULT_HAPPINESS)
            doc.put(HAPPINESS_TAG, new org.bson.BsonInt32(entry.happiness));
          if (entry.lastMoodUpdate != 0L)
            doc.put(LAST_MOOD_UPDATE_TAG, new org.bson.BsonInt64(entry.lastMoodUpdate));
          if (entry.lastGiftTime != 0L)
            doc.put(LAST_GIFT_TIME_TAG, new org.bson.BsonInt64(entry.lastGiftTime));
          if (entry.totalGifts != 0)
            doc.put(TOTAL_GIFTS_TAG, new org.bson.BsonInt32(entry.totalGifts));
          return doc;
        }

        @Override
        public Schema toSchema(@Nonnull SchemaContext context) {
          return Schema.anyOf();
        }
      };

  public CatDataEntry(
      @Nonnull UUID uuid,
      @Nullable UUID ownerUuid,
      @Nullable String ownerName,
      @Nonnull CatType catType,
      @Nullable String name,
      @Nonnull CatState state,
      @Nullable Vector3i position,
      @Nonnull CatStatus status) {
    this(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        null,
        null,
        0,
        MoodData.DEFAULT_HAPPINESS,
        0L,
        0L,
        0);
  }

  public static CatDataEntry empty() {
    return new CatDataEntry(
        UUID.randomUUID(),
        null,
        null,
        CatType.UNKNOWN,
        null,
        CatState.WANDERING,
        null,
        CatStatus.UNKNOWN);
  }

  public boolean hasOwner() {
    return ownerUuid != null;
  }

  public boolean isSpawned() {
    return status == CatStatus.SPAWNED;
  }

  public boolean isDead() {
    return status == CatStatus.DEATH;
  }

  public CatDataEntry withUuid(@Nonnull UUID newUuid) {
    return new CatDataEntry(
        newUuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withOwnerUuid(@Nullable UUID newOwnerUuid) {
    return new CatDataEntry(
        uuid,
        newOwnerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withOwnerName(@Nullable String newOwnerName) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        newOwnerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withOwner(@Nullable UUID newOwnerUuid, @Nullable String newOwnerName) {
    return new CatDataEntry(
        uuid,
        newOwnerUuid,
        newOwnerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withCatType(@Nonnull CatType newCatType) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        newCatType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withName(@Nullable String newName) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        newName,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withState(@Nonnull CatState newState) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        newState,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withPosition(@Nullable Vector3i newPosition) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        newPosition,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withStatus(@Nonnull CatStatus newStatus) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        newStatus,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withPersonalityType(@Nullable PersonalityType newPersonalityType) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        newPersonalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withSecondaryPersonality(@Nullable PersonalityType newSecondaryPersonality) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        newSecondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withPersonalityProgress(int newPersonalityProgress) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        newPersonalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withHappiness(int newHappiness) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        Math.clamp(newHappiness, MoodData.MIN_HAPPINESS, MoodData.MAX_HAPPINESS),
        lastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withLastMoodUpdate(long newLastMoodUpdate) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        newLastMoodUpdate,
        lastGiftTime,
        totalGifts);
  }

  public CatDataEntry withLastGiftTime(long newLastGiftTime) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        newLastGiftTime,
        totalGifts);
  }

  public CatDataEntry withTotalGifts(int newTotalGifts) {
    return new CatDataEntry(
        uuid,
        ownerUuid,
        ownerName,
        catType,
        name,
        state,
        position,
        status,
        personalityType,
        secondaryPersonality,
        personalityProgress,
        happiness,
        lastMoodUpdate,
        lastGiftTime,
        newTotalGifts);
  }
}
