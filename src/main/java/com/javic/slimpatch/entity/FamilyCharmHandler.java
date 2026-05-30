package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.config.VillagerNameConfig;
import com.javic.slimpatch.data.WorldSkinData;
import com.javic.slimpatch.familytree.FamilyTreeSavedData;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import com.javic.slimpatch.familytree.FamilyTreeVillagerSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "slimpatch")
public final class FamilyCharmHandler {

    private static final double MAX_PLAYER_USE_DISTANCE_SQR = 64.0D;
    private static final double MAX_PARENT_DISTANCE_SQR = 144.0D;
    private static final Map<UUID, SelectedParent> SELECTED_PARENTS = new HashMap<>();

    private FamilyCharmHandler() {
    }

    public static InteractionResult handleUseOnVillager(ServerPlayer player, ItemStack stack, Villager target) {
        ValidationResult targetValidation = validateCandidate(player, target);
        if (!targetValidation.valid()) {
            player.displayClientMessage(Component.translatable(targetValidation.messageKey()), true);
            return InteractionResult.SUCCESS;
        }

        SelectedParent selectedParent = SELECTED_PARENTS.get(player.getUUID());
        if (selectedParent == null) {
            selectParent(player, target);
            return InteractionResult.SUCCESS;
        }

        Villager firstParent = resolveSelectedParent(player, selectedParent);
        if (firstParent == null) {
            SELECTED_PARENTS.remove(player.getUUID());
            selectParent(player, target);
            return InteractionResult.SUCCESS;
        }

        ValidationResult firstValidation = validateCandidate(player, firstParent);
        if (!firstValidation.valid()) {
            SELECTED_PARENTS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable(firstValidation.messageKey()), true);
            return InteractionResult.SUCCESS;
        }

        ValidationResult pairValidation = validatePair(player, firstParent, target);
        if (!pairValidation.valid()) {
            if (pairValidation.clearSelection()) {
                SELECTED_PARENTS.remove(player.getUUID());
            }
            player.displayClientMessage(Component.translatable(pairValidation.messageKey()), true);
            return InteractionResult.SUCCESS;
        }

        if (!(firstParent instanceof FamilyVillager firstFamilyVillager) || !(target instanceof FamilyVillager secondFamilyVillager)) {
            player.displayClientMessage(Component.translatable("slimpatch.message.family_charm_invalid_target"), true);
            return InteractionResult.SUCCESS;
        }

        clearDeceasedSpouseIfNeeded(player.serverLevel(), firstParent);
        clearDeceasedSpouseIfNeeded(player.serverLevel(), target);

        Villager femaleParent = firstParent instanceof FemaleVillagerEntity ? firstParent : target;
        Villager otherParent = femaleParent == firstParent ? target : firstParent;
        FamilyVillager femaleFamilyVillager = femaleParent == firstParent ? firstFamilyVillager : secondFamilyVillager;

        String childLimitMessage = validateChildLimit(player.serverLevel(), firstParent.getUUID(), target.getUUID());
        if (childLimitMessage != null) {
            player.displayClientMessage(Component.translatable(childLimitMessage), true);
            return InteractionResult.SUCCESS;
        }

        linkSpouses(firstParent, firstFamilyVillager, target, secondFamilyVillager);
        FamilyPregnancyHandler.startVillagerPregnancy(femaleFamilyVillager, otherParent, player.serverLevel().getGameTime());
        FamilyTreeTracker.upsertVillager(player.serverLevel().getServer(), firstParent);
        FamilyTreeTracker.upsertVillager(player.serverLevel().getServer(), target);
        playFamilyCharmSuccessEffects(player.serverLevel(), firstParent, target);

        if (!player.getAbilities().instabuild) {
            stack.consume(1, player);
        }

