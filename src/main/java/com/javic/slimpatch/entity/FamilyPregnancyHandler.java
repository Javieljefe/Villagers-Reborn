package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

public final class FamilyPregnancyHandler {

    public static final long EXPECTING_DURATION_TICKS = 24000L;

    private FamilyPregnancyHandler() {
    }

    public static Result startFamily(Villager villager, FamilyVillager familyVillager, ServerPlayer player) {
        if (player == null || villager == null || familyVillager == null) {
            return new Result(false, "slimpatch.message.family_not_spouse");
        }
        if (!villager.isAlive() || villager.level() != player.level()) {
            return new Result(false, "slimpatch.message.family_not_spouse");
        }
        if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.MARRIED) {
            return new Result(false, "slimpatch.message.family_not_spouse");
        }
        if (!player.getUUID().equals(familyVillager.getSpousePlayerUuid())) {
            return new Result(false, "slimpatch.message.family_not_spouse");
        }
        if (familyVillager.getAgeStage() != VillagerAgeStage.ADULT) {
            return new Result(false, "slimpatch.message.family_requires_adult");
        }
        if (familyVillager.isExpectingChild()) {
            return new Result(false, "slimpatch.message.family_already_expecting");
        }

        long gameTime = villager.level().getGameTime();
        familyVillager.setExpectingChild(true);
        familyVillager.setExpectingParentPlayerUuid(player.getUUID());
        familyVillager.setExpectingParentPlayerName(player.getGameProfile().getName());
        familyVillager.setExpectingStartedAt(gameTime);
        familyVillager.setExpectingEndsAt(gameTime + getExpectingDurationTicks());
        return new Result(true, "slimpatch.message.family_started");
    }

    public static void startVillagerPregnancy(FamilyVillager femaleVillager, Villager otherParent, long gameTime) {
        if (femaleVillager == null || otherParent == null) {
            return;
        }
        femaleVillager.setExpectingChild(true);
        femaleVillager.setExpectingParentPlayerUuid(null);
        femaleVillager.setExpectingParentPlayerName("");
        femaleVillager.setExpectingOtherParentVillagerUuid(otherParent.getUUID());
        femaleVillager.setExpectingOtherParentVillagerName(otherParent.getName().getString());
        femaleVillager.setExpectingStartedAt(gameTime);
        femaleVillager.setExpectingEndsAt(gameTime + getExpectingDurationTicks());
    }

    public static long getExpectingDurationTicks() {
        return Config.PREGNANCY_DURATION_TICKS.get();
    }

    public static void tickPregnancy(Villager villager, FamilyVillager familyVillager) {
        if (!(villager instanceof FemaleVillagerEntity femaleVillager)) {
            return;
        }
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!familyVillager.isExpectingChild()) {
            return;
        }
        if (familyVillager.getExpectingParentPlayerUuid() != null) {
            return;
        }
        if (serverLevel.getGameTime() < familyVillager.getExpectingEndsAt()) {
            return;
        }

        Villager otherParent = resolveOtherParent(serverLevel, familyVillager);
        if (otherParent == null) {
            var snapshot = com.javic.slimpatch.familytree.FamilyTreeSavedData.get(serverLevel.getServer()).getVillager(familyVillager.getExpectingOtherParentVillagerUuid());
            if (snapshot != null && !snapshot.isAlive()) {
                familyVillager.setExpectingChild(false);
            }
            return;
        }

        FamilyBirthHandler.spawnVillagerFamilyChild(serverLevel, femaleVillager, familyVillager, otherParent);
    }

    private static Villager resolveOtherParent(ServerLevel serverLevel, FamilyVillager familyVillager) {
        if (serverLevel == null || familyVillager == null || familyVillager.getExpectingOtherParentVillagerUuid() == null) {
            return null;
        }
        if (!(serverLevel.getEntity(familyVillager.getExpectingOtherParentVillagerUuid()) instanceof Villager villager)) {
            return null;
        }
        return villager.isAlive() ? villager : null;
    }

    public record Result(boolean success, String messageKey) {
    }
}
