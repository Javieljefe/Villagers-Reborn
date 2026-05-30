package com.javic.slimpatch.entity;

import com.javic.slimpatch.sounds.HumanZombieVillagerSounds;
import com.javic.slimpatch.util.SkinPathHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.UUID;

public class HumanZombieVillagerEntity extends ZombieVillager {

    private static final String CURING_PLAYER_TAG = "SlimPatchCuringPlayer";
    private static final float CURING_RELATIONSHIP_BONUS = 2.0F;
    private static final EntityDataAccessor<String> DATA_GENDER =
            SynchedEntityData.defineId(HumanZombieVillagerEntity.class, EntityDataSerializers.STRING);

    private int conversionTicks = -1;

    public HumanZombieVillagerEntity(EntityType<? extends ZombieVillager> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GENDER, "male");
    }

    public String getGender() {
        return this.entityData.get(DATA_GENDER);
    }

    public void setGender(String gender) {
        this.entityData.set(DATA_GENDER, gender);
        this.getPersistentData().putString("slimpatch_gender", gender);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("slimpatch_saved_data")) {
            this.getPersistentData().put("slimpatch_saved_data", tag.getCompound("slimpatch_saved_data").copy());
        }
        String gender = getSavedGender(this.getPersistentData().getCompound("slimpatch_saved_data"));
        if (gender == null && tag.contains("slimpatch_gender")) {
            gender = normalizeGender(tag.getString("slimpatch_gender"));
        }
        if (gender != null) {
            this.setGender(gender);
        }
        if (tag.hasUUID(CURING_PLAYER_TAG)) {
            this.getPersistentData().putUUID(CURING_PLAYER_TAG, tag.getUUID(CURING_PLAYER_TAG));
        }
        this.conversionTicks = tag.contains("SlimPatchConversionTicks") ? tag.getInt("SlimPatchConversionTicks") : -1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getPersistentData().contains("slimpatch_saved_data")) {
            tag.put("slimpatch_saved_data", this.getPersistentData().getCompound("slimpatch_saved_data").copy());
        }
        String gender = getSavedGender(this.getPersistentData().getCompound("slimpatch_saved_data"));
        tag.putString("slimpatch_gender", gender != null ? gender : normalizeGender(this.getGender()));
        if (this.getPersistentData().hasUUID(CURING_PLAYER_TAG)) {
            tag.putUUID(CURING_PLAYER_TAG, this.getPersistentData().getUUID(CURING_PLAYER_TAG));
        }
        if (this.conversionTicks >= 0) {
            tag.putInt("SlimPatchConversionTicks", this.conversionTicks);
        }
    }

    public ResourceLocation getSkinTexture() {
        String gender = this.getGender();
        return SkinPathHelper.getSkinForType("human_zombie_villager", gender, 1, level());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        String gender = getSavedGender(this.getPersistentData().getCompound("slimpatch_saved_data"));
        if (gender == null && this.getPersistentData().contains("slimpatch_gender")) {
            gender = normalizeGender(this.getPersistentData().getString("slimpatch_gender"));
        }
        if (gender != null) {
            this.setGender(gender);
        }
        return data;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return "female".equalsIgnoreCase(this.getGender())
                ? HumanZombieVillagerSounds.femaleAmbient()
                : SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return "female".equalsIgnoreCase(this.getGender())
                ? HumanZombieVillagerSounds.femaleHurt()
                : SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return "female".equalsIgnoreCase(this.getGender())
                ? HumanZombieVillagerSounds.femaleDeath()
                : SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        var item = player.getItemInHand(hand);
        if (item.is(net.minecraft.world.item.Items.GOLDEN_APPLE) && this.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) {
            if (!this.level().isClientSide) {
                item.consume(1, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    this.getPersistentData().putUUID(CURING_PLAYER_TAG, serverPlayer.getUUID());
                }
                startManualConversion();
                this.level().levelEvent(null, 1027, this.blockPosition(), 0);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void startManualConversion() {
        this.conversionTicks = 200; // 10 segundos (ajustable)
        this.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, conversionTicks, 1));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isAlive() && this.conversionTicks > 0) {
            this.conversionTicks--;
            if (this.conversionTicks == 0) {
                finishManualConversion((ServerLevel) this.level());
            }
        }
    }

    private void finishManualConversion(ServerLevel level) {
        CompoundTag saved = this.getPersistentData().getCompound("slimpatch_saved_data");
        if ((saved == null || saved.isEmpty()) && this.getUUID() != null) {
            saved = com.javic.slimpatch.memory.CuredVillagerMemory.peek(this.getUUID());
        }
        if (saved == null || saved.isEmpty()) {
            return;
        }

        String gender = getSavedGender(saved);
        if (gender == null) {
            gender = normalizeGender(this.getGender());
        }
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        float yRot = this.getYRot();
        float xRot = this.getXRot();

        this.discard();

        CompoundTag nbt = saved.copy();
        nbt.putString("id", "slimpatch:" + (gender.equalsIgnoreCase("female") ? "female_villager" : "male_villager"));
        nbt.putString("slimpatch_gender", gender);
        nbt.putBoolean("slimpatch_restored", true);
        nbt.put("slimpatch_saved_data", saved.copy());
        nbt.remove("DeathTime");
        nbt.remove("HurtTime");
        nbt.remove("HurtByTimestamp");
        nbt.remove("Fire");
        nbt.remove("Air");
        if (!nbt.contains("Health") || nbt.getFloat("Health") <= 0.0F) {
            nbt.putFloat("Health", 20.0F);
        }

        net.minecraft.world.entity.Entity villager = net.minecraft.world.entity.EntityType.loadEntityRecursive(nbt, level, (entity) -> {
            entity.moveTo(x, y, z, yRot, xRot);
            return entity;
        });

        if (villager != null) {
            if (villager instanceof LivingEntity living && living.getHealth() <= 0.0F) {
                living.setHealth(living.getMaxHealth());
            }
            level.addFreshEntity(villager);
            if (villager instanceof Villager restoredVillager) {
                applyCuringRewards(level, restoredVillager);
            }
            level.levelEvent(null, 1027, this.blockPosition(), 0);
            com.javic.slimpatch.memory.CuredVillagerMemory.consume(this.getUUID());

        }
    }

    private void applyCuringRewards(ServerLevel level, Villager villager) {
        if (!this.getPersistentData().hasUUID(CURING_PLAYER_TAG) || level.getServer() == null) {
            return;
        }

        UUID playerUuid = this.getPersistentData().getUUID(CURING_PLAYER_TAG);
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (player == null) {
            return;
        }

        if (villager.isAlive() && !villager.isRemoved()) {
            level.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, player, villager);
        }
        applyRelationshipReward(villager, player);
    }

    private static String getSavedGender(CompoundTag saved) {
        if (saved == null || saved.isEmpty() || !saved.contains("slimpatch_gender")) {
            return null;
        }
        return normalizeGender(saved.getString("slimpatch_gender"));
    }

    private static String normalizeGender(String gender) {
        return "female".equalsIgnoreCase(gender) ? "female" : "male";
    }

    private void applyRelationshipReward(Villager villager, ServerPlayer player) {
        if (villager instanceof MaleVillagerEntity maleVillager) {
            float current = VillagerRelationshipData.getRelationshipForPlayer(maleVillager, player.getUUID(), maleVillager.getRelationship());
            float updated = VillagerRelationshipData.setRelationshipForPlayer(maleVillager, player.getUUID(), current + CURING_RELATIONSHIP_BONUS, maleVillager.getRelationship());
            if (!VillagerRelationshipData.usesPerPlayerRelationships(maleVillager)) {
                maleVillager.setRelationship(updated);
            }
        } else if (villager instanceof FemaleVillagerEntity femaleVillager) {
            float current = VillagerRelationshipData.getRelationshipForPlayer(femaleVillager, player.getUUID(), femaleVillager.getRelationship());
            float updated = VillagerRelationshipData.setRelationshipForPlayer(femaleVillager, player.getUUID(), current + CURING_RELATIONSHIP_BONUS, femaleVillager.getRelationship());
            if (!VillagerRelationshipData.usesPerPlayerRelationships(femaleVillager)) {
                femaleVillager.setRelationship(updated);
            }
        }
    }
}
