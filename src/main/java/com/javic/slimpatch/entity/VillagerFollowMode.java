package com.javic.slimpatch.entity;

public enum VillagerFollowMode {
    CLOSE,
    RELAXED;

    public static VillagerFollowMode fromName(String name) {
        if (name == null || name.isEmpty()) {
            return CLOSE;
        }
        try {
            return VillagerFollowMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return CLOSE;
        }
    }
}
