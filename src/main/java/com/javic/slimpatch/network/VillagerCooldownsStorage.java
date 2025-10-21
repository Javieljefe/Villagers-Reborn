package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerCooldownsStorage {

    private static final Map<UUID, Map<String, Integer>> DATA = new HashMap<>();
    private static final Map<Integer, UUID> ID_LINKS = new HashMap<>();

    public static void setCooldowns(UUID uuid, Map<String, Integer> cooldowns) {
        if (uuid == null) return;
        DATA.put(uuid, new HashMap<>(cooldowns));
        SlimPatch.LOGGER.debug("[SlimPatch] ✅ Cooldowns actualizados para aldeano UUID {}", uuid);
    }

    public static Map<String, Integer> getCooldowns(UUID uuid) {
        return DATA.getOrDefault(uuid, Collections.emptyMap());
    }

    public static Map<UUID, Map<String, Integer>> getAll() {
        SlimPatch.LOGGER.debug("[SlimPatch] 🕒 getAll() → {} aldeanos con cooldowns activos", DATA.size());
        return new HashMap<>(DATA);
    }

    public static void ensureLinked(int entityId, UUID uuid) {
        if (!ID_LINKS.containsKey(entityId)) {
            ID_LINKS.put(entityId, uuid);
            SlimPatch.LOGGER.debug("[SlimPatch] Vinculado entityId {} ↔ UUID {}", entityId, uuid);
        }
    }

    public static UUID getUUIDFromEntityId(int entityId) {
        return ID_LINKS.get(entityId);
    }

    public static void clear() {
        DATA.clear();
        ID_LINKS.clear();
        SlimPatch.LOGGER.debug("[SlimPatch] VillagerCooldownsStorage reseteado");
    }

    private static final Map<UUID, Long> GIFT_COOLDOWNS = new HashMap<>();
    private static final long GIFT_COOLDOWN_MS = 20 * 60 * 1000L;

    public static boolean hasGiftCooldown(UUID uuid) {
        if (uuid == null) return false;
        Long lastGift = GIFT_COOLDOWNS.get(uuid);
        if (lastGift == null) return false;
        return System.currentTimeMillis() - lastGift < GIFT_COOLDOWN_MS;
    }

    public static void setGiftCooldown(UUID uuid) {
        if (uuid == null) return;
        GIFT_COOLDOWNS.put(uuid, System.currentTimeMillis());
    }

    public static long getGiftCooldownRemaining(UUID uuid) {
        if (uuid == null || !GIFT_COOLDOWNS.containsKey(uuid)) return 0;
        long elapsed = System.currentTimeMillis() - GIFT_COOLDOWNS.get(uuid);
        return Math.max(0, GIFT_COOLDOWN_MS - elapsed);
    }

    public static void clearGiftCooldowns() {
        GIFT_COOLDOWNS.clear();
    }
}