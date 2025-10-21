package com.javic.slimpatch.entity;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import com.javic.slimpatch.util.SkinPathHelper;
import com.javic.slimpatch.dialogue.DialogueGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaleVillagerEntity extends Villager {

    private static final int MALE_SKINS = 70;

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_PERSONALITY =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Float> DATA_RELATIONSHIP =
            SynchedEntityData.defineId(MaleVillagerEntity.class, EntityDataSerializers.FLOAT);

    private static final String[] MALE_NAMES = {
            "Adam", "Adrian", "Alexander", "Alfred", "Andrew",
            "Anthony", "Arthur", "Benjamin", "Blake", "Brandon",
            "Brian", "Caleb", "Cameron", "Carl", "Charles",
            "Christian", "Christopher", "Colin", "Connor", "Daniel",
            "David", "Dean", "Dominic", "Dylan", "Edward",
            "Elias", "Elijah", "Elliot", "Ethan", "Evan",
            "Felix", "Finn", "Francis", "Gabriel", "Gavin",
            "George", "Grant", "Gregory", "Harold", "Harrison",
            "Harry", "Henry", "Hugh", "Ian", "Isaac",
            "Jack", "Jacob", "James", "Jason", "Jeremy",
            "Joel", "John", "Jonathan", "Joseph", "Joshua",
            "Julian", "Keith", "Kenneth", "Kevin", "Kyle",
            "Liam", "Logan", "Louis", "Lucas", "Luke",
            "Malcolm", "Marcus", "Mark", "Martin", "Matthew",
            "Michael", "Nathan", "Nicholas", "Noah", "Oliver",
            "Oscar", "Owen", "Patrick", "Paul", "Peter",
            "Philip", "Raymond", "Richard", "Robert", "Ryan",
            "Samuel", "Scott", "Sean", "Simon", "Stephen",
            "Theodore", "Thomas", "Timothy", "Tristan", "Victor",
            "Vincent", "Walter", "William", "Zachary", "Ezekiel"
    };

    public MaleVillagerEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
        SlimPatch.LOGGER.info("[SlimPatch] Constructor MaleVillagerEntity llamado (level={})", level.dimension().location());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_PERSONALITY, "FRIENDLY");
        builder.define(DATA_RELATIONSHIP, 0.5f);
    }

    public void setSkinIndex(int index) {
        this.entityData.set(DATA_SKIN, index);
        this.getPersistentData().putInt("slimpatch_skin", index);
    }

    public int getSkinIndex() {
        return this.entityData.get(DATA_SKIN);
    }

    public ResourceLocation getSkinTexture() {
        int skin = this.getSkinIndex();

        if (skin <= 0 || skin > MALE_SKINS) {
            CompoundTag data = this.getPersistentData();
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
        this.entityData.set(DATA_PERSONALITY, personality.name());
        this.getPersistentData().putString("slimpatch_personality", personality.name());
    }

    public float getRelationship() {
        return this.entityData.get(DATA_RELATIONSHIP);
    }

    public void setRelationship(float value) {
        float clamped = Math.max(0.0f, Math.min(5.0f, value));
        this.entityData.set(DATA_RELATIONSHIP, clamped);
        this.getPersistentData().putFloat("slimpatch_relationship", clamped);
    }

    public void applyRelationshipChange(String option) {
        boolean success = com.javic.slimpatch.dialogue.DialogueManager.calculateSuccess(getPersonality(), option);
        applyRelationshipChange(option, success);
    }

    public void applyRelationshipChange(String option, boolean success) {
        long now = System.currentTimeMillis();

        if (optionCooldowns.containsKey(option)) {
            long lastTime = optionCooldowns.get(option);
            long cooldown = OPTION_COOLDOWNS.getOrDefault(option, 20_000L);
            if (now - lastTime < cooldown) {
                SlimPatch.LOGGER.info("[SlimPatch] Option '{}' on cooldown for {}", option, this.getName().getString());
                return;
            }
        }
        optionCooldowns.put(option, now);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            UUID uuid = this.getUUID();
            serverLevel.getServer().getPlayerList().getPlayers().forEach(player -> {
                if (player.distanceToSqr(this) < 64 * 64) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                            player,
                            new com.javic.slimpatch.network.VillagerCooldownsPacket(uuid, this.getId(), this.getCooldownsForClient())
                    );
                }
            });
        }

        VillagerPersonality personality = getPersonality();
        float delta = com.javic.slimpatch.dialogue.DialogueManager.getRelationshipChange(personality, option, success);
        float old = getRelationship();
        setRelationship(old + delta);

        if (delta > 0) {
            level().playSound(null, blockPosition(), com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            for (int i = 0; i < 7; i++) {
                ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        getX() + random.nextGaussian() * 0.3,
                        getY() + 1.0,
                        getZ() + random.nextGaussian() * 0.3,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        } else if (delta < 0) {
            level().playSound(null, blockPosition(), com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionNegative(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            for (int i = 0; i < 4; i++) {
                ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                        getX() + random.nextGaussian() * 0.3,
                        getY() + 1.0,
                        getZ() + random.nextGaussian() * 0.3,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        SlimPatch.LOGGER.info("[SlimPatch] {} ({}) -> option '{}' -> deltaRel={} (success={})",
                this.getName().getString(), personality.name(), option, delta, success);
    }

    public void giveGiftToPlayer(net.minecraft.world.entity.player.Player player) {
        if (this.level().isClientSide) return;
        UUID uuid = this.getUUID();
        if (com.javic.slimpatch.network.VillagerCooldownsStorage.hasGiftCooldown(uuid)) return;
        if (this.getRelationship() < 5.0f) return;

        net.minecraft.world.item.ItemStack gift = com.javic.slimpatch.entity.GiftReactionHandler.getRandomGift(this.getPersonality(), this.getRandom());
        boolean added = player.addItem(gift);
        if (!added) this.spawnAtLocation(gift);
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);

        com.javic.slimpatch.network.VillagerCooldownsStorage.setGiftCooldown(uuid);

        ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        this.level().playSound(null, this.blockPosition(),
                com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);

        SlimPatch.LOGGER.info("[SlimPatch] {} gave a gift to {}", this.getName().getString(), player.getName().getString());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DialogueGoal(this));
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

            String chosenName = MALE_NAMES[this.getRandom().nextInt(MALE_NAMES.length)];
            this.setCustomName(Component.literal(chosenName));
            this.setCustomNameVisible(true);

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

        SlimPatch.LOGGER.info("[SlimPatch] MaleVillagerEntity inicializado: skin={} name={} personality={} relationship={}",
                this.getSkinIndex(), this.getName().getString(), this.getPersonality(), this.getRelationship());

        return groupData;
    }

    @Override
    public boolean shouldShowName() {
        Player nearestPlayer = this.level().getNearestPlayer(this, 8.0);
        return nearestPlayer != null && this.hasCustomName();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.level().playSound(null, this.blockPosition(),
                    HumanVillagerSounds.maleClick(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HumanVillagerSounds.maleClick();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return HumanVillagerSounds.maleClick();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return HumanVillagerSounds.maleHurt();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HumanVillagerSounds.maleDeath();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("slimpatch_skin")) {
            this.setSkinIndex(tag.getInt("slimpatch_skin"));
        } else {
            int newSkin = this.getRandom().nextInt(MALE_SKINS) + 1;
            this.setSkinIndex(newSkin);
            SlimPatch.LOGGER.warn("[SlimPatch] Aldeano antiguo sin campo 'slimpatch_skin', asignada skin aleatoria: {}", newSkin);
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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("slimpatch_skin", this.getSkinIndex());
        tag.putString("slimpatch_personality", this.getPersonality().name());
        tag.putFloat("slimpatch_relationship", this.getRelationship());
    }

    private static final Map<String, Long> OPTION_COOLDOWNS = Map.of(
            "Friendly", 60_000L,
            "Mean", 30_000L,
            "Joke", 120_000L,
            "Flirt", 180_000L
    );

    private final Map<String, Long> optionCooldowns = new HashMap<>();

    public Map<String, Integer> getCooldownsForClient() {
        Map<String, Integer> result = new HashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : optionCooldowns.entrySet()) {
            long elapsed = now - entry.getValue();
            long remaining = Math.max(0, OPTION_COOLDOWNS.getOrDefault(entry.getKey(), 20_000L) - elapsed);
            result.put(entry.getKey(), (int) (remaining / 1000));
        }
        return result;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SKIN.equals(key)) {
            int skin = this.getSkinIndex();
            if (skin <= 0 || skin > MALE_SKINS) {
                int newSkin = this.getRandom().nextInt(MALE_SKINS) + 1;
                this.setSkinIndex(newSkin);
                System.out.println("[SlimPatch] Skin invalid detectada en cliente, reasignada: " + newSkin);
            }
        }
    }
}