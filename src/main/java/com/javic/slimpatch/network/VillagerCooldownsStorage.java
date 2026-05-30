package com.javic.slimpatch.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerCooldownsStorage {

    private static final Map<UUID, Map<UUID, Map<String, Long>>> DATA = new HashMap<>();
    private static final Map<Integer, UUID> ID_LINKS = new HashMap<>();

    public static void setCooldowns(UUID villagerUuid, UUID playerUuid, Map<String, Integer> cooldowns) {
        if (villagerUuid == null || playerUuid == null) return;
        Map<String, Long> expiryMap = new HashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
            expiryMap.put(entry.getKey(), now + Math.max(0, entry.getValue()) * 1000L);
        }
        DATA.computeIfAbsent(villagerUuid, ignored -> new HashMap<>()).put(playerUuid, expiryMap);
    }

    public static Map<String, Integer> getCooldowns(UUID villagerUuid, UUID playerUuid) {
        if (villagerUuid == null || playerUuid == null) return Collections.emptyMap();
        Map<String, Long> expiryMap = DATA.getOrDefault(villagerUuid, Collections.emptyMap()).getOrDefault(playerUuid, Collections.emptyMap());
        Map<String, Integer> result = new HashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : expiryMap.entrySet()) {
            result.put(entry.getKey(), (int) Math.max(0L, (entry.getValue() - now) / 1000L));
        }
        return result;
    }

    public static Map<UUID, Map<UUID, Map<String, Long>>> getAll() {
        return new HashMap<>(DATA);
    }

    public static void ensureLinked(int entityId, UUID uuid) {
        if (!ID_LINKS.containsKey(entityId)) {
            ID_LINKS.put(entityId, uuid);
        }
    }

    public static UUID getUUIDFromEntityId(int entityId) {
        return ID_LINKS.get(entityId);
    }

    public static void clear() {
        DATA.clear();
        ID_LINKS.clear();
    }

    private static final Map<UUID, Map<UUID, Long>> GIFT_COOLDOWNS = new HashMap<>();
    private static final long GIFT_COOLDOWN_MS = 20 * 60 * 1000L;

    public static boolean hasGiftCooldown(UUID villagerUuid, UUID playerUuid) {
        if (villagerUuid == null || playerUuid == null) return false;
        Long lastGift = GIFT_COOLDOWNS.getOrDefault(villagerUuid, Collections.emptyMap()).get(playerUuid);
        if (lastGift == null) return false;
        return System.currentTimeMillis() < lastGift;
    }

    public static void setGiftCooldown(UUID villagerUuid, UUID playerUuid) {
        if (villagerUuid == null || playerUuid == null) return;
        GIFT_COOLDOWNS.computeIfAbsent(villagerUuid, ignored -> new HashMap<>()).put(playerUuid, System.currentTimeMillis() + GIFT_COOLDOWN_MS);
    }

    public static void setGiftCooldownRemaining(UUID villagerUuid, UUID playerUuid, long remainingMs) {
        if (villagerUuid == null || playerUuid == null) return;
        if (remainingMs <= 0L) {
            Map<UUID, Long> byPlayer = GIFT_COOLDOWNS.get(villagerUuid);
            if (byPlayer != null) {
                byPlayer.remove(playerUuid);
            }
            return;
        }
        GIFT_COOLDOWNS.computeIfAbsent(villagerUuid, ignored -> new HashMap<>()).put(playerUuid, System.currentTimeMillis() + remainingMs);
    }

    public static long getGiftCooldownRemaining(UUID villagerUuid, UUID playerUuid) {
        if (villagerUuid == null || playerUuid == null) return 0;
        Long lastGift = GIFT_COOLDOWNS.getOrDefault(villagerUuid, Collections.emptyMap()).get(playerUuid);
        if (lastGift == null) return 0;
        return Math.max(0, lastGift - System.currentTimeMillis());
    }

    public static void clearGiftCooldowns() {
        GIFT_COOLDOWNS.clear();
    }
}
