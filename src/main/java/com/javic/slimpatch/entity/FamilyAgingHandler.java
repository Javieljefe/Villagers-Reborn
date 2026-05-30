package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

public final class FamilyAgingHandler {

    public static final long TODDLER_DURATION_TICKS = 72000L;
    public static final long CHILD_DURATION_TICKS = 168000L;
    public static final long TEEN_DURATION_TICKS = 168000L;

    private FamilyAgingHandler() {
    }

    public static void initializeForBirth(FamilyVillager familyVillager, long gameTime) {
        setAgeStageWithTimer(familyVillager, VillagerAgeStage.TODDLER, gameTime);
    }

    public static void tick(Villager villager) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return;
        }
        VillagerAgeStage ageStage = familyVillager.getAgeStage();
        if (ageStage == VillagerAgeStage.ADULT) {
            if (familyVillager.getAgeStageStartedAt() <= 0L) {
                familyVillager.setAgeStageStartedAt(villager.level().getGameTime());
            }
            if (familyVillager.getNextAgeStageAt() != 0L) {
                familyVillager.setNextAgeStageAt(0L);
            }
            return;
        }
        if (!Config.ENABLE_AGING.get()) {
            return;
        }
        long nextAgeStageAt = familyVillager.getNextAgeStageAt();
        if (nextAgeStageAt <= 0L) {
            initializeForCurrentStage(familyVillager, villager.level().getGameTime());
            return;
        }
        long gameTime = villager.level().getGameTime();
        if (gameTime < nextAgeStageAt) {
            return;
        }
        if (ageStage == VillagerAgeStage.TODDLER) {
            setAgeStageWithTimer(familyVillager, VillagerAgeStage.CHILD, gameTime);
            playAgeUpEffects(villager);
        } else if (ageStage == VillagerAgeStage.CHILD) {
            setAgeStageWithTimer(familyVillager, VillagerAgeStage.TEEN, gameTime);
            playAgeUpEffects(villager);
        } else if (ageStage == VillagerAgeStage.TEEN) {
            setAgeStageWithTimer(familyVillager, VillagerAgeStage.ADULT, gameTime);
            playAgeUpEffects(villager);
        }
    }

    public static void setAgeStageWithTimer(FamilyVillager familyVillager, VillagerAgeStage ageStage, long gameTime) {
        familyVillager.setAgeStage(ageStage);
        familyVillager.setAgeStageStartedAt(gameTime);
        familyVillager.setNextAgeStageAt(resolveNextAgeStageAt(ageStage, gameTime));
    }

    public static void setReadyForNextStage(FamilyVillager familyVillager, long gameTime) {
        if (familyVillager.getAgeStage() == VillagerAgeStage.ADULT) {
            familyVillager.setAgeStageStartedAt(gameTime);
            familyVillager.setNextAgeStageAt(0L);
            return;
        }
        if (familyVillager.getAgeStageStartedAt() <= 0L) {
            familyVillager.setAgeStageStartedAt(gameTime);
        }
        familyVillager.setNextAgeStageAt(gameTime);
    }

    public static void initializeForCurrentStage(FamilyVillager familyVillager, long gameTime) {
        familyVillager.setAgeStageStartedAt(gameTime);
        familyVillager.setNextAgeStageAt(resolveNextAgeStageAt(familyVillager.getAgeStage(), gameTime));
    }

    private static void playAgeUpEffects(Villager villager) {
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + villager.getBbHeight() * 0.65D, villager.getZ(), 12, 0.35D, 0.35D, 0.35D, 0.02D);
        serverLevel.playSound(null, villager.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.8F, 1.15F);
    }

    private static long resolveNextAgeStageAt(VillagerAgeStage ageStage, long gameTime) {
        return switch (ageStage) {
            case TODDLER -> gameTime + Config.TODDLER_DURATION_TICKS.get();
            case CHILD -> gameTime + Config.CHILD_DURATION_TICKS.get();
            case TEEN -> gameTime + Config.TEEN_DURATION_TICKS.get();
            case ADULT -> 0L;
        };
    }
}
