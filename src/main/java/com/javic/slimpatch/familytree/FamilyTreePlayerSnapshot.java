package com.javic.slimpatch.familytree;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class FamilyTreePlayerSnapshot {

    private UUID playerUuid;
    private String name = "";

    public FamilyTreePlayerSnapshot() {
    }

    public FamilyTreePlayerSnapshot(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public static FamilyTreePlayerSnapshot load(CompoundTag tag) {
        FamilyTreePlayerSnapshot snapshot = new FamilyTreePlayerSnapshot(readUuid(tag, "Uuid"));
        snapshot.setName(tag.contains("Name") ? tag.getString("Name") : "");
        return snapshot;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        writeUuid(tag, "Uuid", this.playerUuid);
        if (!this.name.isEmpty()) {
            tag.putString("Name", this.name);
        }
        return tag;
    }

    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
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
