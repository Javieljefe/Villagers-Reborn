package com.javic.slimpatch.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VillagerCooldownData {

    private static final String DIALOGUE_COOLDOWNS_TAG = "SlimPatchDialogueCooldowns";
    private static final String GIFT_COOLDOWNS_TAG = "SlimPatchGiftCooldowns";
    private static final String ROMANTIC_GIFT_COOLDOWNS_TAG = "SlimPatchRomanticGiftCooldowns";
    private static final String SPOUSE_MEAL_COOLDOWNS_TAG = "SlimPatchSpouseMealCooldowns";
    private static final long GIFT_COOLDOWN_MS = 20 * 60 * 1000L;
    private static final long ROMANTIC_GIFT_COOLDOWN_TICKS = 12000L;

    private VillagerCooldownData() {
    }

    public static Map<String, Integer> getDialogueCooldownsForClient(Villager villager, UUID playerUuid, Map<String, Long> optionCooldowns) {
        Map<String, Integer> result = new HashMap<>();
        if (villager == null || playerUuid == null) {
            return result;
        }

        CompoundTag playerCooldowns = villager.getPersistentData()
                .getCompound(DIALOGUE_COOLDOWNS_TAG)
                .getCompound(playerUuid.toString());
        long now = System.currentTimeMillis();

        for (String option : optionCooldowns.keySet()) {
            long cooldownUntil = playerCooldowns.getLong(option);
            long remaining = Math.max(0L, cooldownUntil - now);
            result.put(option, (int) (remaining / 1000L));
        }

        return result;
    }

    public static boolean isDialogueOptionOnCooldown(Villager villager, UUID playerUuid, String option) {
        if (villager == null || playerUuid == null || option == null) {
            return false;
        }
        return getDialogueCooldownRemainingMs(villager, playerUuid, option) > 0L;
    }

    public static void setDialogueCooldown(Villager villager, UUID playerUuid, String option, long cooldownMs) {
        if (villager == null || playerUuid == null || option == null) {
            return;
        }

        CompoundTag allCooldowns = villager.getPersistentData().getCompound(DIALOGUE_COOLDOWNS_TAG);
        CompoundTag playerCooldowns = allCooldowns.getCompound(playerUuid.toString());
        playerCooldowns.putLong(option, System.currentTimeMillis() + cooldownMs);
        allCooldowns.put(playerUuid.toString(), playerCooldowns);
        villager.getPersistentData().put(DIALOGUE_COOLDOWNS_TAG, allCooldowns);
    }

    public static long getDialogueCooldownRemainingMs(Villager villager, UUID playerUuid, String option) {
        if (villager == null || playerUuid == null || option == null) {
            return 0L;
        }

        CompoundTag playerCooldowns = villager.getPersistentData()
                .getCompound(DIALOGUE_COOLDOWNS_TAG)
                .getCompound(playerUuid.toString());
        long cooldownUntil = playerCooldowns.getLong(option);
        return Math.max(0L, cooldownUntil - System.currentTimeMillis());
    }

    public static boolean hasGiftCooldown(Villager villager, UUID playerUuid) {
        return getGiftCooldownRemainingMs(villager, playerUuid) > 0L;
    }

    public static void setGiftCooldown(Villager villager, UUID playerUuid) {
        if (villager == null || playerUuid == null) {
            return;
        }

        CompoundTag allCooldowns = villager.getPersistentData().getCompound(GIFT_COOLDOWNS_TAG);
        allCooldowns.putLong(playerUuid.toString(), System.currentTimeMillis() + GIFT_COOLDOWN_MS);
        villager.getPersistentData().put(GIFT_COOLDOWNS_TAG, allCooldowns);
    }

    public static long getGiftCooldownRemainingMs(Villager villager, UUID playerUuid) {
        if (villager == null || playerUuid == null) {
            return 0L;
        }

        long cooldownUntil = villager.getPersistentData().getCompound(GIFT_COOLDOWNS_TAG).getLong(playerUuid.toString());
        return Math.max(0L, cooldownUntil - System.currentTimeMillis());
    }

    public static boolean hasRomanticGiftCooldown(Villager villager, UUID playerUuid) {
        return getRomanticGiftCooldownRemainingTicks(villager, playerUuid) > 0L;
    }

    public static void setRomanticGiftCooldown(Villager villager, UUID playerUuid) {
        if (villager == null || playerUuid == null) {
            return;
        }

        CompoundTag allCooldowns = villager.getPersistentData().getCompound(ROMANTIC_GIFT_COOLDOWNS_TAG);
        allCooldowns.putLong(playerUuid.toString(), villager.level().getGameTime() + ROMANTIC_GIFT_COOLDOWN_TICKS);
        villager.getPersistentData().put(ROMANTIC_GIFT_COOLDOWNS_TAG, allCooldowns);
    }

    public static long getRomanticGiftCooldownRemainingTicks(Villager villager, UUID playerUuid) {
        if (villager == null || playerUuid == null) {
            return 0L;
        }

        long cooldownUntil = villager.getPersistentData().getCompound(ROMANTIC_GIFT_COOLDOWNS_TAG).getLong(playerUuid.toString());
        return Math.max(0L, cooldownUntil - villager.level().getGameTime());
    }

    public static boolean hasSpouseMealCooldown(Villager villager, UUID playerUuid) {
        return getSpouseMealCooldownRemainingTicks(villager, playerUuid) > 0L;
    }

    public static void setSpouseMealCooldown(Villager villager, UUID playerUuid, long cooldownTicks) {
        if (villager == null || playerUuid == null) {
            return;
        }

        CompoundTag allCooldowns = villager.getPersistentData().getCompound(SPOUSE_MEAL_COOLDOWNS_TAG);
        allCooldowns.putLong(playerUuid.toString(), villager.level().getGameTime() + cooldownTicks);
        villager.getPersistentData().put(SPOUSE_MEAL_COOLDOWNS_TAG, allCooldowns);
    }

    public static long getSpouseMealCooldownRemainingTicks(Villager villager, UUID playerUuid) {
        if (villager == null || playerUuid == null) {
            return 0L;
        }

        long cooldownUntil = villager.getPersistentData().getCompound(SPOUSE_MEAL_COOLDOWNS_TAG).getLong(playerUuid.toString());
        return Math.max(0L, cooldownUntil - villager.level().getGameTime());
    }

    public static void syncDialogueCooldownsToPlayer(Villager villager, ServerPlayer player, Map<String, Long> optionCooldowns) {
        if (villager == null || player == null) {
            return;
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player,
                new com.javic.slimpatch.network.VillagerCooldownsPacket(
                        villager.getUUID(),
                        player.getUUID(),
                        villager.getId(),
                        getDialogueCooldownsForClient(villager, player.getUUID(), optionCooldowns),
                        getGiftCooldownRemainingMs(villager, player.getUUID())
                )
        );
    }
}
