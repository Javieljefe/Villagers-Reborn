package com.javic.slimpatch.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class VillagerFamilyData {

    public static final String GOLDEN_RELATIONSHIP_TAG = "SlimPatchGoldenRelationship";
    public static final String SPOUSE_PLAYER_UUID_TAG = "SlimPatchSpousePlayerUuid";
    public static final String SPOUSE_PLAYER_NAME_TAG = "SlimPatchSpousePlayerName";
    public static final String FORMER_SPOUSE_PLAYER_UUID_TAG = "SlimPatchFormerSpousePlayerUuid";
    public static final String FORMER_SPOUSE_PLAYER_NAME_TAG = "SlimPatchFormerSpousePlayerName";
    public static final String SPOUSE_VILLAGER_UUID_TAG = "SlimPatchSpouseVillagerUuid";
    public static final String SPOUSE_VILLAGER_NAME_TAG = "SlimPatchSpouseVillagerName";
    public static final String RELATIONSHIP_STAGE_TAG = "SlimPatchRelationshipStage";
    public static final String AGE_STAGE_TAG = "SlimPatchAgeStage";
    public static final String AGE_STAGE_STARTED_AT_TAG = "SlimPatchAgeStageStartedAt";
    public static final String NEXT_AGE_STAGE_AT_TAG = "SlimPatchNextAgeStageAt";
    public static final String EXPECTING_CHILD_TAG = "SlimPatchExpectingChild";
    public static final String EXPECTING_PARENT_PLAYER_UUID_TAG = "SlimPatchExpectingParentPlayerUuid";
    public static final String EXPECTING_PARENT_PLAYER_NAME_TAG = "SlimPatchExpectingParentPlayerName";
    public static final String EXPECTING_OTHER_PARENT_VILLAGER_UUID_TAG = "SlimPatchExpectingOtherParentVillagerUuid";
    public static final String EXPECTING_OTHER_PARENT_VILLAGER_NAME_TAG = "SlimPatchExpectingOtherParentVillagerName";
    public static final String FORCED_BABY_GENDER_TAG = "SlimPatchForcedBabyGender";
    public static final String EXPECTING_STARTED_AT_TAG = "SlimPatchExpectingStartedAt";
    public static final String EXPECTING_ENDS_AT_TAG = "SlimPatchExpectingEndsAt";
    public static final String BORN_FROM_FAMILY_SYSTEM_TAG = "SlimPatchBornFromFamilySystem";
    public static final String PARENT_PLAYER_UUID_TAG = "SlimPatchParentPlayerUuid";
    public static final String PARENT_PLAYER_NAME_TAG = "SlimPatchParentPlayerName";
    public static final String PARENT_VILLAGER_UUID_TAG = "SlimPatchParentVillagerUuid";
    public static final String PARENT_VILLAGER_NAME_TAG = "SlimPatchParentVillagerName";
    public static final String PARENT_VILLAGER_2_UUID_TAG = "SlimPatchParentVillager2Uuid";
    public static final String PARENT_VILLAGER_2_NAME_TAG = "SlimPatchParentVillager2Name";
    public static final String FAMILY_OWNER_PLAYER_UUID_TAG = "SlimPatchFamilyOwnerPlayerUuid";
    public static final String FAMILY_OWNER_PLAYER_NAME_TAG = "SlimPatchFamilyOwnerPlayerName";
    public static final String NATURAL_FAMILY_GROUP_ID_TAG = "SlimPatchNaturalFamilyGroupId";
    public static final String NATURAL_FAMILY_MEMBER_TAG = "SlimPatchNaturalFamilyMember";

    private VillagerFamilyData() {
    }

    public static float clampGoldenRelationship(float value) {
        return Math.max(0.0F, Math.min(5.0F, value));
    }

    public static UUID readSpousePlayerUuid(CompoundTag tag) {
        return readUuid(tag, SPOUSE_PLAYER_UUID_TAG);
    }

    public static void writeSpousePlayerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, SPOUSE_PLAYER_UUID_TAG, uuid);
    }

    public static UUID readFormerSpousePlayerUuid(CompoundTag tag) {
        return readUuid(tag, FORMER_SPOUSE_PLAYER_UUID_TAG);
    }

    public static void writeFormerSpousePlayerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, FORMER_SPOUSE_PLAYER_UUID_TAG, uuid);
    }

    public static UUID readSpouseVillagerUuid(CompoundTag tag) {
        return readUuid(tag, SPOUSE_VILLAGER_UUID_TAG);
    }

    public static void writeSpouseVillagerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, SPOUSE_VILLAGER_UUID_TAG, uuid);
    }

    public static UUID readExpectingParentPlayerUuid(CompoundTag tag) {
        return readUuid(tag, EXPECTING_PARENT_PLAYER_UUID_TAG);
    }

    public static void writeExpectingParentPlayerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, EXPECTING_PARENT_PLAYER_UUID_TAG, uuid);
    }

    public static UUID readParentPlayerUuid(CompoundTag tag) {
        return readUuid(tag, PARENT_PLAYER_UUID_TAG);
    }

    public static void writeParentPlayerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, PARENT_PLAYER_UUID_TAG, uuid);
    }

    public static UUID readParentVillagerUuid(CompoundTag tag) {
        return readUuid(tag, PARENT_VILLAGER_UUID_TAG);
    }

    public static UUID readExpectingOtherParentVillagerUuid(CompoundTag tag) {
        return readUuid(tag, EXPECTING_OTHER_PARENT_VILLAGER_UUID_TAG);
    }

    public static void writeExpectingOtherParentVillagerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, EXPECTING_OTHER_PARENT_VILLAGER_UUID_TAG, uuid);
    }

    public static void writeParentVillagerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, PARENT_VILLAGER_UUID_TAG, uuid);
    }

    public static UUID readParentVillager2Uuid(CompoundTag tag) {
        return readUuid(tag, PARENT_VILLAGER_2_UUID_TAG);
    }

    public static void writeParentVillager2Uuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, PARENT_VILLAGER_2_UUID_TAG, uuid);
    }

    public static UUID readFamilyOwnerPlayerUuid(CompoundTag tag) {
        return readUuid(tag, FAMILY_OWNER_PLAYER_UUID_TAG);
    }

    public static void writeFamilyOwnerPlayerUuid(CompoundTag tag, UUID uuid) {
        writeUuid(tag, FAMILY_OWNER_PLAYER_UUID_TAG, uuid);
    }

    public static UUID readNaturalFamilyGroupId(CompoundTag tag) {
        return readUuid(tag, NATURAL_FAMILY_GROUP_ID_TAG);
    }

    public static void writeNaturalFamilyGroupId(CompoundTag tag, UUID uuid) {
        writeUuid(tag, NATURAL_FAMILY_GROUP_ID_TAG, uuid);
    }

    public static String readOptionalString(CompoundTag tag, String key) {
        return tag.contains(key) ? tag.getString(key) : "";
    }

    public static void writeOptionalString(CompoundTag tag, String key, String value) {
        if (value == null || value.isEmpty()) {
            tag.remove(key);
        } else {
            tag.putString(key, value);
        }
    }

    public static VillagerAgeStage parseAgeStage(String value) {
        if (value == null || value.isEmpty()) {
            return VillagerAgeStage.ADULT;
        }
        try {
            return VillagerAgeStage.valueOf(value);
        } catch (IllegalArgumentException e) {
            return VillagerAgeStage.ADULT;
        }
    }

    public static VillagerRelationshipStage parseRelationshipStage(String value) {
        if (value == null || value.isEmpty()) {
            return VillagerRelationshipStage.FRIENDSHIP;
        }
        try {
            return VillagerRelationshipStage.valueOf(value);
        } catch (IllegalArgumentException e) {
            return VillagerRelationshipStage.FRIENDSHIP;
        }
    }

    public static boolean isUnderage(FamilyVillager familyVillager) {
        return familyVillager != null && familyVillager.getAgeStage() != VillagerAgeStage.ADULT;
    }

    public static float getAgeStagePitch(FamilyVillager familyVillager) {
        if (familyVillager == null) {
            return 1.0F;
        }
        return switch (familyVillager.getAgeStage()) {
            case TODDLER -> 1.45F;
            case CHILD -> 1.28F;
            case TEEN -> 1.12F;
            case ADULT -> 1.0F;
        };
    }

    public static boolean isBornFromFamilySystem(Villager villager) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return false;
        }
        return familyVillager.isBornFromFamilySystem()
                || familyVillager.getParentPlayerUuid() != null
                || !familyVillager.getParentPlayerName().isEmpty()
                || familyVillager.getParentVillagerUuid() != null
                || !familyVillager.getParentVillagerName().isEmpty()
                || familyVillager.getFamilyOwnerPlayerUuid() != null
                || !familyVillager.getFamilyOwnerPlayerName().isEmpty();
    }

    public static boolean isFamilyChildOf(Villager villager, Player player) {
        if (!(villager instanceof FamilyVillager familyVillager) || player == null || !isBornFromFamilySystem(villager)) {
            return false;
        }
        UUID parentPlayerUuid = familyVillager.getParentPlayerUuid();
        if (parentPlayerUuid != null) {
            return parentPlayerUuid.equals(player.getUUID());
        }
        String parentPlayerName = familyVillager.getParentPlayerName();
        return !parentPlayerName.isEmpty() && parentPlayerName.equals(player.getGameProfile().getName());
    }

    public static boolean canUseRomanticInteraction(Villager villager, Player player) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return true;
        }
        if (isUnderage(familyVillager)) {
            return false;
        }
        return !isFamilyChildOf(villager, player);
    }

    public static boolean canHaveRomanticPersonality(FamilyVillager familyVillager) {
        return !isUnderage(familyVillager);
    }

    public static boolean hasSpouseVillager(FamilyVillager familyVillager) {
        return familyVillager != null && familyVillager.getSpouseVillagerUuid() != null;
    }

    public static boolean hasSecondVillagerParent(FamilyVillager familyVillager) {
        return familyVillager != null && familyVillager.getParentVillager2Uuid() != null;
    }

    public static VillagerPersonality sanitizePersonality(FamilyVillager familyVillager, VillagerPersonality personality) {
        if (personality == null) {
            return VillagerPersonality.FRIENDLY;
        }
        if (personality == VillagerPersonality.ROMANTIC && !canHaveRomanticPersonality(familyVillager)) {
            return VillagerPersonality.FRIENDLY;
        }
        return personality;
    }

    public static boolean canStartDating(FamilyVillager villager, Villager entity, Player player, float relationship) {
        return villager != null
                && entity != null
                && canUseRomanticInteraction(entity, player)
                && villager.getRelationshipStage() == VillagerRelationshipStage.FRIENDSHIP
                && !villager.hasSpouse()
                && relationship >= 5.0F;
    }

    public static boolean canStartDating(FamilyVillager villager, float relationship) {
        return villager != null
                && !isUnderage(villager)
                && villager.getRelationshipStage() == VillagerRelationshipStage.FRIENDSHIP
                && !villager.hasSpouse()
                && relationship >= 5.0F;
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        if (tag.hasUUID(key)) {
            return tag.getUUID(key);
        }
        if (tag.contains(key)) {
            String value = tag.getString(key);
            if (value != null && !value.isEmpty()) {
                try {
                    return UUID.fromString(value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static void writeUuid(CompoundTag tag, String key, UUID uuid) {
        if (uuid == null) {
            tag.remove(key);
        } else {
            tag.putUUID(key, uuid);
        }
    }
}
