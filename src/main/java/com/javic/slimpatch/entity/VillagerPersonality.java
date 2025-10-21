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

        if (roll < 0.30f) return FRIENDLY;
        else if (roll < 0.40f) return MEAN;
        else if (roll < 0.50f) return SHY;
        else if (roll < 0.60f) return BRAVE;
        else if (roll < 0.69f) return GRUMPY;
        else if (roll < 0.79f) return GREEDY;
        else if (roll < 0.89f) return ROMANTIC;
        else return WISE;
    }
}