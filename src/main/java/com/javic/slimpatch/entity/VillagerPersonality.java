package com.javic.slimpatch.entity;

import net.minecraft.util.RandomSource;

public enum VillagerPersonality {
    FRIENDLY,
    MEAN,
    SHY,
    BRAVE,
    GRUMPY,
    GREEDY,
    ROMANTIC,
    WISE;

    public static VillagerPersonality getRandom(RandomSource random) {
        float roll = random.nextFloat();

        if (roll < 0.30f) return FRIENDLY;         // 30 %
        else if (roll < 0.40f) return MEAN;        // +10 %
        else if (roll < 0.50f) return SHY;         // +10 %
        else if (roll < 0.60f) return BRAVE;       // +10 %
        else if (roll < 0.69f) return GRUMPY;      // +9 %
        else if (roll < 0.79f) return GREEDY;      // +10 %
        else if (roll < 0.89f) return ROMANTIC;    // +10 %
        else return WISE;                          // +10 %
    }
}