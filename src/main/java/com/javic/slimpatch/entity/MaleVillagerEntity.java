package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import com.javic.slimpatch.util.SkinPathHelper;
import com.javic.slimpatch.dialogue.DialogueGoal;
import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.config.VillagerNameConfig;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.RelationshipSyncPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.File;

public class MaleVillagerEntity extends Villager implements CommandableVillager, VillagerEquipmentHolder, FamilyVillager {

    private static final int MALE_SKINS = 70;
    private static final double RELAXED_OWNER_LOOK_SUPPRESS_DISTANCE_SQR = 64.0D;
    private static final double RELAXED_OWNER_LOOK_NEAR_DISTANCE_SQR = 16.0D;
    private static final double RELAXED_OWNER_LOOK_MOVE_AWAY_SPEED_SQR = 0.01D;
    private static final double RELAXED_OWNER_LOOK_MOVE_AWAY_DOT = 0.08D;
    private VillagerCommandState commandState = VillagerCommandState.NONE;
    private UUID commandTargetUuid;
    private UUID commandOwnerUuid;
    private UUID spousePlayerUuid;
    private UUID formerSpousePlayerUuid;
    private UUID spouseVillagerUuid;
    private UUID expectingParentPlayerUuid;
    private UUID expectingOtherParentVillagerUuid;
    private UUID parentPlayerUuid;
    private UUID parentVillagerUuid;
    private UUID parentVillager2Uuid;
    private UUID familyOwnerPlayerUuid;
    private UUID naturalFamilyGroupId;
    private BlockPos stayPos;
    private Vec3 stayAnchorPos;
    private BlockPos homePos;
    private ResourceKey<Level> homeDimension;
    private Vec3 dialogueHoldPos;
    private long lastCommandTeleportTick;
    private int combatLockTicks;
    private int lastDamageTick;
    private int lastAggressiveScanTick;
    private int lastBowAttackTick;
    private int bowChargeStartTick = -1;
    private int lastCrossbowAttackTick;
    private int crossbowChargeStartTick = -1;
    private int lastShieldBlockTick;
    private int shieldRaiseUntilTick;
    private float bonusHealth;
    private double baseMaxHealth;
    private final SimpleContainer equipmentInventory = new SimpleContainer(27);
    private ItemStack persistentMainHandItem = ItemStack.EMPTY;

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_PERSONALITY =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Float> DATA_RELATIONSHIP =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.FLOAT);

    public static final EntityDataAccessor<Boolean> DATA_HAS_QUEST =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<String> DATA_QUEST_ID =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_CUSTOM_SKIN_PATH =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_SAVED_SKIN_INPUT =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Integer> DATA_HEIGHT =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_WIDTH =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_COMMAND_STATE =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_HAS_HOME =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<String> DATA_COMMAND_OWNER_UUID =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_COMMAND_OWNER_NAME =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_COMBAT_MODE =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_FOLLOW_MODE =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_HIDE_ARMOR =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_MUTED =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Float> DATA_GOLDEN_RELATIONSHIP =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<String> DATA_SPOUSE_PLAYER_NAME =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_FORMER_SPOUSE_PLAYER_NAME =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_AGE_STAGE =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_RELATIONSHIP_STAGE =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_EXPECTING_CHILD =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_BORN_FROM_FAMILY_SYSTEM =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<String> DATA_PARENT_PLAYER_UUID =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    public MaleVillagerEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_PERSONALITY, "FRIENDLY");
        builder.define(DATA_RELATIONSHIP, 0.5f);
        builder.define(DATA_HAS_QUEST, false);
        builder.define(DATA_QUEST_ID, "");
        builder.define(DATA_CUSTOM_SKIN_PATH, "");
        builder.define(DATA_SAVED_SKIN_INPUT, "");
        builder.define(DATA_HEIGHT, 100);
        builder.define(DATA_WIDTH, 100);
        builder.define(DATA_COMMAND_STATE, VillagerCommandState.NONE.name());
        builder.define(DATA_HAS_HOME, false);
        builder.define(DATA_COMMAND_OWNER_UUID, "");
        builder.define(DATA_COMMAND_OWNER_NAME, "");
        builder.define(DATA_COMBAT_MODE, VillagerCombatMode.AGGRESSIVE.name());
        builder.define(DATA_FOLLOW_MODE, VillagerFollowMode.CLOSE.name());
        builder.define(DATA_HIDE_ARMOR, false);
        builder.define(DATA_MUTED, false);
        builder.define(DATA_GOLDEN_RELATIONSHIP, 0.0F);
        builder.define(DATA_SPOUSE_PLAYER_NAME, "");
        builder.define(DATA_FORMER_SPOUSE_PLAYER_NAME, "");
        builder.define(DATA_AGE_STAGE, VillagerAgeStage.ADULT.name());
        builder.define(DATA_RELATIONSHIP_STAGE, VillagerRelationshipStage.FRIENDSHIP.name());
        builder.define(DATA_EXPECTING_CHILD, false);
        builder.define(DATA_BORN_FROM_FAMILY_SYSTEM, false);
        builder.define(DATA_PARENT_PLAYER_UUID, "");
    }

    public void setSkinIndex(int index) {
        this.entityData.set(DATA_SKIN, index);
        this.getPersistentData().putInt("slimpatch_skin", index);
    }

    public int getSkinIndex() {
        return this.entityData.get(DATA_SKIN);
    }

    public ResourceLocation getSkinTexture() {
        CompoundTag data = this.getPersistentData();

        if (data.contains("CustomSkinPath")) {
            String customSkinPath = data.getString("CustomSkinPath");
            ResourceLocation internal = ResourceLocation.tryParse(customSkinPath);
            if (internal != null) {
                return internal;
            }
            File external = new File(customSkinPath);
            if (external.exists() && external.isFile()) {
                try {
                    ResourceLocation loc = SkinPathHelper.loadExternalSkinTexture(this.getUUID(), external);
                    if (loc != null) {
                        return loc;
                    }
                } catch (Exception e) {
                }
            }
        }

        int skin = this.getSkinIndex();

        if (skin <= 0 || skin > MALE_SKINS) {
            if (data.contains("slimpatch_skin")) {
                skin = data.getInt("slimpatch_skin");
            } else {
                skin = this.getRandom().nextInt(MALE_SKINS) + 1;
                this.setSkinIndex(skin);
                data.putInt("slimpatch_skin", skin);
            }
        }

        return SkinPathHelper.getSkin("male", skin, this.level());
    }

    public VillagerPersonality getPersonality() {
        try {
            return VillagerPersonality.valueOf(this.entityData.get(DATA_PERSONALITY));
        } catch (IllegalArgumentException e) {
            return VillagerPersonality.FRIENDLY;
        }
    }

    public void setPersonality(VillagerPersonality personality) {
        VillagerPersonality resolvedPersonality = VillagerFamilyData.sanitizePersonality(this, personality);
        this.entityData.set(DATA_PERSONALITY, resolvedPersonality.name());
        this.getPersistentData().putString("slimpatch_personality", resolvedPersonality.name());
    }

    public float getRelationship() {
        return this.entityData.get(DATA_RELATIONSHIP);
    }

    public void setRelationship(float value) {
        float clamped = Math.max(0.0f, Math.min(5.0f, value));
        this.entityData.set(DATA_RELATIONSHIP, clamped);
        this.getPersistentData().putFloat("slimpatch_relationship", clamped);
    }

    public boolean hasQuest() {
        return this.entityData.get(DATA_HAS_QUEST);
    }

    public void setHasQuest(boolean value) {
        this.entityData.set(DATA_HAS_QUEST, value);
        this.getPersistentData().putBoolean("HasQuest", value);
    }

    public String getQuestId() {
        return this.entityData.get(DATA_QUEST_ID);
    }

    public void setQuestId(String id) {
        this.entityData.set(DATA_QUEST_ID, id == null ? "" : id);
        if (id == null || id.isEmpty()) {
            this.getPersistentData().remove("QuestId");
        } else {
            this.getPersistentData().putString("QuestId", id);
        }
    }

    public String getCustomSkinPath() {
        return this.entityData.get(DATA_CUSTOM_SKIN_PATH);
    }

    public void setCustomSkinPath(String path) {
        String value = path == null ? "" : path;
        this.entityData.set(DATA_CUSTOM_SKIN_PATH, value);
        if (value.isEmpty()) {
            this.getPersistentData().remove("CustomSkinPath");
        } else {
            this.getPersistentData().putString("CustomSkinPath", value);
        }
    }

    public String getSavedSkinInput() {
        return this.entityData.get(DATA_SAVED_SKIN_INPUT);
    }

    public void setSavedSkinInput(String input) {
        String value = input == null ? "" : input;
        this.entityData.set(DATA_SAVED_SKIN_INPUT, value);
        if (value.isEmpty()) {
            this.getPersistentData().remove("SavedSkinInput");
        } else {
            this.getPersistentData().putString("SavedSkinInput", value);
        }
    }

    public int getVisualHeight() {
        return this.entityData.get(DATA_HEIGHT);
    }

    public void setVisualHeight(int value) {
        int clamped = Math.max(50, Math.min(150, value));
        this.entityData.set(DATA_HEIGHT, clamped);
        this.getPersistentData().putInt("Height", clamped);
    }

    public int getVisualWidth() {
        return this.entityData.get(DATA_WIDTH);
    }

    public void setVisualWidth(int value) {
        int clamped = Math.max(50, Math.min(150, value));
        this.entityData.set(DATA_WIDTH, clamped);
        this.getPersistentData().putInt("Width", clamped);
    }

    @Override
    public VillagerCombatMode getCombatMode() {
        return VillagerCombatMode.fromName(this.entityData.get(DATA_COMBAT_MODE));
    }

    @Override
    public void setCombatMode(VillagerCombatMode combatMode) {
        VillagerCombatMode resolvedCombatMode = combatMode == null ? VillagerCombatMode.AGGRESSIVE : combatMode;
        this.entityData.set(DATA_COMBAT_MODE, resolvedCombatMode.name());
        this.getPersistentData().putString("SlimPatchCombatMode", resolvedCombatMode.name());
    }

    @Override
    public VillagerFollowMode getFollowMode() {
        return VillagerFollowMode.fromName(this.entityData.get(DATA_FOLLOW_MODE));
    }

    @Override
    public void setFollowMode(VillagerFollowMode followMode) {
        VillagerFollowMode resolvedFollowMode = followMode == null ? VillagerFollowMode.CLOSE : followMode;
        this.entityData.set(DATA_FOLLOW_MODE, resolvedFollowMode.name());
        this.getPersistentData().putString("SlimPatchFollowMode", resolvedFollowMode.name());
    }

    @Override
    public boolean isArmorHidden() {
        return this.entityData.get(DATA_HIDE_ARMOR);
    }

    @Override
    public void setArmorHidden(boolean hidden) {
        this.entityData.set(DATA_HIDE_ARMOR, hidden);
        this.getPersistentData().putBoolean("SlimPatchHideArmor", hidden);
    }

    @Override
    public boolean isMuted() {
        return this.entityData.get(DATA_MUTED);
    }

    @Override
    public void setMuted(boolean muted) {
        this.entityData.set(DATA_MUTED, muted);
        this.getPersistentData().putBoolean("SlimPatchMuted", muted);
    }

    @Override
    public float getGoldenRelationship() {
        return this.entityData.get(DATA_GOLDEN_RELATIONSHIP);
    }

    @Override
    public void setGoldenRelationship(float value) {
        float clamped = VillagerFamilyData.clampGoldenRelationship(value);
        this.entityData.set(DATA_GOLDEN_RELATIONSHIP, clamped);
        this.getPersistentData().putFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG, clamped);
        if (clamped <= 0.0F && this.getRelationshipStage() == VillagerRelationshipStage.DATING) {
            this.setRelationshipStage(VillagerRelationshipStage.FRIENDSHIP);
        }
    }

    @Override
    public UUID getSpousePlayerUuid() {
        return this.spousePlayerUuid;
    }

    @Override
    public void setSpousePlayerUuid(UUID uuid) {
        this.spousePlayerUuid = uuid;
        VillagerFamilyData.writeSpousePlayerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getSpousePlayerName() {
        return this.entityData.get(DATA_SPOUSE_PLAYER_NAME);
    }

    @Override
    public void setSpousePlayerName(String name) {
        String value = name == null ? "" : name;
        this.entityData.set(DATA_SPOUSE_PLAYER_NAME, value);
        if (value.isEmpty()) {
            this.getPersistentData().remove(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG);
        } else {
            this.getPersistentData().putString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG, value);
        }
    }

    @Override
    public UUID getFormerSpousePlayerUuid() {
        return this.formerSpousePlayerUuid;
    }

    @Override
    public void setFormerSpousePlayerUuid(UUID uuid) {
        this.formerSpousePlayerUuid = uuid;
        VillagerFamilyData.writeFormerSpousePlayerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getFormerSpousePlayerName() {
        return this.entityData.get(DATA_FORMER_SPOUSE_PLAYER_NAME);
    }

    @Override
    public void setFormerSpousePlayerName(String name) {
        String value = name == null ? "" : name;
        this.entityData.set(DATA_FORMER_SPOUSE_PLAYER_NAME, value);
        if (value.isEmpty()) {
            this.getPersistentData().remove(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG);
        } else {
            this.getPersistentData().putString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG, value);
        }
    }

    @Override
    public UUID getSpouseVillagerUuid() {
        return this.spouseVillagerUuid;
    }

    @Override
    public void setSpouseVillagerUuid(UUID uuid) {
        this.spouseVillagerUuid = uuid;
        VillagerFamilyData.writeSpouseVillagerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getSpouseVillagerName() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.SPOUSE_VILLAGER_NAME_TAG);
    }

    @Override
    public void setSpouseVillagerName(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.SPOUSE_VILLAGER_NAME_TAG, name);
    }

    @Override
    public VillagerRelationshipStage getRelationshipStage() {
        return VillagerFamilyData.parseRelationshipStage(this.entityData.get(DATA_RELATIONSHIP_STAGE));
    }

    @Override
    public void setRelationshipStage(VillagerRelationshipStage relationshipStage) {
        VillagerRelationshipStage resolvedRelationshipStage = relationshipStage == null ? VillagerRelationshipStage.FRIENDSHIP : relationshipStage;
        this.entityData.set(DATA_RELATIONSHIP_STAGE, resolvedRelationshipStage.name());
        this.getPersistentData().putString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG, resolvedRelationshipStage.name());
    }

    @Override
    public VillagerAgeStage getAgeStage() {
        return VillagerFamilyData.parseAgeStage(this.entityData.get(DATA_AGE_STAGE));
    }

    @Override
    public void setAgeStage(VillagerAgeStage ageStage) {
        VillagerAgeStage resolvedAgeStage = ageStage == null ? VillagerAgeStage.ADULT : ageStage;
        this.entityData.set(DATA_AGE_STAGE, resolvedAgeStage.name());
        this.getPersistentData().putString(VillagerFamilyData.AGE_STAGE_TAG, resolvedAgeStage.name());
        this.refreshDimensions();
        if (!VillagerFamilyData.canHaveRomanticPersonality(this) && this.getPersonality() == VillagerPersonality.ROMANTIC) {
            this.setPersonality(VillagerPersonality.FRIENDLY);
        }
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            FamilyTreeTracker.onVillagerAgeStageChanged(serverLevel.getServer(), this);
        }
    }

    @Override
    public long getAgeStageStartedAt() {
        return this.getPersistentData().contains(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                ? this.getPersistentData().getLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                : 0L;
    }

    @Override
    public void setAgeStageStartedAt(long startedAt) {
        if (startedAt <= 0L) {
            this.getPersistentData().remove(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG);
        } else {
            this.getPersistentData().putLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG, startedAt);
        }
    }

    @Override
    public long getNextAgeStageAt() {
        return this.getPersistentData().contains(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                ? this.getPersistentData().getLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                : 0L;
    }

    @Override
    public void setNextAgeStageAt(long nextAgeStageAt) {
        if (nextAgeStageAt <= 0L) {
            this.getPersistentData().remove(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG);
        } else {
            this.getPersistentData().putLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG, nextAgeStageAt);
        }
    }

    @Override
    public boolean isExpectingChild() {
        return this.entityData.get(DATA_EXPECTING_CHILD);
    }

    @Override
    public void setExpectingChild(boolean expectingChild) {
        this.entityData.set(DATA_EXPECTING_CHILD, expectingChild);
        this.getPersistentData().putBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG, expectingChild);
        if (!expectingChild) {
            this.setExpectingParentPlayerUuid(null);
            this.setExpectingParentPlayerName("");
            this.setExpectingOtherParentVillagerUuid(null);
            this.setExpectingOtherParentVillagerName("");
            this.setForcedBabyGender("");
            this.setExpectingStartedAt(0L);
            this.setExpectingEndsAt(0L);
        }
    }

    @Override
    public UUID getExpectingParentPlayerUuid() {
        return this.expectingParentPlayerUuid;
    }

    @Override
    public void setExpectingParentPlayerUuid(UUID uuid) {
        this.expectingParentPlayerUuid = uuid;
        VillagerFamilyData.writeExpectingParentPlayerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getExpectingParentPlayerName() {
        return this.getPersistentData().contains(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                ? this.getPersistentData().getString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                : "";
    }

    @Override
    public void setExpectingParentPlayerName(String name) {
        String value = name == null ? "" : name;
        if (value.isEmpty()) {
            this.getPersistentData().remove(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG);
        } else {
            this.getPersistentData().putString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG, value);
        }
    }

    @Override
    public UUID getExpectingOtherParentVillagerUuid() {
        return this.expectingOtherParentVillagerUuid;
    }

    @Override
    public void setExpectingOtherParentVillagerUuid(UUID uuid) {
        this.expectingOtherParentVillagerUuid = uuid;
        VillagerFamilyData.writeExpectingOtherParentVillagerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getExpectingOtherParentVillagerName() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG);
    }

    @Override
    public void setExpectingOtherParentVillagerName(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG, name);
    }

    @Override
    public String getForcedBabyGender() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.FORCED_BABY_GENDER_TAG);
    }

    @Override
    public void setForcedBabyGender(String gender) {
        String value = gender == null || gender.isEmpty() ? "" : BirthScreenData.normalizeGender(gender);
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.FORCED_BABY_GENDER_TAG, value);
    }

    @Override
    public long getExpectingStartedAt() {
        return this.getPersistentData().contains(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                ? this.getPersistentData().getLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                : 0L;
    }

    @Override
    public void setExpectingStartedAt(long startedAt) {
        if (startedAt <= 0L) {
            this.getPersistentData().remove(VillagerFamilyData.EXPECTING_STARTED_AT_TAG);
        } else {
            this.getPersistentData().putLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG, startedAt);
        }
    }

    @Override
    public long getExpectingEndsAt() {
        return this.getPersistentData().contains(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                ? this.getPersistentData().getLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                : 0L;
    }

    @Override
    public void setExpectingEndsAt(long endsAt) {
        if (endsAt <= 0L) {
            this.getPersistentData().remove(VillagerFamilyData.EXPECTING_ENDS_AT_TAG);
        } else {
            this.getPersistentData().putLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG, endsAt);
        }
    }

    @Override
    public boolean isBornFromFamilySystem() {
        return this.entityData.get(DATA_BORN_FROM_FAMILY_SYSTEM);
    }

    @Override
    public void setBornFromFamilySystem(boolean bornFromFamilySystem) {
        this.entityData.set(DATA_BORN_FROM_FAMILY_SYSTEM, bornFromFamilySystem);
        this.getPersistentData().putBoolean(VillagerFamilyData.BORN_FROM_FAMILY_SYSTEM_TAG, bornFromFamilySystem);
    }

    @Override
    public UUID getParentPlayerUuid() {
        String parentPlayerUuid = this.entityData.get(DATA_PARENT_PLAYER_UUID);
        if (parentPlayerUuid.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(parentPlayerUuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void setParentPlayerUuid(UUID uuid) {
        this.parentPlayerUuid = uuid;
        this.entityData.set(DATA_PARENT_PLAYER_UUID, uuid == null ? "" : uuid.toString());
        VillagerFamilyData.writeParentPlayerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getParentPlayerName() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_PLAYER_NAME_TAG);
    }

    @Override
    public void setParentPlayerName(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_PLAYER_NAME_TAG, name);
    }

    @Override
    public UUID getParentVillagerUuid() {
        return this.parentVillagerUuid;
    }

    @Override
    public void setParentVillagerUuid(UUID uuid) {
        this.parentVillagerUuid = uuid;
        VillagerFamilyData.writeParentVillagerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getParentVillagerName() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_VILLAGER_NAME_TAG);
    }

    @Override
    public void setParentVillagerName(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_VILLAGER_NAME_TAG, name);
    }

    @Override
    public UUID getParentVillager2Uuid() {
        return this.parentVillager2Uuid;
    }

    @Override
    public void setParentVillager2Uuid(UUID uuid) {
        this.parentVillager2Uuid = uuid;
        VillagerFamilyData.writeParentVillager2Uuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getParentVillager2Name() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_VILLAGER_2_NAME_TAG);
    }

    @Override
    public void setParentVillager2Name(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.PARENT_VILLAGER_2_NAME_TAG, name);
    }

    @Override
    public UUID getNaturalFamilyGroupId() {
        return this.naturalFamilyGroupId;
    }

    @Override
    public void setNaturalFamilyGroupId(UUID uuid) {
        this.naturalFamilyGroupId = uuid;
        VillagerFamilyData.writeNaturalFamilyGroupId(this.getPersistentData(), uuid);
    }

    @Override
    public boolean isNaturalFamilyMember() {
        return this.getPersistentData().getBoolean(VillagerFamilyData.NATURAL_FAMILY_MEMBER_TAG);
    }

    @Override
    public void setNaturalFamilyMember(boolean naturalFamilyMember) {
        this.getPersistentData().putBoolean(VillagerFamilyData.NATURAL_FAMILY_MEMBER_TAG, naturalFamilyMember);
    }

    @Override
    public UUID getFamilyOwnerPlayerUuid() {
        return this.familyOwnerPlayerUuid;
    }

    @Override
    public void setFamilyOwnerPlayerUuid(UUID uuid) {
        this.familyOwnerPlayerUuid = uuid;
        VillagerFamilyData.writeFamilyOwnerPlayerUuid(this.getPersistentData(), uuid);
    }

    @Override
    public String getFamilyOwnerPlayerName() {
        return VillagerFamilyData.readOptionalString(this.getPersistentData(), VillagerFamilyData.FAMILY_OWNER_PLAYER_NAME_TAG);
    }

    @Override
    public void setFamilyOwnerPlayerName(String name) {
        VillagerFamilyData.writeOptionalString(this.getPersistentData(), VillagerFamilyData.FAMILY_OWNER_PLAYER_NAME_TAG, name);
    }

    private void loadFamilyChildData(CompoundTag tag) {
        this.setBornFromFamilySystem(tag.getBoolean(VillagerFamilyData.BORN_FROM_FAMILY_SYSTEM_TAG));
        this.setParentPlayerUuid(VillagerFamilyData.readParentPlayerUuid(tag));
        this.setParentPlayerName(VillagerFamilyData.readOptionalString(tag, VillagerFamilyData.PARENT_PLAYER_NAME_TAG));
        this.setParentVillagerUuid(VillagerFamilyData.readParentVillagerUuid(tag));
        this.setParentVillagerName(VillagerFamilyData.readOptionalString(tag, VillagerFamilyData.PARENT_VILLAGER_NAME_TAG));
        this.setParentVillager2Uuid(VillagerFamilyData.readParentVillager2Uuid(tag));
        this.setParentVillager2Name(VillagerFamilyData.readOptionalString(tag, VillagerFamilyData.PARENT_VILLAGER_2_NAME_TAG));
        this.setSpouseVillagerUuid(VillagerFamilyData.readSpouseVillagerUuid(tag));
        this.setSpouseVillagerName(VillagerFamilyData.readOptionalString(tag, VillagerFamilyData.SPOUSE_VILLAGER_NAME_TAG));
        this.setFamilyOwnerPlayerUuid(VillagerFamilyData.readFamilyOwnerPlayerUuid(tag));
        this.setFamilyOwnerPlayerName(VillagerFamilyData.readOptionalString(tag, VillagerFamilyData.FAMILY_OWNER_PLAYER_NAME_TAG));
        this.setNaturalFamilyGroupId(VillagerFamilyData.readNaturalFamilyGroupId(tag));
        this.setNaturalFamilyMember(tag.getBoolean(VillagerFamilyData.NATURAL_FAMILY_MEMBER_TAG));
    }

    private void saveFamilyChildData(CompoundTag tag) {
        if (this.isBornFromFamilySystem()) {
            tag.putBoolean(VillagerFamilyData.BORN_FROM_FAMILY_SYSTEM_TAG, true);
        }
        VillagerFamilyData.writeParentPlayerUuid(tag, this.getParentPlayerUuid());
        VillagerFamilyData.writeOptionalString(tag, VillagerFamilyData.PARENT_PLAYER_NAME_TAG, this.getParentPlayerName());
        VillagerFamilyData.writeParentVillagerUuid(tag, this.getParentVillagerUuid());
        VillagerFamilyData.writeOptionalString(tag, VillagerFamilyData.PARENT_VILLAGER_NAME_TAG, this.getParentVillagerName());
        VillagerFamilyData.writeParentVillager2Uuid(tag, this.getParentVillager2Uuid());
        VillagerFamilyData.writeOptionalString(tag, VillagerFamilyData.PARENT_VILLAGER_2_NAME_TAG, this.getParentVillager2Name());
        VillagerFamilyData.writeSpouseVillagerUuid(tag, this.getSpouseVillagerUuid());
        VillagerFamilyData.writeOptionalString(tag, VillagerFamilyData.SPOUSE_VILLAGER_NAME_TAG, this.getSpouseVillagerName());
        VillagerFamilyData.writeFamilyOwnerPlayerUuid(tag, this.getFamilyOwnerPlayerUuid());
        VillagerFamilyData.writeOptionalString(tag, VillagerFamilyData.FAMILY_OWNER_PLAYER_NAME_TAG, this.getFamilyOwnerPlayerName());
        VillagerFamilyData.writeNaturalFamilyGroupId(tag, this.getNaturalFamilyGroupId());
        tag.putBoolean(VillagerFamilyData.NATURAL_FAMILY_MEMBER_TAG, this.isNaturalFamilyMember());
    }

    @Override
    public VillagerCommandState getCommandState() {
        try {
            return VillagerCommandState.valueOf(this.entityData.get(DATA_COMMAND_STATE));
        } catch (IllegalArgumentException e) {
            return VillagerCommandState.NONE;
        }
    }

    @Override
    public void setCommandState(VillagerCommandState commandState) {
        this.commandState = commandState == null ? VillagerCommandState.NONE : commandState;
        this.entityData.set(DATA_COMMAND_STATE, this.commandState.name());
    }

    @Override
    public UUID getCommandTargetUuid() {
        return this.commandTargetUuid;
    }

    @Override
    public void setCommandTargetUuid(UUID commandTargetUuid) {
        this.commandTargetUuid = commandTargetUuid;
    }

    @Override
    public UUID getCommandOwnerUuid() {
        return this.commandOwnerUuid;
    }

    @Override
    public void setCommandOwnerUuid(UUID commandOwnerUuid) {
        this.commandOwnerUuid = commandOwnerUuid;
        this.entityData.set(DATA_COMMAND_OWNER_UUID, commandOwnerUuid == null ? "" : commandOwnerUuid.toString());
    }

    @Override
    public String getCommandOwnerName() {
        return this.entityData.get(DATA_COMMAND_OWNER_NAME);
    }

    @Override
    public void setCommandOwnerName(String commandOwnerName) {
        this.entityData.set(DATA_COMMAND_OWNER_NAME, commandOwnerName == null ? "" : commandOwnerName);
    }

    @Override
    public BlockPos getStayPos() {
        return this.stayPos;
    }

    @Override
    public void setStayPos(BlockPos stayPos) {
        this.stayPos = stayPos;
    }

    @Override
    public Vec3 getStayAnchorPos() {
        return this.stayAnchorPos;
    }

    @Override
    public void setStayAnchorPos(Vec3 stayAnchorPos) {
        this.stayAnchorPos = stayAnchorPos;
    }

    @Override
    public boolean hasHome() {
        return this.entityData.get(DATA_HAS_HOME);
    }

    @Override
    public void setHome(BlockPos homePos, ResourceKey<Level> homeDimension) {
        this.homePos = homePos;
        this.homeDimension = homeDimension;
        this.entityData.set(DATA_HAS_HOME, homePos != null && homeDimension != null);
    }

    @Override
    public void clearHome() {
        this.homePos = null;
        this.homeDimension = null;
        this.entityData.set(DATA_HAS_HOME, false);
    }

    @Override
    public BlockPos getHomePos() {
        return this.homePos;
    }

    @Override
    public ResourceKey<Level> getHomeDimension() {
        return this.homeDimension;
    }

    @Override
    public long getLastCommandTeleportTick() {
        return this.lastCommandTeleportTick;
    }

    @Override
    public void setLastCommandTeleportTick(long tick) {
        this.lastCommandTeleportTick = tick;
    }

    @Override
    public int getCombatLockTicks() {
        return this.combatLockTicks;
    }

    @Override
    public void setCombatLockTicks(int ticks) {
        this.combatLockTicks = ticks;
    }

    @Override
    public int getLastDamageTick() {
        return this.lastDamageTick;
    }

    @Override
    public void setLastDamageTick(int tick) {
        this.lastDamageTick = tick;
    }

    @Override
    public int getLastAggressiveScanTick() {
        return this.lastAggressiveScanTick;
    }

    @Override
    public void setLastAggressiveScanTick(int tick) {
        this.lastAggressiveScanTick = tick;
    }

    @Override
    public int getLastBowAttackTick() {
        return this.lastBowAttackTick;
    }

    @Override
    public void setLastBowAttackTick(int tick) {
        this.lastBowAttackTick = tick;
    }

    @Override
    public int getBowChargeStartTick() {
        return this.bowChargeStartTick;
    }

    @Override
    public void setBowChargeStartTick(int tick) {
        this.bowChargeStartTick = tick;
    }

    @Override
    public int getLastCrossbowAttackTick() {
        return this.lastCrossbowAttackTick;
    }

    @Override
    public void setLastCrossbowAttackTick(int tick) {
        this.lastCrossbowAttackTick = tick;
    }

    @Override
    public int getCrossbowChargeStartTick() {
        return this.crossbowChargeStartTick;
    }

    @Override
    public void setCrossbowChargeStartTick(int tick) {
        this.crossbowChargeStartTick = tick;
    }

    @Override
    public int getLastShieldBlockTick() {
        return this.lastShieldBlockTick;
    }

    @Override
    public void setLastShieldBlockTick(int tick) {
        this.lastShieldBlockTick = tick;
    }

    @Override
    public int getShieldRaiseUntilTick() {
        return this.shieldRaiseUntilTick;
    }

    @Override
    public void setShieldRaiseUntilTick(int tick) {
        this.shieldRaiseUntilTick = tick;
    }

    @Override
    public float getBonusHealth() {
        return this.bonusHealth;
    }

    @Override
    public void setBonusHealth(float bonusHealth) {
        this.bonusHealth = Math.max(0.0F, bonusHealth);
        this.getPersistentData().putFloat(VillagerHealthHandler.BONUS_HEALTH_TAG, this.bonusHealth);
    }

    @Override
    public double getBaseMaxHealth() {
        return this.baseMaxHealth;
    }

    @Override
    public void setBaseMaxHealth(double baseMaxHealth) {
        this.baseMaxHealth = Math.max(1.0D, baseMaxHealth);
    }

    @Override
    public SimpleContainer getEquipmentInventory() {
        return this.equipmentInventory;
    }

    @Override
    public ItemStack getPersistentMainHandItem() {
        return this.persistentMainHandItem.copy();
    }

    @Override
    public void setPersistentMainHandItem(ItemStack stack) {
        this.persistentMainHandItem = stack.copy();
        if (!this.level().isClientSide) {
            if (this.persistentMainHandItem.isEmpty()) {
                this.getPersistentData().remove("PersistentMainHand");
            } else {
                this.getPersistentData().put("PersistentMainHand", this.persistentMainHandItem.save(this.registryAccess()));
            }
        }
    }

    @Override
    public void syncPersistentMainHand() {
        if (!ItemStack.matches(this.getMainHandItem(), this.persistentMainHandItem)) {
            super.setItemInHand(InteractionHand.MAIN_HAND, this.persistentMainHandItem.copy());
        }
    }

    public void applyRelationshipChange(String option) {
        applyRelationshipChange(option, com.javic.slimpatch.dialogue.DialogueManager.calculateSuccess(getPersonality(), option));
    }

    public void applyRelationshipChange(String option, boolean success) {
        float delta = com.javic.slimpatch.dialogue.DialogueManager.getRelationshipChange(getPersonality(), option, success);
        setRelationship(getRelationship() + delta);
    }

    public void applyRelationshipChange(net.minecraft.server.level.ServerPlayer player, String option) {
        applyRelationshipChange(player, option, com.javic.slimpatch.dialogue.DialogueManager.calculateSuccess(getPersonality(), option));
    }

    public void applyRelationshipChange(net.minecraft.server.level.ServerPlayer player, String option, boolean success) {
        if (option.equalsIgnoreCase("Flirt") && !VillagerFamilyData.canUseRomanticInteraction(this, player)) {
            return;
        }
        if (VillagerCooldownData.isDialogueOptionOnCooldown(this, player.getUUID(), option)) {
            return;
        }

        VillagerCooldownData.setDialogueCooldown(this, player.getUUID(), option, OPTION_COOLDOWNS.getOrDefault(option, 20_000L));
        VillagerCooldownData.syncDialogueCooldownsToPlayer(this, player, OPTION_COOLDOWNS);

        VillagerPersonality personality = getPersonality();
        boolean usePerPlayerGolden = VillagerRelationshipData.usesPerPlayerRelationships(this);
        float goldenRelationship = VillagerRelationshipData.getGoldenRelationshipForPlayer(this, player.getUUID(), this.getGoldenRelationship());
        boolean isDatingForPlayer = usePerPlayerGolden ? goldenRelationship > 0.0F : this.getRelationshipStage() == VillagerRelationshipStage.DATING;
        if (isDatingForPlayer && option.equalsIgnoreCase("Flirt")) {
            float goldenDelta = success ? 0.5F : -0.25F;
            float updatedGoldenRelationship = usePerPlayerGolden
                    ? VillagerRelationshipData.setGoldenRelationshipForPlayer(this, player.getUUID(), goldenRelationship + goldenDelta, this.getGoldenRelationship())
                    : updateGlobalGoldenRelationship(goldenDelta);
            ModNetworking.sendToClient(new RelationshipSyncPacket(this.getId(), VillagerRelationshipData.getRelationshipForPlayer(this, player.getUUID(), getRelationship()), updatedGoldenRelationship), player);
            if (goldenDelta > 0.0F) {
                playPositiveReactionFeedback();
            } else if (goldenDelta < 0.0F) {
                playNegativeReactionFeedback();
            }
            return;
        }
        float delta = com.javic.slimpatch.dialogue.DialogueManager.getRelationshipChange(personality, option, success);
        float updatedGoldenRelationship = usePerPlayerGolden ? goldenRelationship : this.getGoldenRelationship();
        if (isDatingForPlayer && option.equalsIgnoreCase("Mean") && !success) {
            updatedGoldenRelationship = usePerPlayerGolden
                    ? VillagerRelationshipData.setGoldenRelationshipForPlayer(this, player.getUUID(), goldenRelationship - 0.25F, this.getGoldenRelationship())
                    : updateGlobalGoldenRelationship(-0.25F);
        }
        float relationship;
        if (VillagerRelationshipData.usesPerPlayerRelationships(this)) {
            float old = VillagerRelationshipData.getRelationshipForPlayer(this, player.getUUID(), getRelationship());
            relationship = VillagerRelationshipData.setRelationshipForPlayer(this, player.getUUID(), old + delta, getRelationship());
        } else {
            float old = getRelationship();
            setRelationship(old + delta);
            relationship = getRelationship();
        }
        ModNetworking.sendToClient(new RelationshipSyncPacket(this.getId(), relationship, updatedGoldenRelationship), player);

        if (delta > 0) {
            playPositiveReactionFeedback();
        } else if (delta < 0) {
            playNegativeReactionFeedback();
        }
    }

    private void playPositiveReactionFeedback() {
        if (Config.CUSTOM_VILLAGER_SOUNDS.get()) {
            level().playSound(null, blockPosition(), com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch(this));
        }
        for (int i = 0; i < 7; i++) {
            ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    getX() + random.nextGaussian() * 0.3,
                    getY() + 1.0,
                    getZ() + random.nextGaussian() * 0.3,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void playNegativeReactionFeedback() {
        if (Config.CUSTOM_VILLAGER_SOUNDS.get()) {
            level().playSound(null, blockPosition(), com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionNegative(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch(this));
        }
        for (int i = 0; i < 4; i++) {
            ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    getX() + random.nextGaussian() * 0.3,
                    getY() + 1.0,
                    getZ() + random.nextGaussian() * 0.3,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private float updateGlobalGoldenRelationship(float delta) {
        this.setGoldenRelationship(this.getGoldenRelationship() + delta);
        return this.getGoldenRelationship();
    }

    private InteractionResult handleFlowerInteraction(Player player, ItemStack stack) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (!VillagerFamilyData.canUseRomanticInteraction(this, serverPlayer)) {
            playNegativeReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        float relationship = VillagerRelationshipData.getRelationshipForPlayer(this, serverPlayer.getUUID(), this.getRelationship());
        boolean usePerPlayerGolden = VillagerRelationshipData.usesPerPlayerRelationships(this);
        float goldenRelationship = VillagerRelationshipData.getGoldenRelationshipForPlayer(this, serverPlayer.getUUID(), this.getGoldenRelationship());
        boolean married = this.getRelationshipStage() == VillagerRelationshipStage.MARRIED || this.hasSpouse();
        if (married) {
            consumeFlowerIfNeeded(player, stack);
            playPositiveReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        if (usePerPlayerGolden ? goldenRelationship > 0.0F : this.getRelationshipStage() == VillagerRelationshipStage.DATING) {
            consumeFlowerIfNeeded(player, stack);
            playPositiveReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        if (!VillagerFamilyData.canStartDating(this, this, serverPlayer, relationship)) {
            playNegativeReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        consumeFlowerIfNeeded(player, stack);
        this.setRelationshipStage(VillagerRelationshipStage.DATING);
        float updatedGoldenRelationship = usePerPlayerGolden
                ? VillagerRelationshipData.setGoldenRelationshipForPlayer(this, serverPlayer.getUUID(), goldenRelationship + 0.5F, this.getGoldenRelationship())
                : updateGlobalGoldenRelationship(0.5F);
        ModNetworking.sendToClient(new RelationshipSyncPacket(this.getId(), relationship, updatedGoldenRelationship), serverPlayer);
        playPositiveReactionFeedback();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleBouquetInteraction(Player player, ItemStack stack) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (!VillagerFamilyData.canUseRomanticInteraction(this, serverPlayer)) {
            playNegativeReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        float relationship = VillagerRelationshipData.getRelationshipForPlayer(this, serverPlayer.getUUID(), this.getRelationship());
        boolean usePerPlayerGolden = VillagerRelationshipData.usesPerPlayerRelationships(this);
        float goldenRelationship = VillagerRelationshipData.getGoldenRelationshipForPlayer(this, serverPlayer.getUUID(), this.getGoldenRelationship());
        boolean married = this.getRelationshipStage() == VillagerRelationshipStage.MARRIED || this.hasSpouse();
        if (married) {
            consumeFlowerIfNeeded(player, stack);
            playPositiveReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        if (usePerPlayerGolden ? goldenRelationship > 0.0F : this.getRelationshipStage() == VillagerRelationshipStage.DATING) {
            consumeFlowerIfNeeded(player, stack);
            playPositiveReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        if (!VillagerFamilyData.canStartDating(this, this, serverPlayer, relationship)) {
            playNegativeReactionFeedback();
            return InteractionResult.SUCCESS;
        }

        consumeFlowerIfNeeded(player, stack);
        this.setRelationshipStage(VillagerRelationshipStage.DATING);
        float updatedGoldenRelationship = usePerPlayerGolden
                ? VillagerRelationshipData.setGoldenRelationshipForPlayer(this, serverPlayer.getUUID(), goldenRelationship + 1.5F, this.getGoldenRelationship())
                : updateGlobalGoldenRelationship(1.5F);
        ModNetworking.sendToClient(new RelationshipSyncPacket(this.getId(), relationship, updatedGoldenRelationship), serverPlayer);
        playPositiveReactionFeedback();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleWeddingRingInteraction(Player player, ItemStack stack) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        String failureMessage = WeddingRingHandler.getMarriageFailureMessage(this, this, serverPlayer);
        if (failureMessage != null) {
            serverPlayer.displayClientMessage(Component.translatable(failureMessage), true);
            return InteractionResult.SUCCESS;
        }

        ModNetworking.sendToClient(new com.javic.slimpatch.network.OpenMarriageProposalScreenPacket(this.getId(), this.getName().getString()), serverPlayer);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleDivorcePapersInteraction(Player player, ItemStack stack) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (this.getRelationshipStage() != VillagerRelationshipStage.MARRIED || !this.hasSpouse()) {
            serverPlayer.displayClientMessage(Component.translatable("slimpatch.message.divorce_requires_marriage"), true);
            return InteractionResult.SUCCESS;
        }
        if (!serverPlayer.getUUID().equals(this.getSpousePlayerUuid())) {
            serverPlayer.displayClientMessage(Component.translatable("slimpatch.message.divorce_not_spouse"), true);
            return InteractionResult.SUCCESS;
        }

        ModNetworking.sendToClient(new com.javic.slimpatch.network.OpenDivorceConfirmationScreenPacket(this.getId(), this.getName().getString()), serverPlayer);
        return InteractionResult.SUCCESS;
    }

    public void confirmMarriageProposal(net.minecraft.server.level.ServerPlayer player) {
        String failureMessage = WeddingRingHandler.getMarriageFailureMessage(this, this, player);
        if (failureMessage != null) {
            player.displayClientMessage(Component.translatable(failureMessage), true);
            return;
        }

        ItemStack ringStack = WeddingRingHandler.findWeddingRingStack(player);
        if (ringStack.isEmpty()) {
            return;
        }

        this.setRelationshipStage(VillagerRelationshipStage.MARRIED);
        this.setSpousePlayerUuid(player.getUUID());
        this.setSpousePlayerName(player.getGameProfile().getName());
        if (player.getUUID().equals(this.getFormerSpousePlayerUuid())) {
            this.setFormerSpousePlayerUuid(null);
            this.setFormerSpousePlayerName("");
        }
        if (!player.getAbilities().instabuild) {
            ringStack.consume(1, player);
        }
        SpouseCookingHandler.markMarriageCooldownUsed(this, player);
        playPositiveReactionFeedback();
        if (Config.ENABLE_WEDDING_CUTSCENE.get()) {
            WeddingCutsceneServerHandler.start(this, player);
        }
        FamilyTreeTracker.onMarriageToPlayer(player.serverLevel().getServer(), this, player.getUUID(), player.getGameProfile().getName());
        ModNetworking.sendToClient(new com.javic.slimpatch.network.StartWeddingCutscenePacket(this.getId(), this.getName().getString()), player);
    }

    public void confirmDivorce(net.minecraft.server.level.ServerPlayer player) {
        if (this.getRelationshipStage() != VillagerRelationshipStage.MARRIED || !this.hasSpouse()) {
            player.displayClientMessage(Component.translatable("slimpatch.message.divorce_requires_marriage"), true);
            return;
        }
        if (!player.getUUID().equals(this.getSpousePlayerUuid())) {
            player.displayClientMessage(Component.translatable("slimpatch.message.divorce_not_spouse"), true);
            return;
        }

        ItemStack divorcePapers = DivorcePapersHandler.findDivorcePapersStack(player);
        if (divorcePapers.isEmpty()) {
            return;
        }

        this.setFormerSpousePlayerUuid(player.getUUID());
        this.setFormerSpousePlayerName(player.getGameProfile().getName());
        this.setSpousePlayerUuid(null);
        this.setSpousePlayerName("");
        this.setRelationshipStage(VillagerRelationshipStage.FRIENDSHIP);
        this.setGoldenRelationship(0.0F);
        float relationship;
        float goldenRelationship;
        if (VillagerRelationshipData.usesPerPlayerRelationships(this)) {
            relationship = VillagerRelationshipData.setRelationshipForPlayer(this, player.getUUID(), 2.0F, this.getRelationship());
            goldenRelationship = VillagerRelationshipData.setGoldenRelationshipForPlayer(this, player.getUUID(), 0.0F, this.getGoldenRelationship());
        } else {
            this.setRelationship(2.0F);
            relationship = this.getRelationship();
            goldenRelationship = this.getGoldenRelationship();
        }
        ModNetworking.sendToClient(new RelationshipSyncPacket(this.getId(), relationship, goldenRelationship), player);
        if (!player.getAbilities().instabuild) {
            divorcePapers.consume(1, player);
        }
        FamilyTreeTracker.onDivorceFromPlayer(player.serverLevel().getServer(), this, player.getUUID(), player.getGameProfile().getName());
        player.displayClientMessage(Component.translatable("slimpatch.message.divorce_success", this.getName().getString()), true);
    }

    private static void consumeFlowerIfNeeded(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.consume(1, player);
        }
    }

    public void giveGiftToPlayer(net.minecraft.world.entity.player.Player player) {
        if (this.level().isClientSide) return;
        if (VillagerCooldownData.hasGiftCooldown(this, player.getUUID())) return;
        float relationship = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                ? VillagerRelationshipData.getRelationshipForPlayer(this, serverPlayer.getUUID(), this.getRelationship())
                : this.getRelationship();
        if (relationship < 5.0f) return;

        net.minecraft.world.item.ItemStack gift = com.javic.slimpatch.entity.GiftReactionHandler.getRandomGift(this.getPersonality(), this.getRandom());
        boolean added = player.addItem(gift);
        if (!added) this.spawnAtLocation(gift);
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);

        VillagerCooldownData.setGiftCooldown(this, player.getUUID());

        ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        if (Config.CUSTOM_VILLAGER_SOUNDS.get()) {
            this.level().playSound(null, this.blockPosition(),
                    com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch(this));
        }

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        VillagerCommandHandler.updateNavigationForCommand(this, this.getCommandState());
        this.goalSelector.addGoal(0, new DialogueGoal(this));
        this.goalSelector.addGoal(4, new ArmedVillagerMeleeAttackGoal(this, this, 0.75D));
        this.goalSelector.addGoal(5, new VillagerCommandGoal(this, this));
        this.goalSelector.addGoal(6, new ReturnHomeAtNightGoal(this, this));
    }

    @Override
    protected void customServerAiStep() {
        VillagerCommandHandler.suppressBedBehaviorForHomeAtNight(this, this);
        VillagerCombatHandler.tickCombat(this, this);
        if (WeddingCutsceneServerHandler.tick(this) || FamilyCutsceneServerHandler.tick(this)) {
            this.dialogueHoldPos = null;
        } else if (com.javic.slimpatch.dialogue.DialogueManager.isInDialogue(this)) {
            if (this.dialogueHoldPos == null) {
                this.dialogueHoldPos = this.position();
            }
            VillagerCommandHandler.holdForDialogue(this, this.dialogueHoldPos);
        } else if (this.getCommandState() != VillagerCommandState.NONE && !VillagerCombatHandler.isActivelyDefending(this, this)) {
            this.dialogueHoldPos = null;
            VillagerCommandHandler.tick(this, this);
        } else {
            this.dialogueHoldPos = null;
        }

        super.customServerAiStep();
        this.suppressRelaxedOwnerLookTarget();
    }

    private void suppressRelaxedOwnerLookTarget() {
        if (this.getCommandState() != VillagerCommandState.FOLLOW || this.getFollowMode() != VillagerFollowMode.RELAXED) {
            return;
        }
        if (this.isTrading() || com.javic.slimpatch.dialogue.DialogueManager.isInDialogue(this) || VillagerCombatHandler.isActivelyDefending(this, this)) {
            return;
        }
        UUID targetUuid = this.getCommandTargetUuid();
        if (targetUuid == null) {
            return;
        }
        Player player = this.level().getPlayerByUUID(targetUuid);
        if (player == null || !player.isAlive() || player.level() != this.level()) {
            return;
        }
        double distanceToPlayerSqr = this.distanceToSqr(player);
        if (distanceToPlayerSqr > RELAXED_OWNER_LOOK_SUPPRESS_DISTANCE_SQR) {
            return;
        }
        if (distanceToPlayerSqr > RELAXED_OWNER_LOOK_NEAR_DISTANCE_SQR && this.isOwnerMovingAwayClearly(player)) {
            return;
        }
        this.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(lookTarget -> {
            if (lookTarget instanceof EntityTracker entityTracker && entityTracker.getEntity() == player) {
                this.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            }
        });
    }

    private boolean isOwnerMovingAwayClearly(Player player) {
        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeedSqr = motion.x * motion.x + motion.z * motion.z;
        if (horizontalSpeedSqr < RELAXED_OWNER_LOOK_MOVE_AWAY_SPEED_SQR) {
            return false;
        }
        Vec3 toPlayer = player.position().subtract(this.position());
        double horizontalLength = Math.sqrt(toPlayer.x * toPlayer.x + toPlayer.z * toPlayer.z);
        if (horizontalLength < 1.0E-4D) {
            return false;
        }
        double dot = (motion.x * toPlayer.x + motion.z * toPlayer.z) / horizontalLength;
        return dot > RELAXED_OWNER_LOOK_MOVE_AWAY_DOT;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (VillagerCommandHandler.shouldIgnoreDamage(this, this, source)) {
            return false;
        }
        if (VillagerCombatHandler.tryBlockDamage(this, this, source, amount)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            VillagerPassiveHealHandler.recordDamage(this, this);
            VillagerCombatHandler.onVillagerHurt(this, this, source);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        this.dropBackpackAndEquipment();
    }

    private void dropBackpackAndEquipment() {
        for (int i = 0; i < this.equipmentInventory.getContainerSize(); i++) {
            ItemStack stack = this.equipmentInventory.getItem(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
                this.equipmentInventory.setItem(i, ItemStack.EMPTY);
            }
        }

        this.dropAndClearEquipmentSlot(EquipmentSlot.HEAD);
        this.dropAndClearEquipmentSlot(EquipmentSlot.CHEST);
        this.dropAndClearEquipmentSlot(EquipmentSlot.LEGS);
        this.dropAndClearEquipmentSlot(EquipmentSlot.FEET);
        this.dropAndClearOffhand();
        this.dropAndClearMainHand();
    }

    private void dropAndClearEquipmentSlot(EquipmentSlot slot) {
        ItemStack stack = this.getItemBySlot(slot);
        if (!stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
            this.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    private void dropAndClearOffhand() {
        ItemStack stack = this.getOffhandItem();
        if (!stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
            this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private void dropAndClearMainHand() {
        ItemStack stack = this.getMainHandItem();
        if (stack.isEmpty()) {
            stack = this.getPersistentMainHandItem();
        }
        if (!stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
        }
        super.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.setPersistentMainHandItem(ItemStack.EMPTY);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, SpawnGroupData spawnData) {
        SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        CompoundTag data = this.getPersistentData();

        if (reason == MobSpawnType.CONVERSION || data.getBoolean("slimpatch_restored") || data.contains("slimpatch_saved_data")) {
            if (data.contains("slimpatch_skin")) {
                this.setSkinIndex(data.getInt("slimpatch_skin"));
            }
            if (data.contains("slimpatch_personality")) {
                try {
                    this.setPersonality(VillagerPersonality.valueOf(data.getString("slimpatch_personality")));
                } catch (IllegalArgumentException e) {
                    this.setPersonality(VillagerPersonality.FRIENDLY);
                }
            }
            if (data.contains("slimpatch_relationship")) {
                this.setRelationship(data.getFloat("slimpatch_relationship"));
            }
            if (data.contains("SlimPatchCombatMode")) {
                this.setCombatMode(VillagerCombatMode.fromName(data.getString("SlimPatchCombatMode")));
            }
            if (data.contains("SlimPatchFollowMode")) {
                this.setFollowMode(VillagerFollowMode.fromName(data.getString("SlimPatchFollowMode")));
            }
            if (data.contains(VillagerHealthHandler.BONUS_HEALTH_TAG)) {
                this.setBonusHealth(data.getFloat(VillagerHealthHandler.BONUS_HEALTH_TAG));
                VillagerHealthHandler.reapplyBonusHealth(this, this);
            }
            this.setGoldenRelationship(data.contains(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                    ? data.getFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                    : 0.0F);
            this.setSpousePlayerUuid(VillagerFamilyData.readSpousePlayerUuid(data));
            this.setSpousePlayerName(data.contains(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                    ? data.getString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                    : "");
            this.setFormerSpousePlayerUuid(VillagerFamilyData.readFormerSpousePlayerUuid(data));
            this.setFormerSpousePlayerName(data.contains(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                    ? data.getString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                    : "");
            this.setRelationshipStage(VillagerFamilyData.parseRelationshipStage(data.contains(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                    ? data.getString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                    : null));
            this.setAgeStage(VillagerFamilyData.parseAgeStage(data.contains(VillagerFamilyData.AGE_STAGE_TAG)
                    ? data.getString(VillagerFamilyData.AGE_STAGE_TAG)
                    : null));
            this.setAgeStageStartedAt(data.contains(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                    ? data.getLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                    : 0L);
            this.setNextAgeStageAt(data.contains(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                    ? data.getLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                    : 0L);
            this.setExpectingChild(data.getBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG));
            this.setExpectingParentPlayerUuid(VillagerFamilyData.readExpectingParentPlayerUuid(data));
            this.setExpectingParentPlayerName(data.contains(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                    ? data.getString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                    : "");
            this.setExpectingStartedAt(data.contains(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                    ? data.getLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                    : 0L);
            this.setExpectingEndsAt(data.contains(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                    ? data.getLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                    : 0L);
            this.loadFamilyChildData(data);
            if (data.contains("SavedName") && !data.getString("SavedName").isEmpty()) {
                this.setCustomName(Component.literal(data.getString("SavedName")));
                this.setCustomNameVisible(true);
            }

            return groupData;
        }

        if (!data.contains("slimpatch_initialized")) {
            data.putString("slimpatch_gender", "male");
            data.putBoolean("slimpatch_forced", true);

            if (!data.contains("slimpatch_skin")) {
                int skinIndex = this.getRandom().nextInt(MALE_SKINS) + 1;
                this.setSkinIndex(skinIndex);
                data.putInt("slimpatch_skin", skinIndex);
            } else {
                this.setSkinIndex(data.getInt("slimpatch_skin"));
            }

            if (!this.hasCustomName()) {
                String chosenName = VillagerNameConfig.getRandomMaleName(this.getRandom());
                this.setCustomName(Component.literal(chosenName));
                this.setCustomNameVisible(true);
            }

            if (!data.contains("slimpatch_personality")) {
                this.setPersonality(VillagerPersonality.getRandom(this.getRandom()));
            } else {
                try {
                    this.setPersonality(VillagerPersonality.valueOf(data.getString("slimpatch_personality")));
                } catch (IllegalArgumentException e) {
                    this.setPersonality(VillagerPersonality.getRandom(this.getRandom()));
                }
            }

            this.setRelationship(0.5f);
            data.putBoolean("slimpatch_named", true);
            data.putBoolean("slimpatch_initialized", true);
        }

        NaturalFamilySpawnHandler.maybeSpawnNaturalFamily(this, this, level, reason);
        return groupData;
    }

    @Override
    public boolean shouldShowName() {
        if (!Config.VILLAGER_NAME_TAG.get()) {
            return false;
        }
        Player nearestPlayer = this.level().getNearestPlayer(this, 8.0);
        return nearestPlayer != null && this.hasCustomName();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult healthResult = VillagerHealthHandler.tryHandleGoldenCarrotInteraction(this, this, player, hand);
        if (healthResult != InteractionResult.PASS) {
            return healthResult;
        }
        if (DivorcePapersHandler.isDivorcePapers(player.getItemInHand(hand))) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return handleDivorcePapersInteraction(player, player.getItemInHand(hand));
        }
        if (hand == InteractionHand.MAIN_HAND && WeddingRingHandler.isWeddingRing(player.getItemInHand(hand))) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return handleWeddingRingInteraction(player, player.getItemInHand(hand));
        }
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(ItemTags.FLOWERS)) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return handleFlowerInteraction(player, player.getItemInHand(hand));
        }
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(com.javic.slimpatch.item.ModItems.ROMANTIC_BOUQUET.get())) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return handleBouquetInteraction(player, player.getItemInHand(hand));
        }
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(com.javic.slimpatch.item.ModItems.FAMILY_CHARM.get())) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                return FamilyCharmHandler.handleUseOnVillager(serverPlayer, player.getItemInHand(hand), this);
            }
            return InteractionResult.SUCCESS;
        }
        if (hand == InteractionHand.MAIN_HAND && (player.getItemInHand(hand).is(com.javic.slimpatch.item.ModItems.SUN_CHARM.get())
                || player.getItemInHand(hand).is(com.javic.slimpatch.item.ModItems.MOON_CHARM.get()))) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                return BabyGenderCharmHandler.handleUseOnVillager(serverPlayer, player.getItemInHand(hand), this);
            }
            return InteractionResult.SUCCESS;
        }
        if (hand == InteractionHand.MAIN_HAND && (VillagerRelationshipData.usesPerPlayerRelationships(this)
                ? VillagerRelationshipData.getGoldenRelationshipForPlayer(this, player.getUUID(), this.getGoldenRelationship()) > 0.0F
                : this.getRelationshipStage() == VillagerRelationshipStage.DATING)
                && RomanticGiftHandler.isRomanticGift(player.getItemInHand(hand))) {
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                return RomanticGiftHandler.handleGift(this, this, serverPlayer, player.getItemInHand(hand), this::playPositiveReactionFeedback);
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND && Config.CUSTOM_VILLAGER_SOUNDS.get()) {
            this.level().playSound(null, this.blockPosition(),
                    HumanVillagerSounds.maleClick(), SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch(this));
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public float getVoicePitch() {
        return VillagerFamilyData.getAgeStagePitch(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Config.CUSTOM_VILLAGER_SOUNDS.get() ? (this.isMuted() ? SoundEvents.EMPTY : HumanVillagerSounds.maleClick()) : super.getAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return Config.CUSTOM_VILLAGER_SOUNDS.get() ? HumanVillagerSounds.maleClick() : super.getNotifyTradeSound();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return Config.CUSTOM_VILLAGER_SOUNDS.get() ? HumanVillagerSounds.maleHurt() : super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Config.CUSTOM_VILLAGER_SOUNDS.get() ? HumanVillagerSounds.maleDeath() : super.getDeathSound();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("slimpatch_skin")) {
            this.setSkinIndex(tag.getInt("slimpatch_skin"));
        } else {
            int newSkin = this.getRandom().nextInt(MALE_SKINS) + 1;
            this.setSkinIndex(newSkin);
        }

        if (tag.contains("slimpatch_personality")) {
            try {
                this.setPersonality(VillagerPersonality.valueOf(tag.getString("slimpatch_personality")));
            } catch (IllegalArgumentException e) {
                this.setPersonality(VillagerPersonality.getRandom(this.getRandom()));
            }
        }

        if (tag.contains("slimpatch_relationship")) {
            this.setRelationship(tag.getFloat("slimpatch_relationship"));
        } else {
            this.setRelationship(0.5f);
        }
        this.setGoldenRelationship(tag.contains(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                ? tag.getFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                : 0.0F);
        this.setSpousePlayerUuid(VillagerFamilyData.readSpousePlayerUuid(tag));
        this.setSpousePlayerName(tag.contains(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                ? tag.getString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                : "");
        this.setFormerSpousePlayerUuid(VillagerFamilyData.readFormerSpousePlayerUuid(tag));
        this.setFormerSpousePlayerName(tag.contains(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                ? tag.getString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                : "");
        this.setRelationshipStage(VillagerFamilyData.parseRelationshipStage(tag.contains(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                ? tag.getString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                : null));
        this.setAgeStage(VillagerFamilyData.parseAgeStage(tag.contains(VillagerFamilyData.AGE_STAGE_TAG)
                ? tag.getString(VillagerFamilyData.AGE_STAGE_TAG)
                : null));
        this.setAgeStageStartedAt(tag.contains(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                ? tag.getLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                : 0L);
        this.setNextAgeStageAt(tag.contains(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                ? tag.getLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                : 0L);
        this.setExpectingChild(tag.getBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG));
        this.setExpectingParentPlayerUuid(VillagerFamilyData.readExpectingParentPlayerUuid(tag));
        this.setExpectingParentPlayerName(tag.contains(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                ? tag.getString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                : "");
        this.setExpectingOtherParentVillagerUuid(VillagerFamilyData.readExpectingOtherParentVillagerUuid(tag));
        this.setExpectingOtherParentVillagerName(tag.contains(VillagerFamilyData.EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG)
                ? tag.getString(VillagerFamilyData.EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG)
                : "");
        this.setForcedBabyGender(tag.contains(VillagerFamilyData.FORCED_BABY_GENDER_TAG)
                ? tag.getString(VillagerFamilyData.FORCED_BABY_GENDER_TAG)
                : "");
        this.setExpectingStartedAt(tag.contains(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                ? tag.getLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                : 0L);
        this.setExpectingEndsAt(tag.contains(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                ? tag.getLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                : 0L);
        this.loadFamilyChildData(tag);

        if (tag.contains("HasQuest")) {
            this.setHasQuest(tag.getBoolean("HasQuest"));
        }

        if (tag.contains("QuestId")) {
            this.setQuestId(tag.getString("QuestId"));
        }

        if (tag.contains("SavedName") && !tag.getString("SavedName").isEmpty()) {
            this.setCustomName(net.minecraft.network.chat.Component.Serializer.fromJson(tag.getString("SavedName"), this.registryAccess()));
            this.setCustomNameVisible(true);
        }

        this.setCustomSkinPath(tag.contains("CustomSkinPath") ? tag.getString("CustomSkinPath") : "");
        this.setSavedSkinInput(tag.contains("SavedSkinInput") ? tag.getString("SavedSkinInput") : "");
        this.setVisualHeight(tag.contains("Height") ? tag.getInt("Height") : 100);
        this.setVisualWidth(tag.contains("Width") ? tag.getInt("Width") : 100);
        VillagerRelationshipData.load(tag, this);
        VillagerCommandHandler.load(tag, this);
        this.setBonusHealth(tag.contains(VillagerHealthHandler.BONUS_HEALTH_TAG) ? tag.getFloat(VillagerHealthHandler.BONUS_HEALTH_TAG) : 0.0F);
        VillagerHealthHandler.reapplyBonusHealth(this, this);
        this.setCombatMode(tag.contains("SlimPatchCombatMode")
                ? VillagerCombatMode.fromName(tag.getString("SlimPatchCombatMode"))
                : VillagerCombatMode.AGGRESSIVE);
        this.setFollowMode(tag.contains("SlimPatchFollowMode")
                ? VillagerFollowMode.fromName(tag.getString("SlimPatchFollowMode"))
                : VillagerFollowMode.CLOSE);
        this.setArmorHidden(tag.getBoolean("SlimPatchHideArmor"));
        this.setMuted(tag.getBoolean("SlimPatchMuted"));
        this.persistentMainHandItem = tag.contains("PersistentMainHand")
                ? ItemStack.parseOptional(this.registryAccess(), tag.getCompound("PersistentMainHand"))
                : ItemStack.EMPTY;
        this.syncPersistentMainHand();

        this.equipmentInventory.clearContent();
        if (tag.contains("EquipmentInventory")) {
            NonNullList<ItemStack> items = NonNullList.withSize(this.equipmentInventory.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("EquipmentInventory"), items, this.registryAccess());
            for (int i = 0; i < items.size(); i++) {
                this.equipmentInventory.setItem(i, items.get(i));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("slimpatch_skin", this.getSkinIndex());
        tag.putString("slimpatch_personality", this.getPersonality().name());
        tag.putFloat("slimpatch_relationship", this.getRelationship());
        tag.putFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG, this.getGoldenRelationship());
        VillagerFamilyData.writeSpousePlayerUuid(tag, this.getSpousePlayerUuid());
        tag.putString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG, this.getSpousePlayerName());
        VillagerFamilyData.writeFormerSpousePlayerUuid(tag, this.getFormerSpousePlayerUuid());
        tag.putString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG, this.getFormerSpousePlayerName());
        tag.putString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG, this.getRelationshipStage().name());
        tag.putString(VillagerFamilyData.AGE_STAGE_TAG, this.getAgeStage().name());
        if (this.getAgeStageStartedAt() > 0L) {
            tag.putLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG, this.getAgeStageStartedAt());
        }
        if (this.getNextAgeStageAt() > 0L) {
            tag.putLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG, this.getNextAgeStageAt());
        }
        tag.putBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG, this.isExpectingChild());
        VillagerFamilyData.writeExpectingParentPlayerUuid(tag, this.getExpectingParentPlayerUuid());
        if (!this.getExpectingParentPlayerName().isEmpty()) {
            tag.putString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG, this.getExpectingParentPlayerName());
        }
        VillagerFamilyData.writeExpectingOtherParentVillagerUuid(tag, this.getExpectingOtherParentVillagerUuid());
        if (!this.getExpectingOtherParentVillagerName().isEmpty()) {
            tag.putString(VillagerFamilyData.EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG, this.getExpectingOtherParentVillagerName());
        }
        if (!this.getForcedBabyGender().isEmpty()) {
            tag.putString(VillagerFamilyData.FORCED_BABY_GENDER_TAG, this.getForcedBabyGender());
        }
        if (this.getExpectingStartedAt() > 0L) {
            tag.putLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG, this.getExpectingStartedAt());
        }
        if (this.getExpectingEndsAt() > 0L) {
            tag.putLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG, this.getExpectingEndsAt());
        }
        this.saveFamilyChildData(tag);
        tag.putBoolean("HasQuest", this.hasQuest());
        tag.putString("QuestId", this.getQuestId());

        if (this.hasCustomName()) {
            tag.putString("SavedName", net.minecraft.network.chat.Component.Serializer.toJson(this.getCustomName(), this.registryAccess()));
        }

        if (!this.getCustomSkinPath().isEmpty()) {
            tag.putString("CustomSkinPath", this.getCustomSkinPath());
        }

        if (!this.getSavedSkinInput().isEmpty()) {
            tag.putString("SavedSkinInput", this.getSavedSkinInput());
        }

        tag.putInt("Height", this.getVisualHeight());
        tag.putInt("Width", this.getVisualWidth());
        VillagerRelationshipData.save(tag, this);
        VillagerCommandHandler.save(tag, this);
        tag.putString("SlimPatchCombatMode", this.getCombatMode().name());
        tag.putString("SlimPatchFollowMode", this.getFollowMode().name());
        tag.putBoolean("SlimPatchHideArmor", this.isArmorHidden());
        tag.putBoolean("SlimPatchMuted", this.isMuted());
        tag.putFloat(VillagerHealthHandler.BONUS_HEALTH_TAG, this.getBonusHealth());
        if (this.persistentMainHandItem.isEmpty()) {
            tag.remove("PersistentMainHand");
            if (!this.level().isClientSide) {
                this.getPersistentData().remove("PersistentMainHand");
            }
        } else {
            tag.put("PersistentMainHand", this.persistentMainHandItem.save(this.registryAccess()));
        }

        CompoundTag equipmentTag = new CompoundTag();
        NonNullList<ItemStack> items = NonNullList.withSize(this.equipmentInventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < this.equipmentInventory.getContainerSize(); i++) {
            items.set(i, this.equipmentInventory.getItem(i));
        }
        ContainerHelper.saveAllItems(equipmentTag, items, this.registryAccess());
        tag.put("EquipmentInventory", equipmentTag);
    }

    private static final Map<String, Long> OPTION_COOLDOWNS = Map.of(
            "Friendly", 60_000L,
            "Mean", 30_000L,
            "Joke", 120_000L,
            "Flirt", 180_000L
    );

    public Map<String, Long> getOptionCooldowns() {
        return OPTION_COOLDOWNS;
    }

    public Map<String, Integer> getCooldownsForClient() {
        return getCooldownsForClient(null);
    }

    public Map<String, Integer> getCooldownsForClient(UUID playerUuid) {
        Map<String, Integer> result = new HashMap<>();
        if (playerUuid == null) {
            return result;
        }
        CompoundTag playerCooldowns = this.getPersistentData().getCompound("SlimPatchDialogueCooldowns").getCompound(playerUuid.toString());
        long now = System.currentTimeMillis();
        for (String key : playerCooldowns.getAllKeys()) {
            long elapsed = now - playerCooldowns.getLong(key);
            long remaining = Math.max(0, OPTION_COOLDOWNS.getOrDefault(key, 20_000L) - elapsed);
            result.put(key, (int) (remaining / 1000));
        }
        return result;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_COMMAND_STATE.equals(key)) {
            this.commandState = this.getCommandState();
        }
        if (DATA_HAS_HOME.equals(key) && !this.hasHome()) {
            this.homePos = null;
            this.homeDimension = null;
        }
        if (DATA_COMMAND_OWNER_UUID.equals(key)) {
            String ownerUuid = this.entityData.get(DATA_COMMAND_OWNER_UUID);
            this.commandOwnerUuid = ownerUuid.isEmpty() ? null : UUID.fromString(ownerUuid);
        }
        if (DATA_GOLDEN_RELATIONSHIP.equals(key)) {
            this.getPersistentData().putFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG, this.getGoldenRelationship());
        }
        if (DATA_SPOUSE_PLAYER_NAME.equals(key)) {
            if (this.getSpousePlayerName().isEmpty()) {
                this.getPersistentData().remove(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG);
            } else {
                this.getPersistentData().putString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG, this.getSpousePlayerName());
            }
        }
        if (DATA_FORMER_SPOUSE_PLAYER_NAME.equals(key)) {
            if (this.getFormerSpousePlayerName().isEmpty()) {
                this.getPersistentData().remove(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG);
            } else {
                this.getPersistentData().putString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG, this.getFormerSpousePlayerName());
            }
        }
        if (DATA_AGE_STAGE.equals(key)) {
            this.refreshDimensions();
            this.getPersistentData().putString(VillagerFamilyData.AGE_STAGE_TAG, this.getAgeStage().name());
        }
        if (DATA_RELATIONSHIP_STAGE.equals(key)) {
            this.getPersistentData().putString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG, this.getRelationshipStage().name());
        }
        if (DATA_EXPECTING_CHILD.equals(key)) {
            this.getPersistentData().putBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG, this.isExpectingChild());
        }
        if (DATA_BORN_FROM_FAMILY_SYSTEM.equals(key)) {
            this.getPersistentData().putBoolean(VillagerFamilyData.BORN_FROM_FAMILY_SYSTEM_TAG, this.isBornFromFamilySystem());
        }
        if (DATA_PARENT_PLAYER_UUID.equals(key)) {
            String parentPlayerUuid = this.entityData.get(DATA_PARENT_PLAYER_UUID);
            if (parentPlayerUuid.isEmpty()) {
                this.parentPlayerUuid = null;
            } else {
                try {
                    this.parentPlayerUuid = UUID.fromString(parentPlayerUuid);
                } catch (IllegalArgumentException e) {
                    this.parentPlayerUuid = null;
                }
            }
            VillagerFamilyData.writeParentPlayerUuid(this.getPersistentData(), this.parentPlayerUuid);
        }
        if (DATA_SKIN.equals(key)) {
            int skin = this.getSkinIndex();
            if (skin <= 0 || skin > MALE_SKINS) {
                int newSkin = this.getRandom().nextInt(MALE_SKINS) + 1;
                this.setSkinIndex(newSkin);
            }
        }
        if (DATA_HAS_QUEST.equals(key) || DATA_QUEST_ID.equals(key)) {
            if (!this.level().isClientSide) {
                this.getPersistentData().putBoolean("HasQuest", this.hasQuest());
                this.getPersistentData().putString("QuestId", this.getQuestId());
            }
        }
        if (DATA_CUSTOM_SKIN_PATH.equals(key)) {
            if (this.getCustomSkinPath().isEmpty()) {
                this.getPersistentData().remove("CustomSkinPath");
            } else {
                this.getPersistentData().putString("CustomSkinPath", this.getCustomSkinPath());
            }
        }
        if (DATA_SAVED_SKIN_INPUT.equals(key)) {
            if (this.getSavedSkinInput().isEmpty()) {
                this.getPersistentData().remove("SavedSkinInput");
            } else {
                this.getPersistentData().putString("SavedSkinInput", this.getSavedSkinInput());
            }
        }
        if (DATA_HEIGHT.equals(key)) {
            this.getPersistentData().putInt("Height", this.getVisualHeight());
        }
        if (DATA_WIDTH.equals(key)) {
            this.getPersistentData().putInt("Width", this.getVisualWidth());
        }
        if (DATA_COMBAT_MODE.equals(key)) {
            this.getPersistentData().putString("SlimPatchCombatMode", this.getCombatMode().name());
        }
        if (DATA_FOLLOW_MODE.equals(key)) {
            this.getPersistentData().putString("SlimPatchFollowMode", this.getFollowMode().name());
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return VillagerAgeStageDimensions.forStage(this.getAgeStage(), super.getDefaultDimensions(pose));
    }

    @Override
    public <T extends net.minecraft.world.entity.Mob> T convertTo(EntityType<T> type, boolean keepEquipment) {
        if (type == EntityType.ZOMBIE_VILLAGER) {
            CompoundTag saved = new CompoundTag();
            this.addAdditionalSaveData(saved);
            saved.putString("slimpatch_gender", this.getPersistentData().getString("slimpatch_gender"));

            net.minecraft.world.entity.Mob mob = super.convertTo(com.javic.slimpatch.ModEntities.HUMAN_ZOMBIE_VILLAGER.get(), keepEquipment);

            if (mob instanceof com.javic.slimpatch.entity.HumanZombieVillagerEntity zombie) {
                zombie.getPersistentData().put("slimpatch_saved_data", saved.copy());
                com.javic.slimpatch.memory.CuredVillagerMemory.store(zombie.getUUID(), saved);

                zombie.setGender(this.getPersistentData().getString("slimpatch_gender"));
                zombie.setCustomName(null);
                zombie.setCustomNameVisible(false);

            }
            return (T) mob;
        }

        return super.convertTo(type, keepEquipment);
    }

    public static MaleVillagerEntity restoreFromCured(ServerLevel level, UUID zombieUUID, double x, double y, double z, float yRot, float xRot) {
        CompoundTag saved = com.javic.slimpatch.memory.CuredVillagerMemory.consume(zombieUUID);
        if (saved == null || saved.isEmpty()) {
            saved = new CompoundTag();
        }

        MaleVillagerEntity villager = com.javic.slimpatch.ModEntities.MALE_VILLAGER.get().create(level);
        if (villager == null) return null;

        villager.moveTo(x, y, z, yRot, xRot);
        villager.getPersistentData().putBoolean("slimpatch_restored", true);

        if (saved.contains("slimpatch_skin")) villager.setSkinIndex(saved.getInt("slimpatch_skin"));
        if (saved.contains("slimpatch_personality")) {
            try {
                villager.setPersonality(com.javic.slimpatch.entity.VillagerPersonality.valueOf(saved.getString("slimpatch_personality")));
            } catch (IllegalArgumentException e) {
                villager.setPersonality(com.javic.slimpatch.entity.VillagerPersonality.FRIENDLY);
            }
        }
        if (saved.contains("slimpatch_relationship")) villager.setRelationship(saved.getFloat("slimpatch_relationship"));
        villager.setGoldenRelationship(saved.contains(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                ? saved.getFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                : 0.0F);
        villager.setSpousePlayerUuid(VillagerFamilyData.readSpousePlayerUuid(saved));
        villager.setSpousePlayerName(saved.contains(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                ? saved.getString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                : "");
        villager.setFormerSpousePlayerUuid(VillagerFamilyData.readFormerSpousePlayerUuid(saved));
        villager.setFormerSpousePlayerName(saved.contains(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                ? saved.getString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                : "");
        villager.setRelationshipStage(VillagerFamilyData.parseRelationshipStage(saved.contains(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                ? saved.getString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                : null));
        villager.setAgeStage(VillagerFamilyData.parseAgeStage(saved.contains(VillagerFamilyData.AGE_STAGE_TAG)
                ? saved.getString(VillagerFamilyData.AGE_STAGE_TAG)
                : null));
        villager.setAgeStageStartedAt(saved.contains(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                ? saved.getLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                : 0L);
        villager.setNextAgeStageAt(saved.contains(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                ? saved.getLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                : 0L);
        villager.setExpectingChild(saved.getBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG));
        villager.setExpectingParentPlayerUuid(VillagerFamilyData.readExpectingParentPlayerUuid(saved));
        villager.setExpectingParentPlayerName(saved.contains(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                ? saved.getString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                : "");
        villager.setExpectingStartedAt(saved.contains(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                ? saved.getLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                : 0L);
        villager.setExpectingEndsAt(saved.contains(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                ? saved.getLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                : 0L);
        villager.loadFamilyChildData(saved);
        if (saved.contains("SlimPatchCombatMode")) villager.setCombatMode(VillagerCombatMode.fromName(saved.getString("SlimPatchCombatMode")));
        if (saved.contains("SlimPatchFollowMode")) villager.setFollowMode(VillagerFollowMode.fromName(saved.getString("SlimPatchFollowMode")));
        if (saved.contains("SlimPatchHideArmor")) villager.setArmorHidden(saved.getBoolean("SlimPatchHideArmor"));
        if (saved.contains("SlimPatchMuted")) villager.setMuted(saved.getBoolean("SlimPatchMuted"));
        if (saved.contains(VillagerHealthHandler.BONUS_HEALTH_TAG)) {
            villager.setBonusHealth(saved.getFloat(VillagerHealthHandler.BONUS_HEALTH_TAG));
            VillagerHealthHandler.reapplyBonusHealth(villager, villager);
        }
        if (saved.contains("SavedName") && !saved.getString("SavedName").isEmpty()) {
            villager.setCustomName(net.minecraft.network.chat.Component.literal(saved.getString("SavedName")));
            villager.setCustomNameVisible(true);
        }

        return villager;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount == 1) {
            CompoundTag data = this.getPersistentData();
            CompoundTag saved = data.getCompound("slimpatch_saved_data");

            if (saved != null && !saved.isEmpty()) {
                if (saved.contains("slimpatch_gender")) data.putString("slimpatch_gender", saved.getString("slimpatch_gender"));
                if (saved.contains("slimpatch_skin")) this.setSkinIndex(saved.getInt("slimpatch_skin"));
                if (saved.contains("slimpatch_personality")) {
                    try {
                        this.setPersonality(com.javic.slimpatch.entity.VillagerPersonality.valueOf(saved.getString("slimpatch_personality")));
                    } catch (IllegalArgumentException e) {
                        this.setPersonality(com.javic.slimpatch.entity.VillagerPersonality.FRIENDLY);
                    }
                }
                if (saved.contains("slimpatch_relationship")) this.setRelationship(saved.getFloat("slimpatch_relationship"));
                this.setGoldenRelationship(saved.contains(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                        ? saved.getFloat(VillagerFamilyData.GOLDEN_RELATIONSHIP_TAG)
                        : 0.0F);
                this.setSpousePlayerUuid(VillagerFamilyData.readSpousePlayerUuid(saved));
                this.setSpousePlayerName(saved.contains(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                        ? saved.getString(VillagerFamilyData.SPOUSE_PLAYER_NAME_TAG)
                        : "");
                this.setFormerSpousePlayerUuid(VillagerFamilyData.readFormerSpousePlayerUuid(saved));
                this.setFormerSpousePlayerName(saved.contains(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                        ? saved.getString(VillagerFamilyData.FORMER_SPOUSE_PLAYER_NAME_TAG)
                        : "");
                this.setRelationshipStage(VillagerFamilyData.parseRelationshipStage(saved.contains(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                        ? saved.getString(VillagerFamilyData.RELATIONSHIP_STAGE_TAG)
                        : null));
                this.setAgeStage(VillagerFamilyData.parseAgeStage(saved.contains(VillagerFamilyData.AGE_STAGE_TAG)
                        ? saved.getString(VillagerFamilyData.AGE_STAGE_TAG)
                        : null));
                this.setAgeStageStartedAt(saved.contains(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                        ? saved.getLong(VillagerFamilyData.AGE_STAGE_STARTED_AT_TAG)
                        : 0L);
                this.setNextAgeStageAt(saved.contains(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                        ? saved.getLong(VillagerFamilyData.NEXT_AGE_STAGE_AT_TAG)
                        : 0L);
                this.setExpectingChild(saved.getBoolean(VillagerFamilyData.EXPECTING_CHILD_TAG));
                this.setExpectingParentPlayerUuid(VillagerFamilyData.readExpectingParentPlayerUuid(saved));
                this.setExpectingParentPlayerName(saved.contains(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                        ? saved.getString(VillagerFamilyData.EXPECTING_PARENT_PLAYER_NAME_TAG)
                        : "");
                this.setExpectingStartedAt(saved.contains(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                        ? saved.getLong(VillagerFamilyData.EXPECTING_STARTED_AT_TAG)
                        : 0L);
                this.setExpectingEndsAt(saved.contains(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                        ? saved.getLong(VillagerFamilyData.EXPECTING_ENDS_AT_TAG)
                        : 0L);
                this.loadFamilyChildData(saved);
                if (saved.contains("SlimPatchCombatMode")) this.setCombatMode(VillagerCombatMode.fromName(saved.getString("SlimPatchCombatMode")));
                if (saved.contains("SlimPatchFollowMode")) this.setFollowMode(VillagerFollowMode.fromName(saved.getString("SlimPatchFollowMode")));
                if (saved.contains("SlimPatchHideArmor")) this.setArmorHidden(saved.getBoolean("SlimPatchHideArmor"));
                if (saved.contains("SlimPatchMuted")) this.setMuted(saved.getBoolean("SlimPatchMuted"));
                if (saved.contains(VillagerHealthHandler.BONUS_HEALTH_TAG)) {
                    this.setBonusHealth(saved.getFloat(VillagerHealthHandler.BONUS_HEALTH_TAG));
                    VillagerHealthHandler.reapplyBonusHealth(this, this);
                }
                if (saved.contains("SavedName") && !saved.getString("SavedName").isEmpty()) {
                    this.setCustomName(net.minecraft.network.chat.Component.literal(saved.getString("SavedName")));
                    this.setCustomNameVisible(true);
                    data.putString("SavedName", saved.getString("SavedName"));
                }
                if (saved.contains("CustomSkinPath")) {
                    data.putString("CustomSkinPath", saved.getString("CustomSkinPath"));
                }
                this.getPersistentData().putBoolean("slimpatch_restored", true);
            } else {
                if (data.contains("SavedName") && !data.getString("SavedName").isEmpty()) {
                    this.setCustomName(Component.literal(data.getString("SavedName")));
                    this.setCustomNameVisible(true);
                }
                if (data.contains("CustomSkinPath") && !data.getString("CustomSkinPath").isEmpty()) {
                    this.getPersistentData().putString("CustomSkinPath", data.getString("CustomSkinPath"));
                }
            }
        }

        super.tick();
        if (!this.level().isClientSide) {
            FamilyAgingHandler.tick(this);
            VillagerPassiveHealHandler.tickHealing(this, this);
            this.syncPersistentMainHand();
        }
    }
}
