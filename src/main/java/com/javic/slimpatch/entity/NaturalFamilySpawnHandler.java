package com.javic.slimpatch.entity;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.config.VillagerNameConfig;
import com.javic.slimpatch.data.WorldSkinData;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class NaturalFamilySpawnHandler {

    private static final double NATURAL_FAMILY_CHANCE = 0.35D;
    private static final double LOCAL_RADIUS = 48.0D;
    private static final int LOCAL_VILLAGER_CAP = 28;
    private static final int LOCAL_NATURAL_FAMILY_GROUP_CAP = 3;
    private static final int MIN_CHILDREN = 1;
    private static final int MAX_CHILDREN = 2;
    public static final String SKIP_NATURAL_FAMILY_SPAWN_TAG = "slimpatch_skip_natural_family_spawn";
    private static final String NATURAL_FAMILY_SPAWN_PROCESSED_TAG = "slimpatch_natural_family_spawn_processed";

    private NaturalFamilySpawnHandler() {
    }

    public static void maybeSpawnNaturalFamily(Villager anchor, FamilyVillager anchorFamilyVillager, ServerLevelAccessor levelAccessor, MobSpawnType reason) {
        if (anchor == null || anchorFamilyVillager == null || levelAccessor == null || levelAccessor.isClientSide()) {
            return;
        }
        if (!(levelAccessor.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (reason != MobSpawnType.NATURAL) {
            return;
        }
        if (!anchor.isAlive()) {
            return;
        }
        if (anchorFamilyVillager.getAgeStage() != VillagerAgeStage.ADULT) {
            return;
        }
        if (anchorFamilyVillager.isNaturalFamilyMember()) {
            return;
        }
        if (anchorFamilyVillager.getNaturalFamilyGroupId() != null) {
            return;
        }
        if (anchor.getPersistentData().getBoolean("slimpatch_vanilla_replacement_loaded_from_disk")) {
            return;
        }
        if (anchor.getPersistentData().getBoolean(SKIP_NATURAL_FAMILY_SPAWN_TAG)) {
            return;
        }
        if (anchor.getPersistentData().getBoolean(NATURAL_FAMILY_SPAWN_PROCESSED_TAG)) {
            return;
        }
        if (countNearbyCustomVillagers(serverLevel, anchor) >= LOCAL_VILLAGER_CAP) {
            return;
        }
        if (countNearbyNaturalFamilyGroups(serverLevel, anchor) >= LOCAL_NATURAL_FAMILY_GROUP_CAP) {
            return;
        }

        RandomSource random = anchor.getRandom();
        if (random.nextDouble() >= NATURAL_FAMILY_CHANCE) {
            return;
        }

        PreparedFamily preparedFamily = prepareFamily(serverLevel, anchor, random);
        if (preparedFamily == null || preparedFamily.children().size() < MIN_CHILDREN) {
            return;
        }
        if (countNearbyCustomVillagers(serverLevel, anchor) + 1 + preparedFamily.children().size() > LOCAL_VILLAGER_CAP) {
            preparedFamily.spouse().discard();
            preparedFamily.children().forEach(Entity::discard);
            return;
        }

        List<Villager> addedVillagers = new ArrayList<>();
        if (!serverLevel.addFreshEntity(preparedFamily.spouse())) {
            preparedFamily.spouse().discard();
            preparedFamily.children().forEach(Entity::discard);
            return;
        }
        addedVillagers.add(preparedFamily.spouse());

        for (Villager child : preparedFamily.children()) {
            if (!serverLevel.addFreshEntity(child)) {
                child.discard();
                for (Villager addedVillager : addedVillagers) {
                    addedVillager.discard();
                    FamilyTreeTracker.markVillagerDead(serverLevel.getServer(), addedVillager.getUUID());
                }
                for (Villager pendingChild : preparedFamily.children()) {
                    if (pendingChild != child && !addedVillagers.contains(pendingChild)) {
                        pendingChild.discard();
                    }
                }
                return;
            }
            addedVillagers.add(child);
        }

        UUID groupId = preparedFamily.groupId();
        anchorFamilyVillager.setNaturalFamilyGroupId(groupId);
        anchorFamilyVillager.setNaturalFamilyMember(true);
        anchorFamilyVillager.setSpouseVillagerUuid(preparedFamily.spouse().getUUID());
        anchorFamilyVillager.setSpouseVillagerName(resolveVillagerName(preparedFamily.spouse()));
        anchorFamilyVillager.setParentPlayerUuid(null);
        anchorFamilyVillager.setParentPlayerName("");
        anchorFamilyVillager.setFamilyOwnerPlayerUuid(null);
        anchorFamilyVillager.setFamilyOwnerPlayerName("");
        anchor.getPersistentData().putBoolean(NATURAL_FAMILY_SPAWN_PROCESSED_TAG, true);

        FamilyTreeTracker.upsertVillager(serverLevel.getServer(), anchor);
        FamilyTreeTracker.upsertVillager(serverLevel.getServer(), preparedFamily.spouse());
        for (Villager child : preparedFamily.children()) {
            FamilyTreeTracker.upsertVillager(serverLevel.getServer(), child);
        }
    }

    private static PreparedFamily prepareFamily(ServerLevel serverLevel, Villager anchor, RandomSource random) {
        FamilyVillager spouseFamilyVillager;
        Villager spouse = createSpouse(serverLevel, anchor);
        if (spouse == null || !(spouse instanceof FamilyVillager createdSpouseFamilyVillager)) {
            return null;
        }
        spouseFamilyVillager = createdSpouseFamilyVillager;

        UUID groupId = UUID.randomUUID();
        applyParentFamilyData(spouseFamilyVillager, groupId, anchor.getUUID(), resolveVillagerName(anchor));

        int targetChildren = MIN_CHILDREN + random.nextInt(MAX_CHILDREN - MIN_CHILDREN + 1);
        List<Villager> children = new ArrayList<>();
        for (int i = 0; i < targetChildren; i++) {
            Villager child = createChild(serverLevel, anchor, spouse, groupId, random);
            if (child != null) {
                children.add(child);
            }
        }

        if (children.size() < MIN_CHILDREN) {
            spouse.discard();
            children.forEach(Entity::discard);
            return null;
        }

        return new PreparedFamily(groupId, spouse, children);
    }

    private static Villager createSpouse(ServerLevel serverLevel, Villager anchor) {
        Villager spouse = createOppositeGenderVillager(serverLevel, anchor);
        if (spouse == null) {
            return null;
        }
        if (!(spouse instanceof FamilyVillager spouseFamilyVillager)) {
            return null;
        }

        Vec3 spawnPos = findSpawnPosition(serverLevel, spouse, List.of(anchor.position()), anchor.getYRot());
        if (spawnPos == null) {
            return null;
        }

        spouse.getPersistentData().putBoolean(SKIP_NATURAL_FAMILY_SPAWN_TAG, true);
        spouseFamilyVillager.setNaturalFamilyMember(true);
        spouse.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, anchor.getYRot(), anchor.getXRot());
        spouse.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(spawnPos)), MobSpawnType.NATURAL, null);
        applyVillagerType(spouse, anchor.getVillagerData().getType());
        if (!serverLevel.noCollision(spouse)) {
            return null;
        }
        return spouse;
    }

    private static Villager createChild(ServerLevel serverLevel, Villager anchor, Villager spouse, UUID groupId, RandomSource random) {
        String childGender = random.nextBoolean() ? "male" : "female";
        Villager child = createVillagerForGender(serverLevel, childGender);
        if (child == null || !(child instanceof FamilyVillager childFamilyVillager)) {
            return null;
        }

        Vec3 spawnPos = findSpawnPosition(serverLevel, child, List.of(anchor.position(), spouse.position()), anchor.getYRot());
        if (spawnPos == null) {
            return null;
        }

        child.getPersistentData().putBoolean(SKIP_NATURAL_FAMILY_SPAWN_TAG, true);
        childFamilyVillager.setNaturalFamilyMember(true);
        child.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, anchor.getYRot(), anchor.getXRot());
        child.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(spawnPos)), MobSpawnType.NATURAL, null);
        applyVillagerType(child, anchor.getVillagerData().getType());
        applyNaturalChildData(serverLevel, child, childFamilyVillager, anchor, spouse, groupId, childGender, random);
        return serverLevel.noCollision(child) ? child : null;
    }

    private static Villager createOppositeGenderVillager(ServerLevel serverLevel, Villager anchor) {
        if (anchor instanceof MaleVillagerEntity) {
            return ModEntities.FEMALE_VILLAGER.get().create(serverLevel);
        }
        if (anchor instanceof FemaleVillagerEntity) {
            return ModEntities.MALE_VILLAGER.get().create(serverLevel);
        }
        return null;
    }

    private static Villager createVillagerForGender(ServerLevel serverLevel, String gender) {
        EntityType<? extends Villager> entityType = "female".equals(BirthScreenData.normalizeGender(gender))
                ? ModEntities.FEMALE_VILLAGER.get()
                : ModEntities.MALE_VILLAGER.get();
        return entityType.create(serverLevel);
    }

    private static void applyParentFamilyData(FamilyVillager parent, UUID groupId, UUID spouseUuid, String spouseName) {
        parent.setNaturalFamilyGroupId(groupId);
        parent.setNaturalFamilyMember(true);
        parent.setSpouseVillagerUuid(spouseUuid);
        parent.setSpouseVillagerName(spouseName);
        parent.setParentPlayerUuid(null);
        parent.setParentPlayerName("");
        parent.setFamilyOwnerPlayerUuid(null);
        parent.setFamilyOwnerPlayerName("");
    }

    private static void applyNaturalChildData(ServerLevel serverLevel, Villager child, FamilyVillager childFamilyVillager, Villager parent1, Villager parent2, UUID groupId, String childGender, RandomSource random) {
        String normalizedGender = BirthScreenData.normalizeGender(childGender);
        String name = "female".equals(normalizedGender)
                ? VillagerNameConfig.getRandomFemaleName(random)
                : VillagerNameConfig.getRandomMaleName(random);
        child.setCustomName(Component.literal(name));
        child.setCustomNameVisible(true);

        VillagerAgeStage ageStage = switch (random.nextInt(3)) {
            case 0 -> VillagerAgeStage.TODDLER;
            case 1 -> VillagerAgeStage.CHILD;
            default -> VillagerAgeStage.TEEN;
        };
        childFamilyVillager.setAgeStage(ageStage);
        FamilyAgingHandler.setAgeStageWithTimer(childFamilyVillager, ageStage, serverLevel.getGameTime());
        childFamilyVillager.setNaturalFamilyGroupId(groupId);
        childFamilyVillager.setNaturalFamilyMember(true);
        childFamilyVillager.setParentVillagerUuid(parent1.getUUID());
        childFamilyVillager.setParentVillagerName(resolveVillagerName(parent1));
        childFamilyVillager.setParentVillager2Uuid(parent2.getUUID());
        childFamilyVillager.setParentVillager2Name(resolveVillagerName(parent2));
        childFamilyVillager.setParentPlayerUuid(null);
        childFamilyVillager.setParentPlayerName("");
        childFamilyVillager.setFamilyOwnerPlayerUuid(null);
        childFamilyVillager.setFamilyOwnerPlayerName("");
        childFamilyVillager.setSpouseVillagerUuid(null);
        childFamilyVillager.setSpouseVillagerName("");
        childFamilyVillager.setBornFromFamilySystem(false);
        childFamilyVillager.setRelationshipStage(VillagerRelationshipStage.FRIENDSHIP);

        VillagerPersonality personality = BirthScreenData.getRandomPersonality(random);
        if (child instanceof MaleVillagerEntity maleChild) {
            maleChild.setPersonality(personality);
            applyChildSkin(serverLevel, maleChild, normalizedGender, random);
        } else if (child instanceof FemaleVillagerEntity femaleChild) {
            femaleChild.setPersonality(personality);
            applyChildSkin(serverLevel, femaleChild, normalizedGender, random);
        }
    }

    private static void applyChildSkin(ServerLevel serverLevel, MaleVillagerEntity child, String gender, RandomSource random) {
        String skinType = BirthScreenData.normalizeSkinType(WorldSkinData.get(serverLevel).getTheme());
        int skinId = BirthScreenData.getRandomSkinId(gender, skinType, random);
        String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(gender, skinType, skinId);
        child.setSkinIndex(skinId);
        child.setSavedSkinInput("");
        child.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
    }

    private static void applyChildSkin(ServerLevel serverLevel, FemaleVillagerEntity child, String gender, RandomSource random) {
        String skinType = BirthScreenData.normalizeSkinType(WorldSkinData.get(serverLevel).getTheme());
        int skinId = BirthScreenData.getRandomSkinId(gender, skinType, random);
        String curatedSkinPath = BirthScreenData.getCuratedSkinResourcePath(gender, skinType, skinId);
        child.setSkinIndex(skinId);
        child.setSavedSkinInput("");
        child.setCustomSkinPath(curatedSkinPath == null ? "" : curatedSkinPath);
    }

    private static void applyVillagerType(Villager villager, VillagerType villagerType) {
        if (villager == null || villagerType == null) {
            return;
        }
        VillagerData villagerData = villager.getVillagerData();
        villager.setVillagerData(villagerData.setType(villagerType));
    }

    private static int countNearbyCustomVillagers(ServerLevel serverLevel, Villager anchor) {
        AABB bounds = anchor.getBoundingBox().inflate(LOCAL_RADIUS, 16.0D, LOCAL_RADIUS);
        int count = serverLevel.getEntitiesOfClass(MaleVillagerEntity.class, bounds).size();
        count += serverLevel.getEntitiesOfClass(FemaleVillagerEntity.class, bounds).size();
        return count;
    }

    private static int countNearbyNaturalFamilyGroups(ServerLevel serverLevel, Villager anchor) {
        AABB bounds = anchor.getBoundingBox().inflate(LOCAL_RADIUS, 16.0D, LOCAL_RADIUS);
        Set<UUID> groupIds = new HashSet<>();
        for (MaleVillagerEntity villager : serverLevel.getEntitiesOfClass(MaleVillagerEntity.class, bounds)) {
            UUID groupId = villager.getNaturalFamilyGroupId();
            if (groupId != null) {
                groupIds.add(groupId);
            }
        }
        for (FemaleVillagerEntity villager : serverLevel.getEntitiesOfClass(FemaleVillagerEntity.class, bounds)) {
            UUID groupId = villager.getNaturalFamilyGroupId();
            if (groupId != null) {
                groupIds.add(groupId);
            }
        }
        return groupIds.size();
    }

    private static Vec3 findSpawnPosition(ServerLevel serverLevel, Entity entity, List<Vec3> anchors, float yRot) {
        for (Vec3 anchor : anchors) {
            for (int radius = 1; radius <= 3; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        for (int dy = 0; dy <= 1; dy++) {
                            BlockPos pos = BlockPos.containing(anchor.x + dx, anchor.y + dy, anchor.z + dz);
                            Vec3 spawnPos = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                            entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yRot, 0.0F);
                            if (serverLevel.noCollision(entity)) {
                                return spawnPos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String resolveVillagerName(Villager villager) {
        if (villager == null) {
            return "";
        }
        Component customName = villager.getCustomName();
        if (customName != null && !customName.getString().isEmpty()) {
            return customName.getString();
        }
        return villager.getName().getString();
    }

    private record PreparedFamily(UUID groupId, Villager spouse, List<Villager> children) {
    }
}