        SELECTED_PARENTS.remove(player.getUUID());
        player.displayClientMessage(Component.translatable("slimpatch.message.family_charm_family_started", firstParent.getName().getString(), target.getName().getString()), true);
        return InteractionResult.SUCCESS;
    }

    public static boolean clearSelection(ServerPlayer player, boolean notify) {
        if (player == null) {
            return false;
        }
        SelectedParent removed = SELECTED_PARENTS.remove(player.getUUID());
        if (removed != null && notify) {
            player.displayClientMessage(Component.translatable("slimpatch.message.family_charm_selection_cleared"), true);
        }
        return removed != null;
    }

    public static void notifyNearbyBirth(ServerLevel level, Villager mother, Villager otherParent, Villager child) {
        if (level == null || mother == null || otherParent == null || child == null) {
            return;
        }
        String message = "slimpatch.message.family_charm_child_born";
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(mother) <= MAX_PARENT_DISTANCE_SQR || player.distanceToSqr(otherParent) <= MAX_PARENT_DISTANCE_SQR) {
                player.displayClientMessage(Component.translatable(message, child.getName().getString()), true);
            }
        }
    }

    public static Villager spawnVillagerFamilyChild(ServerLevel level, Villager mother, FamilyVillager motherFamilyVillager, Villager otherParent) {
        String childGender = BabyGenderCharmHandler.resolveChildGender(motherFamilyVillager, level.random.nextBoolean() ? "male" : "female");
        Villager child = createVillagerForGender(level, childGender);
        if (!(child instanceof FamilyVillager childFamilyVillager)) {
            return null;
        }

        Vec3 spawnPos = findSpawnPosition(level, child, mother, otherParent);
        child.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, mother.getYRot(), 0.0F);
        applyChildData(level, child, childFamilyVillager, mother, motherFamilyVillager, otherParent, childGender);
        if (!level.addFreshEntity(child)) {
            return null;
        }

        FamilyTreeTracker.upsertVillager(level.getServer(), mother);
        FamilyTreeTracker.upsertVillager(level.getServer(), otherParent);
        FamilyTreeTracker.upsertVillager(level.getServer(), child);
        FamilyTreeTracker.linkChildToParents(level.getServer(), child);
        FamilyCharmHandler.notifyNearbyBirth(level, mother, otherParent, child);
        return child;
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SELECTED_PARENTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        SELECTED_PARENTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SELECTED_PARENTS.clear();
    }

    private static void selectParent(ServerPlayer player, Villager target) {
        SELECTED_PARENTS.put(player.getUUID(), new SelectedParent(target.getUUID(), player.level().dimension(), player.level().getGameTime()));
        player.displayClientMessage(Component.translatable("slimpatch.message.family_charm_first_parent_selected"), true);
    }

    private static Villager resolveSelectedParent(ServerPlayer player, SelectedParent selectedParent) {
        if (player == null || selectedParent == null) {
            return null;
        }
        if (!player.level().dimension().equals(selectedParent.dimension())) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(selectedParent.parentVillagerUuid());
        if (!(entity instanceof Villager villager) || !villager.isAlive()) {
            return null;
        }
        if (player.distanceToSqr(villager) > MAX_PARENT_DISTANCE_SQR) {
            return null;
        }
        return villager;
    }

    private static ValidationResult validateCandidate(ServerPlayer player, Villager villager) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return ValidationResult.failure("slimpatch.message.family_charm_invalid_target");
        }
        if (!(villager instanceof MaleVillagerEntity || villager instanceof FemaleVillagerEntity)) {
            return ValidationResult.failure("slimpatch.message.family_charm_invalid_target");
        }
        if (!villager.isAlive()) {
            return ValidationResult.failure("slimpatch.message.family_charm_invalid_target");
        }
        if (player.distanceToSqr(villager) > MAX_PLAYER_USE_DISTANCE_SQR) {
            return ValidationResult.failure("slimpatch.message.family_charm_too_far");
        }
        if (familyVillager.getAgeStage() != VillagerAgeStage.ADULT) {
            return ValidationResult.failure("slimpatch.message.family_charm_must_be_adult");
        }
        if (familyVillager.isExpectingChild()) {
            return ValidationResult.failure("slimpatch.message.family_charm_already_expecting");
        }
        if (familyVillager.getSpousePlayerUuid() != null) {
            return ValidationResult.failure("slimpatch.message.family_charm_married_to_player");
        }
        if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.DATING) {
            return ValidationResult.failure("slimpatch.message.family_charm_dating_player");
        }
        if (VillagerFamilyData.isFamilyChildOf(villager, player) && familyVillager.getAgeStage() != VillagerAgeStage.ADULT) {
            return ValidationResult.failure("slimpatch.message.family_charm_child_of_player");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validatePair(ServerPlayer player, Villager firstParent, Villager secondParent) {
        ValidationResult secondValidation = validateCandidate(player, secondParent);
        if (!secondValidation.valid()) {
            return secondValidation;
        }
        if (firstParent.getUUID().equals(secondParent.getUUID())) {
            return ValidationResult.failure("slimpatch.message.family_charm_same_villager");
        }
        if (!firstParent.level().dimension().equals(secondParent.level().dimension())) {
            return ValidationResult.failureAndClear("slimpatch.message.family_charm_too_far");
        }
        if (firstParent.distanceToSqr(secondParent) > MAX_PARENT_DISTANCE_SQR) {
            return ValidationResult.failure("slimpatch.message.family_charm_too_far");
        }
        if ((firstParent instanceof FemaleVillagerEntity && secondParent instanceof FemaleVillagerEntity)
                || (firstParent instanceof MaleVillagerEntity && secondParent instanceof MaleVillagerEntity)) {
            return ValidationResult.failure("slimpatch.message.family_charm_same_gender");
        }
        if (!(firstParent instanceof FamilyVillager firstFamilyVillager) || !(secondParent instanceof FamilyVillager secondFamilyVillager)) {
            return ValidationResult.failure("slimpatch.message.family_charm_invalid_target");
        }
        if (firstFamilyVillager.getSpouseVillagerUuid() != null && !firstFamilyVillager.getSpouseVillagerUuid().equals(secondParent.getUUID())) {
            ValidationResult firstSpouseValidation = validateVillagerSpouse(player.serverLevel(), firstParent, secondParent);
            if (!firstSpouseValidation.valid()) {
                return firstSpouseValidation;
            }
        }
        if (secondFamilyVillager.getSpouseVillagerUuid() != null && !secondFamilyVillager.getSpouseVillagerUuid().equals(firstParent.getUUID())) {
            ValidationResult secondSpouseValidation = validateVillagerSpouse(player.serverLevel(), secondParent, firstParent);
            if (!secondSpouseValidation.valid()) {
                return secondSpouseValidation;
            }
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateVillagerSpouse(ServerLevel level, Villager villager, Villager targetSpouse) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return ValidationResult.failure("slimpatch.message.family_charm_invalid_target");
        }
        if (familyVillager.getSpousePlayerUuid() != null) {
            return ValidationResult.failure("slimpatch.message.family_charm_married_to_player");
        }
        UUID spouseVillagerUuid = familyVillager.getSpouseVillagerUuid();
        if (spouseVillagerUuid == null || spouseVillagerUuid.equals(targetSpouse.getUUID())) {
            return ValidationResult.ok();
        }
        if (isVillagerSpouseAlive(level, spouseVillagerUuid)) {
            return ValidationResult.failure("slimpatch.message.family_charm_only_spouse");
        }
        return ValidationResult.ok();
    }

    private static boolean isVillagerSpouseAlive(ServerLevel level, UUID spouseVillagerUuid) {
        if (level == null || spouseVillagerUuid == null) {
            return false;
        }
        Entity spouseEntity = level.getEntity(spouseVillagerUuid);
        if (spouseEntity instanceof Villager spouseVillager) {
            return spouseVillager.isAlive();
        }
        FamilyTreeVillagerSnapshot spouseSnapshot = FamilyTreeSavedData.get(level.getServer()).getVillager(spouseVillagerUuid);
        return spouseSnapshot == null || spouseSnapshot.isAlive();
    }

    private static void clearDeceasedSpouseIfNeeded(ServerLevel level, Villager villager) {
        if (level == null || !(villager instanceof FamilyVillager familyVillager)) {
            return;
        }
        UUID spouseVillagerUuid = familyVillager.getSpouseVillagerUuid();
        if (spouseVillagerUuid == null || isVillagerSpouseAlive(level, spouseVillagerUuid)) {
            return;
        }
        familyVillager.setSpouseVillagerUuid(null);
        familyVillager.setSpouseVillagerName("");

        FamilyTreeSavedData data = FamilyTreeSavedData.get(level.getServer());
        FamilyTreeVillagerSnapshot villagerSnapshot = data.getOrCreateVillager(villager.getUUID());
        if (villagerSnapshot != null) {
            villagerSnapshot.setSpouseVillagerUuid(null);
            villagerSnapshot.setSpouseVillagerName("");
            data.putVillager(villagerSnapshot);
        }

        FamilyTreeVillagerSnapshot spouseSnapshot = data.getVillager(spouseVillagerUuid);
        if (spouseSnapshot != null && villager.getUUID().equals(spouseSnapshot.getSpouseVillagerUuid())) {
            spouseSnapshot.setSpouseVillagerUuid(null);
            spouseSnapshot.setSpouseVillagerName("");
            data.putVillager(spouseSnapshot);
        }

        Entity spouseEntity = level.getEntity(spouseVillagerUuid);
        if (spouseEntity instanceof FamilyVillager spouseFamilyVillager && villager.getUUID().equals(spouseFamilyVillager.getSpouseVillagerUuid())) {
            spouseFamilyVillager.setSpouseVillagerUuid(null);
            spouseFamilyVillager.setSpouseVillagerName("");
            if (spouseEntity instanceof Villager spouseVillager) {
                FamilyTreeTracker.upsertVillager(level.getServer(), spouseVillager);
            }
        }

        FamilyTreeTracker.upsertVillager(level.getServer(), villager);
    }

    private static String validateChildLimit(ServerLevel level, UUID parentA, UUID parentB) {
        if (level == null || parentA == null || parentB == null) {
            return null;
        }
        FamilyTreeSavedData savedData = FamilyTreeSavedData.get(level.getServer());
        FamilyTreeVillagerSnapshot snapshot = savedData.getVillager(parentA);
        if (snapshot == null) {
            return null;
        }
        int children = 0;
        for (UUID childUuid : snapshot.getChildVillagerUuids()) {
            if (childUuid == null) {
                continue;
            }
            FamilyTreeVillagerSnapshot childSnapshot = savedData.getVillager(childUuid);
            if (childSnapshot == null) {
                continue;
            }
            boolean matchesForward = parentA.equals(childSnapshot.getParentVillagerUuid()) && parentB.equals(childSnapshot.getParentVillager2Uuid());
            boolean matchesReverse = parentB.equals(childSnapshot.getParentVillagerUuid()) && parentA.equals(childSnapshot.getParentVillager2Uuid());
            if (matchesForward || matchesReverse) {
                children++;
                if (children >= Config.FAMILY_CHARM_MAX_CHILDREN_PER_PAIR.get()) {
                    return "slimpatch.message.family_charm_child_limit";
                }
            }
        }
        return null;
    }

    private static void linkSpouses(Villager firstParent, FamilyVillager firstFamilyVillager, Villager secondParent, FamilyVillager secondFamilyVillager) {
        firstFamilyVillager.setSpouseVillagerUuid(secondParent.getUUID());
        firstFamilyVillager.setSpouseVillagerName(secondParent.getName().getString());
        secondFamilyVillager.setSpouseVillagerUuid(firstParent.getUUID());
        secondFamilyVillager.setSpouseVillagerName(firstParent.getName().getString());
    }

    private static Villager createVillagerForGender(ServerLevel level, String gender) {
        EntityType<? extends Villager> entityType = "female".equals(BirthScreenData.normalizeGender(gender))
                ? ModEntities.FEMALE_VILLAGER.get()
                : ModEntities.MALE_VILLAGER.get();
        return entityType.create(level);
    }

    private static Vec3 findSpawnPosition(ServerLevel level, Entity entity, Villager mother, Villager otherParent) {
        Vec3[] anchors = new Vec3[]{mother.position(), otherParent.position()};
        for (Vec3 anchor : anchors) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = BlockPos.containing(anchor.x + dx, anchor.y, anchor.z + dz);
                    Vec3 spawnPos = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, mother.getYRot(), 0.0F);
                    if (level.noCollision(entity)) {
                        return spawnPos;
                    }
                    Vec3 raisedSpawnPos = spawnPos.add(0.0D, 1.0D, 0.0D);
                    entity.moveTo(raisedSpawnPos.x, raisedSpawnPos.y, raisedSpawnPos.z, mother.getYRot(), 0.0F);
                    if (level.noCollision(entity)) {
                        return raisedSpawnPos;
                    }
                }
            }
        }
        return mother.position().add(0.0D, 1.0D, 0.0D);
    }

    private static void applyChildData(ServerLevel level, Villager child, FamilyVillager childFamilyVillager, Villager mother, FamilyVillager motherFamilyVillager, Villager otherParent, String childGender) {
        String normalizedGender = BirthScreenData.normalizeGender(childGender);
        String name = "female".equals(normalizedGender)
                ? VillagerNameConfig.getRandomFemaleName(level.random)
                : VillagerNameConfig.getRandomMaleName(level.random);
        child.setCustomName(Component.literal(name));
        child.setCustomNameVisible(true);
        childFamilyVillager.setBornFromFamilySystem(false);
        childFamilyVillager.setParentPlayerUuid(null);
        childFamilyVillager.setParentPlayerName("");
        childFamilyVillager.setParentVillagerUuid(mother.getUUID());
        childFamilyVillager.setParentVillagerName(mother.getName().getString());
        childFamilyVillager.setParentVillager2Uuid(otherParent.getUUID());
        childFamilyVillager.setParentVillager2Name(otherParent.getName().getString());
        childFamilyVillager.setFamilyOwnerPlayerUuid(null);
        childFamilyVillager.setFamilyOwnerPlayerName("");
        childFamilyVillager.setNaturalFamilyMember(true);
        childFamilyVillager.setSpouseVillagerUuid(null);
        childFamilyVillager.setSpouseVillagerName("");
        childFamilyVillager.setRelationshipStage(VillagerRelationshipStage.FRIENDSHIP);
        UUID groupId = motherFamilyVillager.getNaturalFamilyGroupId();
        if (groupId == null && otherParent instanceof FamilyVillager otherParentFamilyVillager) {
            groupId = otherParentFamilyVillager.getNaturalFamilyGroupId();
        }
        if (groupId == null) {
            groupId = UUID.randomUUID();
        }
        motherFamilyVillager.setNaturalFamilyGroupId(groupId);
        motherFamilyVillager.setNaturalFamilyMember(true);
        if (otherParent instanceof FamilyVillager otherParentFamilyVillager) {
            otherParentFamilyVillager.setNaturalFamilyGroupId(groupId);
            otherParentFamilyVillager.setNaturalFamilyMember(true);
        }
        childFamilyVillager.setNaturalFamilyGroupId(groupId);
        FamilyAgingHandler.initializeForBirth(childFamilyVillager, level.getGameTime());
        VillagerPersonality personality = BirthScreenData.getRandomPersonality(level.random);
        String skinType = BirthScreenData.normalizeSkinType(WorldSkinData.get(level).getTheme());
        int skinId = BirthScreenData.getRandomSkinId(normalizedGender, skinType, level.random);
        String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(normalizedGender, skinType, skinId);
        if (child instanceof MaleVillagerEntity maleChild) {
            maleChild.setPersonality(personality);
            maleChild.setSkinIndex(skinId);
            maleChild.setSavedSkinInput("");
            maleChild.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
        } else if (child instanceof FemaleVillagerEntity femaleChild) {
            femaleChild.setPersonality(personality);
            femaleChild.setSkinIndex(skinId);
            femaleChild.setSavedSkinInput("");
            femaleChild.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
        }
    }

    private static void playFamilyCharmSuccessEffects(ServerLevel level, Villager firstParent, Villager secondParent) {
        if (level == null || firstParent == null || secondParent == null) {
            return;
        }
        Vec3 midpoint = firstParent.position().add(secondParent.position()).scale(0.5D);
        level.playSound(null, BlockPos.containing(midpoint), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.55F, 1.2F);
        level.sendParticles(ParticleTypes.HEART, midpoint.x, midpoint.y + 1.2D, midpoint.z, 6, 0.18D, 0.18D, 0.18D, 0.01D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, midpoint.x, midpoint.y + 0.9D, midpoint.z, 6, 0.22D, 0.2D, 0.22D, 0.01D);
    }

    private record SelectedParent(UUID parentVillagerUuid, ResourceKey<net.minecraft.world.level.Level> dimension, long selectedAt) {
    }

    private record ValidationResult(boolean valid, String messageKey, boolean clearSelection) {
        private static ValidationResult ok() {
            return new ValidationResult(true, "", false);
        }

        private static ValidationResult failure(String messageKey) {
            return new ValidationResult(false, messageKey, false);
        }

        private static ValidationResult failureAndClear(String messageKey) {
            return new ValidationResult(false, messageKey, true);
        }
    }
}
