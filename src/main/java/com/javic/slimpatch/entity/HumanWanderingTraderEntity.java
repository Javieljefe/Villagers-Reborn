package com.javic.slimpatch.entity;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HumanWanderingTraderEntity extends WanderingTrader {

    private static final int MALE_SKINS = 3;
    private static final int FEMALE_SKINS = 3;

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(HumanWanderingTraderEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_IS_FEMALE =
            SynchedEntityData.defineId(HumanWanderingTraderEntity.class, EntityDataSerializers.BOOLEAN);

    public HumanWanderingTraderEntity(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
        SlimPatch.LOGGER.info("[SlimPatch] Constructor HumanWanderingTraderEntity llamado (level={})", level.dimension().location());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_IS_FEMALE, false);
    }

    // ========================================================
    // 🎨 Skins
    // ========================================================

    public void setSkinIndex(int index) {
        this.entityData.set(DATA_SKIN, index);
        this.getPersistentData().putInt("hv_skin", index);
    }

    public int getSkinIndex() {
        return this.entityData.get(DATA_SKIN);
    }

    public void setFemale(boolean female) {
        this.entityData.set(DATA_IS_FEMALE, female);
        this.getPersistentData().putBoolean("hv_isFemale", female);
    }

    public boolean isFemale() {
        return this.entityData.get(DATA_IS_FEMALE);
    }

    public ResourceLocation getSkinTexture() {
        String gender = isFemale() ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath("slimpatch",
                "textures/entity/human_trader/" + gender + "/skin_" + String.format("%02d", getSkinIndex()) + ".png");
    }

    // ========================================================
    // 🧬 Spawn & setup
    // ========================================================

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, SpawnGroupData spawnData) {
        SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        CompoundTag data = this.getPersistentData();
        if (!data.contains("hv_initialized")) {
            boolean female = level.getRandom().nextBoolean();
            this.setFemale(female);

            int maxSkins = female ? FEMALE_SKINS : MALE_SKINS;
            int skinIndex = this.getRandom().nextInt(maxSkins) + 1;
            this.setSkinIndex(skinIndex);

            String name = female ? "Wanderer" : "Traveler";
            this.setCustomName(Component.literal(name));
            this.setCustomNameVisible(true);

            data.putBoolean("hv_initialized", true);
        }

        SlimPatch.LOGGER.info("[SlimPatch] HumanWanderingTraderEntity inicializado → gender={} skin={}",
                isFemale() ? "female" : "male", this.getSkinIndex());

        return groupData;
    }

    // ========================================================
    // 🔊 Sonidos e interacción
    // ========================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.level().playSound(null, this.blockPosition(),
                    isFemale() ? HumanVillagerSounds.femaleClick() : HumanVillagerSounds.maleClick(),
                    SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isFemale() ? HumanVillagerSounds.femaleClick() : HumanVillagerSounds.maleClick();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return isFemale() ? HumanVillagerSounds.femaleClick() : HumanVillagerSounds.maleClick();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isFemale() ? HumanVillagerSounds.femaleHurt() : HumanVillagerSounds.maleHurt();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isFemale() ? HumanVillagerSounds.femaleDeath() : HumanVillagerSounds.maleDeath();
    }

    // ========================================================
    // 💾 Persistencia
    // ========================================================

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("hv_skin")) {
            this.setSkinIndex(tag.getInt("hv_skin"));
        }
        if (tag.contains("hv_isFemale")) {
            this.setFemale(tag.getBoolean("hv_isFemale"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("hv_skin", this.getSkinIndex());
        tag.putBoolean("hv_isFemale", this.isFemale());
    }
}