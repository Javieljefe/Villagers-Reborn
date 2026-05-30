package com.javic.slimpatch.familytree;

import com.javic.slimpatch.entity.VillagerAgeStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class FamilyTreeVillagerSnapshot {

    private UUID villagerUuid;
    private String name = "";
    private String gender = "";
    private VillagerAgeStage ageStage = VillagerAgeStage.ADULT;
    private boolean alive = true;
    private boolean bornFromFamilySystem;
    private int skinIndex;
    private String skinResourcePath = "";
    private String savedSkinInput = "";
    private boolean hasCustomSkin;
    private UUID parentPlayerUuid;
    private String parentPlayerName = "";
    private UUID parentVillagerUuid;
    private String parentVillagerName = "";
    private UUID spousePlayerUuid;
    private String spousePlayerName = "";
    private UUID formerSpousePlayerUuid;
    private String formerSpousePlayerName = "";
    private UUID spouseVillagerUuid;
    private String spouseVillagerName = "";
    private final Set<UUID> childVillagerUuids = new LinkedHashSet<>();
    private UUID parentVillager2Uuid;
    private String parentVillager2Name = "";
    private UUID naturalFamilyGroupId;
    private boolean naturalFamilyMember;
    private String lastKnownDimension = "";
    private int lastKnownX;
    private int lastKnownY;
    private int lastKnownZ;

    public FamilyTreeVillagerSnapshot() {
    }

    public FamilyTreeVillagerSnapshot(UUID villagerUuid) {
        this.villagerUuid = villagerUuid;
    }

    public static FamilyTreeVillagerSnapshot load(CompoundTag tag) {
        FamilyTreeVillagerSnapshot snapshot = new FamilyTreeVillagerSnapshot(readUuid(tag, "Uuid"));
        snapshot.setName(tag.contains("Name") ? tag.getString("Name") : "");
        snapshot.setGender(tag.contains("Gender") ? tag.getString("Gender") : "");
        snapshot.setAgeStage(parseAgeStage(tag.contains("AgeStage") ? tag.getString("AgeStage") : ""));
        snapshot.setAlive(!tag.contains("Alive") || tag.getBoolean("Alive"));
        snapshot.setBornFromFamilySystem(tag.getBoolean("BornFromFamilySystem"));
        snapshot.setSkinIndex(tag.contains("SkinIndex") ? tag.getInt("SkinIndex") : 0);
        snapshot.setSkinResourcePath(tag.contains("SkinResourcePath") ? tag.getString("SkinResourcePath") : "");
        snapshot.setSavedSkinInput(tag.contains("SavedSkinInput") ? tag.getString("SavedSkinInput") : "");
        snapshot.setHasCustomSkin(tag.getBoolean("HasCustomSkin"));
        snapshot.setParentPlayerUuid(readUuid(tag, "ParentPlayerUuid"));
        snapshot.setParentPlayerName(tag.contains("ParentPlayerName") ? tag.getString("ParentPlayerName") : "");
        snapshot.setParentVillagerUuid(readUuid(tag, "ParentVillagerUuid"));
        snapshot.setParentVillagerName(tag.contains("ParentVillagerName") ? tag.getString("ParentVillagerName") : "");
        snapshot.setSpousePlayerUuid(readUuid(tag, "SpousePlayerUuid"));
        snapshot.setSpousePlayerName(tag.contains("SpousePlayerName") ? tag.getString("SpousePlayerName") : "");
        snapshot.setFormerSpousePlayerUuid(readUuid(tag, "FormerSpousePlayerUuid"));
        snapshot.setFormerSpousePlayerName(tag.contains("FormerSpousePlayerName") ? tag.getString("FormerSpousePlayerName") : "");
        snapshot.setSpouseVillagerUuid(readUuid(tag, "SpouseVillagerUuid"));
        snapshot.setSpouseVillagerName(tag.contains("SpouseVillagerName") ? tag.getString("SpouseVillagerName") : "");
        if (tag.contains("ChildVillagerUuids")) {
            ListTag children = tag.getList("ChildVillagerUuids", 8);
            for (int i = 0; i < children.size(); i++) {
                String value = children.getString(i);
                if (!value.isEmpty()) {
                    try {
                        snapshot.childVillagerUuids.add(UUID.fromString(value));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        snapshot.setParentVillager2Uuid(readUuid(tag, "ParentVillager2Uuid"));
        snapshot.setParentVillager2Name(tag.contains("ParentVillager2Name") ? tag.getString("ParentVillager2Name") : "");
        snapshot.setNaturalFamilyGroupId(readUuid(tag, "NaturalFamilyGroupId"));
        snapshot.setNaturalFamilyMember(tag.getBoolean("NaturalFamilyMember"));
        snapshot.setLastKnownDimension(tag.contains("LastKnownDimension") ? tag.getString("LastKnownDimension") : "");
        snapshot.setLastKnownX(tag.contains("LastKnownX") ? tag.getInt("LastKnownX") : 0);
        snapshot.setLastKnownY(tag.contains("LastKnownY") ? tag.getInt("LastKnownY") : 0);
        snapshot.setLastKnownZ(tag.contains("LastKnownZ") ? tag.getInt("LastKnownZ") : 0);
        return snapshot;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        writeUuid(tag, "Uuid", this.villagerUuid);
        if (!this.name.isEmpty()) {
            tag.putString("Name", this.name);
        }
        if (!this.gender.isEmpty()) {
            tag.putString("Gender", this.gender);
        }
        tag.putString("AgeStage", this.ageStage.name());
        tag.putBoolean("Alive", this.alive);
        tag.putBoolean("BornFromFamilySystem", this.bornFromFamilySystem);
        tag.putInt("SkinIndex", this.skinIndex);
        if (!this.skinResourcePath.isEmpty()) {
            tag.putString("SkinResourcePath", this.skinResourcePath);
        }
        if (!this.savedSkinInput.isEmpty()) {
            tag.putString("SavedSkinInput", this.savedSkinInput);
        }
        tag.putBoolean("HasCustomSkin", this.hasCustomSkin);
        writeUuid(tag, "ParentPlayerUuid", this.parentPlayerUuid);
        if (!this.parentPlayerName.isEmpty()) {
            tag.putString("ParentPlayerName", this.parentPlayerName);
        }
        writeUuid(tag, "ParentVillagerUuid", this.parentVillagerUuid);
        if (!this.parentVillagerName.isEmpty()) {
            tag.putString("ParentVillagerName", this.parentVillagerName);
        }
        writeUuid(tag, "SpousePlayerUuid", this.spousePlayerUuid);
        if (!this.spousePlayerName.isEmpty()) {
            tag.putString("SpousePlayerName", this.spousePlayerName);
        }
        writeUuid(tag, "FormerSpousePlayerUuid", this.formerSpousePlayerUuid);
        if (!this.formerSpousePlayerName.isEmpty()) {
            tag.putString("FormerSpousePlayerName", this.formerSpousePlayerName);
        }
        writeUuid(tag, "SpouseVillagerUuid", this.spouseVillagerUuid);
        if (!this.spouseVillagerName.isEmpty()) {
            tag.putString("SpouseVillagerName", this.spouseVillagerName);
        }
        ListTag children = new ListTag();
        for (UUID childVillagerUuid : this.childVillagerUuids) {
            children.add(StringTag.valueOf(childVillagerUuid.toString()));
        }
        tag.put("ChildVillagerUuids", children);
        writeUuid(tag, "ParentVillager2Uuid", this.parentVillager2Uuid);
        if (!this.parentVillager2Name.isEmpty()) {
            tag.putString("ParentVillager2Name", this.parentVillager2Name);
        }
        writeUuid(tag, "NaturalFamilyGroupId", this.naturalFamilyGroupId);
        tag.putBoolean("NaturalFamilyMember", this.naturalFamilyMember);
        if (!this.lastKnownDimension.isEmpty()) {
            tag.putString("LastKnownDimension", this.lastKnownDimension);
        }
        tag.putInt("LastKnownX", this.lastKnownX);
        tag.putInt("LastKnownY", this.lastKnownY);
        tag.putInt("LastKnownZ", this.lastKnownZ);
        return tag;
    }

    public UUID getVillagerUuid() {
        return this.villagerUuid;
    }

    public void setVillagerUuid(UUID villagerUuid) {
        this.villagerUuid = villagerUuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? "" : gender;
    }

    public VillagerAgeStage getAgeStage() {
        return this.ageStage;
    }

    public void setAgeStage(VillagerAgeStage ageStage) {
        this.ageStage = ageStage == null ? VillagerAgeStage.ADULT : ageStage;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isBornFromFamilySystem() {
        return this.bornFromFamilySystem;
    }

    public void setBornFromFamilySystem(boolean bornFromFamilySystem) {
        this.bornFromFamilySystem = bornFromFamilySystem;
    }

    public int getSkinIndex() {
        return this.skinIndex;
    }

    public void setSkinIndex(int skinIndex) {
        this.skinIndex = skinIndex;
    }

    public String getSkinResourcePath() {
        return this.skinResourcePath;
    }

    public void setSkinResourcePath(String skinResourcePath) {
        this.skinResourcePath = skinResourcePath == null ? "" : skinResourcePath;
    }

    public String getSavedSkinInput() {
        return this.savedSkinInput;
    }

    public void setSavedSkinInput(String savedSkinInput) {
        this.savedSkinInput = savedSkinInput == null ? "" : savedSkinInput;
    }

    public boolean hasCustomSkin() {
        return this.hasCustomSkin;
    }

    public void setHasCustomSkin(boolean hasCustomSkin) {
        this.hasCustomSkin = hasCustomSkin;
    }

    public UUID getParentPlayerUuid() {
        return this.parentPlayerUuid;
    }

    public void setParentPlayerUuid(UUID parentPlayerUuid) {
        this.parentPlayerUuid = parentPlayerUuid;
    }

    public String getParentPlayerName() {
        return this.parentPlayerName;
    }

    public void setParentPlayerName(String parentPlayerName) {
        this.parentPlayerName = parentPlayerName == null ? "" : parentPlayerName;
    }

    public UUID getParentVillagerUuid() {
        return this.parentVillagerUuid;
    }

    public void setParentVillagerUuid(UUID parentVillagerUuid) {
        this.parentVillagerUuid = parentVillagerUuid;
    }

    public String getParentVillagerName() {
        return this.parentVillagerName;
    }

    public void setParentVillagerName(String parentVillagerName) {
        this.parentVillagerName = parentVillagerName == null ? "" : parentVillagerName;
    }

    public UUID getSpousePlayerUuid() {
        return this.spousePlayerUuid;
    }

    public void setSpousePlayerUuid(UUID spousePlayerUuid) {
        this.spousePlayerUuid = spousePlayerUuid;
    }

    public String getSpousePlayerName() {
        return this.spousePlayerName;
    }

    public void setSpousePlayerName(String spousePlayerName) {
        this.spousePlayerName = spousePlayerName == null ? "" : spousePlayerName;
    }

    public UUID getFormerSpousePlayerUuid() {
        return this.formerSpousePlayerUuid;
    }

    public void setFormerSpousePlayerUuid(UUID formerSpousePlayerUuid) {
        this.formerSpousePlayerUuid = formerSpousePlayerUuid;
    }

    public String getFormerSpousePlayerName() {
        return this.formerSpousePlayerName;
    }

    public void setFormerSpousePlayerName(String formerSpousePlayerName) {
        this.formerSpousePlayerName = formerSpousePlayerName == null ? "" : formerSpousePlayerName;
    }

    public UUID getSpouseVillagerUuid() {
        return this.spouseVillagerUuid;
    }

    public void setSpouseVillagerUuid(UUID spouseVillagerUuid) {
        this.spouseVillagerUuid = spouseVillagerUuid;
    }

    public String getSpouseVillagerName() {
        return this.spouseVillagerName;
    }

    public void setSpouseVillagerName(String spouseVillagerName) {
        this.spouseVillagerName = spouseVillagerName == null ? "" : spouseVillagerName;
    }

    public Set<UUID> getChildVillagerUuids() {
        return this.childVillagerUuids;
    }

    public UUID getParentVillager2Uuid() {
        return this.parentVillager2Uuid;
    }

    public void setParentVillager2Uuid(UUID parentVillager2Uuid) {
        this.parentVillager2Uuid = parentVillager2Uuid;
    }

    public String getParentVillager2Name() {
        return this.parentVillager2Name;
    }

    public void setParentVillager2Name(String parentVillager2Name) {
        this.parentVillager2Name = parentVillager2Name == null ? "" : parentVillager2Name;
    }

    public UUID getNaturalFamilyGroupId() {
        return this.naturalFamilyGroupId;
    }

    public void setNaturalFamilyGroupId(UUID naturalFamilyGroupId) {
        this.naturalFamilyGroupId = naturalFamilyGroupId;
    }

    public boolean isNaturalFamilyMember() {
        return this.naturalFamilyMember;
    }

    public void setNaturalFamilyMember(boolean naturalFamilyMember) {
        this.naturalFamilyMember = naturalFamilyMember;
    }

    public String getLastKnownDimension() {
        return this.lastKnownDimension;
    }

    public void setLastKnownDimension(String lastKnownDimension) {
        this.lastKnownDimension = lastKnownDimension == null ? "" : lastKnownDimension;
    }

    public int getLastKnownX() {
        return this.lastKnownX;
    }

    public void setLastKnownX(int lastKnownX) {
        this.lastKnownX = lastKnownX;
    }

    public int getLastKnownY() {
        return this.lastKnownY;
    }

    public void setLastKnownY(int lastKnownY) {
        this.lastKnownY = lastKnownY;
    }

    public int getLastKnownZ() {
        return this.lastKnownZ;
    }

    public void setLastKnownZ(int lastKnownZ) {
        this.lastKnownZ = lastKnownZ;
    }

    private static VillagerAgeStage parseAgeStage(String value) {
        if (value == null || value.isEmpty()) {
            return VillagerAgeStage.ADULT;
        }
        try {
            return VillagerAgeStage.valueOf(value);
        } catch (IllegalArgumentException e) {
            return VillagerAgeStage.ADULT;
        }
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        if (tag.hasUUID(key)) {
            return tag.getUUID(key);
        }
        if (tag.contains(key)) {
            String value = tag.getString(key);
            if (!value.isEmpty()) {
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
        if (uuid != null) {
            tag.putUUID(key, uuid);
        }
    }
}
