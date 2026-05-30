package com.javic.slimpatch.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;

import java.util.UUID;

public final class VillagerRelationshipData {

    private static final String PLAYER_RELATIONSHIPS_TAG = "SlimPatchRelationships";
    private static final String CLIENT_RELATIONSHIP_TAG = "SlimPatchClientRelationship";
    private static final String PLAYER_GOLDEN_RELATIONSHIPS_TAG = "SlimPatchGoldenRelationships";
    private static final String CLIENT_GOLDEN_RELATIONSHIP_TAG = "SlimPatchClientGoldenRelationship";

    private VillagerRelationshipData() {
    }

    public static boolean usesPerPlayerRelationships(Villager villager) {
        return villager.getServer() != null && (villager.getServer().isDedicatedServer() || villager.getServer().isPublished());
    }

    public static float getRelationshipForPlayer(Villager villager, UUID playerUuid, float globalFallback) {
        float fallback = clamp(globalFallback);
        if (playerUuid == null || !usesPerPlayerRelationships(villager)) {
            return fallback;
        }

        CompoundTag relationships = villager.getPersistentData().getCompound(PLAYER_RELATIONSHIPS_TAG);
        String key = playerUuid.toString();
        if (relationships.contains(key)) {
            return clamp(relationships.getFloat(key));
        }

        relationships.putFloat(key, fallback);
        villager.getPersistentData().put(PLAYER_RELATIONSHIPS_TAG, relationships);
        return fallback;
    }

    public static float setRelationshipForPlayer(Villager villager, UUID playerUuid, float value, float globalFallback) {
        float clamped = clamp(value);
        if (playerUuid == null || !usesPerPlayerRelationships(villager)) {
            return clamped;
        }

        CompoundTag relationships = villager.getPersistentData().getCompound(PLAYER_RELATIONSHIPS_TAG);
        relationships.putFloat(playerUuid.toString(), clamped);
        villager.getPersistentData().put(PLAYER_RELATIONSHIPS_TAG, relationships);
        return clamped;
    }

    public static float getDisplayedRelationship(Villager villager, float fallback) {
        CompoundTag data = villager.getPersistentData();
        if (data.contains(CLIENT_RELATIONSHIP_TAG)) {
            return clamp(data.getFloat(CLIENT_RELATIONSHIP_TAG));
        }
        return clamp(fallback);
    }

    public static float getGoldenRelationshipForPlayer(Villager villager, UUID playerUuid, float globalFallback) {
        float fallback = clampGolden(globalFallback);
        if (playerUuid == null || !usesPerPlayerRelationships(villager)) {
            return fallback;
        }

        CompoundTag relationships = villager.getPersistentData().getCompound(PLAYER_GOLDEN_RELATIONSHIPS_TAG);
        String key = playerUuid.toString();
        if (relationships.contains(key)) {
            return clampGolden(relationships.getFloat(key));
        }

        relationships.putFloat(key, fallback);
        villager.getPersistentData().put(PLAYER_GOLDEN_RELATIONSHIPS_TAG, relationships);
        return fallback;
    }

    public static float setGoldenRelationshipForPlayer(Villager villager, UUID playerUuid, float value, float globalFallback) {
        float clamped = clampGolden(value);
        if (playerUuid == null || !usesPerPlayerRelationships(villager)) {
            return clamped;
        }

        CompoundTag relationships = villager.getPersistentData().getCompound(PLAYER_GOLDEN_RELATIONSHIPS_TAG);
        relationships.putFloat(playerUuid.toString(), clamped);
        villager.getPersistentData().put(PLAYER_GOLDEN_RELATIONSHIPS_TAG, relationships);
        return clamped;
    }

    public static float getDisplayedGoldenRelationship(Villager villager, float fallback) {
        CompoundTag data = villager.getPersistentData();
        if (data.contains(CLIENT_GOLDEN_RELATIONSHIP_TAG)) {
            return clampGolden(data.getFloat(CLIENT_GOLDEN_RELATIONSHIP_TAG));
        }
        return clampGolden(fallback);
    }

    public static void setClientRelationship(Villager villager, float relationship) {
        villager.getPersistentData().putFloat(CLIENT_RELATIONSHIP_TAG, clamp(relationship));
    }

    public static void setClientGoldenRelationship(Villager villager, float goldenRelationship) {
        villager.getPersistentData().putFloat(CLIENT_GOLDEN_RELATIONSHIP_TAG, clampGolden(goldenRelationship));
    }

    public static void load(CompoundTag tag, Villager villager) {
        if (tag.contains(PLAYER_RELATIONSHIPS_TAG)) {
            villager.getPersistentData().put(PLAYER_RELATIONSHIPS_TAG, tag.getCompound(PLAYER_RELATIONSHIPS_TAG).copy());
        }
        if (tag.contains(PLAYER_GOLDEN_RELATIONSHIPS_TAG)) {
            villager.getPersistentData().put(PLAYER_GOLDEN_RELATIONSHIPS_TAG, tag.getCompound(PLAYER_GOLDEN_RELATIONSHIPS_TAG).copy());
        }
    }

    public static void save(CompoundTag tag, Villager villager) {
        CompoundTag data = villager.getPersistentData();
        if (data.contains(PLAYER_RELATIONSHIPS_TAG)) {
            tag.put(PLAYER_RELATIONSHIPS_TAG, data.getCompound(PLAYER_RELATIONSHIPS_TAG).copy());
        }
        if (data.contains(PLAYER_GOLDEN_RELATIONSHIPS_TAG)) {
            tag.put(PLAYER_GOLDEN_RELATIONSHIPS_TAG, data.getCompound(PLAYER_GOLDEN_RELATIONSHIPS_TAG).copy());
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(5.0f, value));
    }

    private static float clampGolden(float value) {
        return VillagerFamilyData.clampGoldenRelationship(value);
    }
}
