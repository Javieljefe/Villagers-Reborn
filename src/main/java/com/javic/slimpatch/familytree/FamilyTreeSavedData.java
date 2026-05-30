package com.javic.slimpatch.familytree;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class FamilyTreeSavedData extends SavedData {

    private static final String DATA_ID = "slimpatch_family_tree";
    private static final int DATA_VERSION = 1;

    private final Map<UUID, FamilyTreeVillagerSnapshot> villagers = new LinkedHashMap<>();
    private final Map<UUID, FamilyTreePlayerSnapshot> players = new LinkedHashMap<>();

    public FamilyTreeSavedData() {
    }

    public static FamilyTreeSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(FamilyTreeSavedData::new, FamilyTreeSavedData::load),
                DATA_ID
        );
    }

    public static FamilyTreeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FamilyTreeSavedData data = new FamilyTreeSavedData();
        ListTag villagers = tag.getList("Villagers", 10);
        for (int i = 0; i < villagers.size(); i++) {
            FamilyTreeVillagerSnapshot snapshot = FamilyTreeVillagerSnapshot.load(villagers.getCompound(i));
            if (snapshot.getVillagerUuid() != null) {
                data.villagers.put(snapshot.getVillagerUuid(), snapshot);
            }
        }
        ListTag players = tag.getList("Players", 10);
        for (int i = 0; i < players.size(); i++) {
            FamilyTreePlayerSnapshot snapshot = FamilyTreePlayerSnapshot.load(players.getCompound(i));
            if (snapshot.getPlayerUuid() != null) {
                data.players.put(snapshot.getPlayerUuid(), snapshot);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Version", DATA_VERSION);
        ListTag villagers = new ListTag();
        for (FamilyTreeVillagerSnapshot snapshot : this.villagers.values()) {
            villagers.add(snapshot.save());
        }
        tag.put("Villagers", villagers);
        ListTag players = new ListTag();
        for (FamilyTreePlayerSnapshot snapshot : this.players.values()) {
            players.add(snapshot.save());
        }
        tag.put("Players", players);
        return tag;
    }

    public FamilyTreeVillagerSnapshot getVillager(UUID villagerUuid) {
        return villagerUuid == null ? null : this.villagers.get(villagerUuid);
    }

    public FamilyTreeVillagerSnapshot getOrCreateVillager(UUID villagerUuid) {
        if (villagerUuid == null) {
            return null;
        }
        FamilyTreeVillagerSnapshot snapshot = this.villagers.get(villagerUuid);
        if (snapshot == null) {
            snapshot = new FamilyTreeVillagerSnapshot(villagerUuid);
            this.villagers.put(villagerUuid, snapshot);
            this.setDirty();
        }
        return snapshot;
    }

    public void putVillager(FamilyTreeVillagerSnapshot snapshot) {
        if (snapshot != null && snapshot.getVillagerUuid() != null) {
            this.villagers.put(snapshot.getVillagerUuid(), snapshot);
            this.setDirty();
        }
    }

    public Iterable<FamilyTreeVillagerSnapshot> getVillagers() {
        return this.villagers.values();
    }

    public FamilyTreePlayerSnapshot getPlayer(UUID playerUuid) {
        return playerUuid == null ? null : this.players.get(playerUuid);
    }

    public FamilyTreePlayerSnapshot getOrCreatePlayer(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        FamilyTreePlayerSnapshot snapshot = this.players.get(playerUuid);
        if (snapshot == null) {
            snapshot = new FamilyTreePlayerSnapshot(playerUuid);
            this.players.put(playerUuid, snapshot);
            this.setDirty();
        }
        return snapshot;
    }

    public void putPlayer(FamilyTreePlayerSnapshot snapshot) {
        if (snapshot != null && snapshot.getPlayerUuid() != null) {
            this.players.put(snapshot.getPlayerUuid(), snapshot);
            this.setDirty();
        }
    }
}
