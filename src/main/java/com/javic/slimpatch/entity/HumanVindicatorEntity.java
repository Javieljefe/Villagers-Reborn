package com.javic.slimpatch.entity;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.sounds.HumanIllagerSounds;
import com.javic.slimpatch.util.SkinPathHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class HumanVindicatorEntity extends Vindicator {

    private static final int MALE_SKINS = 8;
    private static final int FEMALE_SKINS = 8;

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(HumanVindicatorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_GENDER =
            SynchedEntityData.defineId(HumanVindicatorEntity.class, EntityDataSerializers.STRING);

    public HumanVindicatorEntity(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        SlimPatch.LOGGER.debug("[SlimPatch] HumanVindicatorEntity construido en nivel {}", level.dimension().location());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_GENDER, "male");
    }

    public boolean isFemale() {
        return "female".equalsIgnoreCase(this.entityData.get(DATA_GENDER));
    }

    public void setGender(String gender) {
        this.entityData.set(DATA_GENDER, gender);
        this.getPersistentData().putString("slimpatch_gender", gender);
    }

    public String getGender() {
        return this.entityData.get(DATA_GENDER);
    }

    public void setSkinIndex(int index) {
        this.entityData.set(DATA_SKIN, index);
        this.getPersistentData().putInt("slimpatch_skin", index);
    }

    public int getSkinIndex() {
        return this.entityData.get(DATA_SKIN);
    }

    public ResourceLocation getSkinTexture() {
        return SkinPathHelper.getSkinForType("human_vindicator", getGender(), getSkinIndex(), level());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, SpawnGroupData spawnData) {
        SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        CompoundTag data = this.getPersistentData();

        if (!data.contains("slimpatch_initialized")) {
            boolean female = this.getRandom().nextBoolean();
            String gender = female ? "female" : "male";
            this.setGender(gender);

            int skinIndex = this.getRandom().nextInt(female ? FEMALE_SKINS : MALE_SKINS) + 1;
            this.setSkinIndex(skinIndex);

            data.putString("slimpatch_gender", gender);
            data.putInt("slimpatch_skin", skinIndex);
            data.putBoolean("slimpatch_initialized", true);

            SlimPatch.LOGGER.info("[SlimPatch] HumanVindicatorEntity spawn → gender={} skin={}", gender, skinIndex);

            this.setCustomName(null);
            this.setCustomNameVisible(false);
        } else {
            if (data.contains("slimpatch_gender")) this.setGender(data.getString("slimpatch_gender"));
            if (data.contains("slimpatch_skin")) this.setSkinIndex(data.getInt("slimpatch_skin"));
        }

        return groupData;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isFemale() ? HumanIllagerSounds.femaleAmbient() : HumanIllagerSounds.maleAmbient();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isFemale() ? HumanIllagerSounds.femaleHurt() : HumanIllagerSounds.maleHurt();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isFemale() ? HumanIllagerSounds.femaleDeath() : HumanIllagerSounds.maleDeath();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.level().playSound(
                    null, this.blockPosition(),
                    this.isFemale() ? HumanIllagerSounds.femaleAmbient() : HumanIllagerSounds.maleAmbient(),
                    SoundSource.HOSTILE, 1.0F, 1.0F
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("slimpatch_skin")) this.setSkinIndex(tag.getInt("slimpatch_skin"));
        if (tag.contains("slimpatch_gender")) this.setGender(tag.getString("slimpatch_gender"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("slimpatch_skin", this.getSkinIndex());
        tag.putString("slimpatch_gender", this.getGender());
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public Component getName() {
        return this.getType().getDescription();
    }
}