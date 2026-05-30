package com.javic.slimpatch.entity;

import java.util.UUID;

public interface FamilyVillager {
    float getGoldenRelationship();

    void setGoldenRelationship(float value);

    UUID getSpousePlayerUuid();

    void setSpousePlayerUuid(UUID uuid);

    String getSpousePlayerName();

    void setSpousePlayerName(String name);

    UUID getFormerSpousePlayerUuid();

    void setFormerSpousePlayerUuid(UUID uuid);

    String getFormerSpousePlayerName();

    void setFormerSpousePlayerName(String name);

    UUID getSpouseVillagerUuid();

    void setSpouseVillagerUuid(UUID uuid);

    String getSpouseVillagerName();

    void setSpouseVillagerName(String name);

    VillagerRelationshipStage getRelationshipStage();

    void setRelationshipStage(VillagerRelationshipStage relationshipStage);

    VillagerAgeStage getAgeStage();

    void setAgeStage(VillagerAgeStage ageStage);

    long getAgeStageStartedAt();

    void setAgeStageStartedAt(long startedAt);

    long getNextAgeStageAt();

    void setNextAgeStageAt(long nextAgeStageAt);

    boolean isExpectingChild();

    void setExpectingChild(boolean expectingChild);

    UUID getExpectingParentPlayerUuid();

    void setExpectingParentPlayerUuid(UUID uuid);

    String getExpectingParentPlayerName();

    void setExpectingParentPlayerName(String name);

    UUID getExpectingOtherParentVillagerUuid();

    void setExpectingOtherParentVillagerUuid(UUID uuid);

    String getExpectingOtherParentVillagerName();

    void setExpectingOtherParentVillagerName(String name);

    String getForcedBabyGender();

    void setForcedBabyGender(String gender);

    long getExpectingStartedAt();

    void setExpectingStartedAt(long startedAt);

    long getExpectingEndsAt();

    void setExpectingEndsAt(long endsAt);

    boolean isBornFromFamilySystem();

    void setBornFromFamilySystem(boolean bornFromFamilySystem);

    UUID getParentPlayerUuid();

    void setParentPlayerUuid(UUID uuid);

    String getParentPlayerName();

    void setParentPlayerName(String name);

    UUID getParentVillagerUuid();

    void setParentVillagerUuid(UUID uuid);

    String getParentVillagerName();

    void setParentVillagerName(String name);

    UUID getParentVillager2Uuid();

    void setParentVillager2Uuid(UUID uuid);

    String getParentVillager2Name();

    void setParentVillager2Name(String name);

    UUID getNaturalFamilyGroupId();

    void setNaturalFamilyGroupId(UUID uuid);

    boolean isNaturalFamilyMember();

    void setNaturalFamilyMember(boolean naturalFamilyMember);

    UUID getFamilyOwnerPlayerUuid();

    void setFamilyOwnerPlayerUuid(UUID uuid);

    String getFamilyOwnerPlayerName();

    void setFamilyOwnerPlayerName(String name);

    default boolean hasSpouse() {
        return getSpousePlayerUuid() != null;
    }

    default boolean isDating() {
        return getRelationshipStage() == VillagerRelationshipStage.DATING;
    }

    default boolean isMarried() {
        return getRelationshipStage() == VillagerRelationshipStage.MARRIED;
    }
}
