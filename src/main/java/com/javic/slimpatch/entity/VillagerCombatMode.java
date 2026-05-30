package com.javic.slimpatch.entity;

public enum VillagerCombatMode {
    AGGRESSIVE,
    DEFENSIVE,
    PASSIVE;

    public static VillagerCombatMode fromName(String name) {
        if (name == null || name.isEmpty()) {
            return DEFENSIVE;
        }
        try {
            return VillagerCombatMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return DEFENSIVE;
        }
    }
}
