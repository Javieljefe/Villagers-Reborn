package com.javic.slimpatch.memory;

import net.minecraft.nbt.CompoundTag;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CuredVillagerMemory {

    private static final Map<UUID, CompoundTag> SAVED = new ConcurrentHashMap<>();
    private static CompoundTag LAST_SAVED = null;

    public static void store(UUID uuid, CompoundTag data) {
        if (uuid != null && data != null) SAVED.put(uuid, data.copy());
    }

    public static CompoundTag consume(UUID uuid) {
        return uuid != null ? SAVED.remove(uuid) : null;
    }

    public static CompoundTag peek(UUID uuid) {
        return uuid != null ? SAVED.get(uuid) : null;
    }

    public static void clear() {
        SAVED.clear();
        LAST_SAVED = null;
    }

    public static void setLast(CompoundTag data) {
        LAST_SAVED = data != null ? data.copy() : null;
    }

    public static CompoundTag getLast() {
        return LAST_SAVED != null ? LAST_SAVED.copy() : null;
    }
}