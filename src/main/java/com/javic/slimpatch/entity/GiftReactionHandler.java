package com.javic.slimpatch.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GiftReactionHandler {

    public static ItemStack getRandomGift(VillagerPersonality personality, RandomSource random) {
        switch (personality) {
            case FRIENDLY -> {
                return random.nextFloat() < 0.5f ? new ItemStack(Items.COOKIE, random.nextInt(2) + 1)
                        : new ItemStack(Items.CAKE);
            }
            case MEAN -> {
                return random.nextFloat() < 0.3f ? new ItemStack(Items.COAL, random.nextInt(3) + 1)
                        : new ItemStack(Items.ROTTEN_FLESH, random.nextInt(2) + 1);
            }
            case SHY -> {
                return random.nextFloat() < 0.5f ? new ItemStack(Items.POPPY)
                        : new ItemStack(Items.PINK_TULIP);
            }
            case BRAVE -> {
                return random.nextFloat() < 0.4f ? new ItemStack(Items.IRON_SWORD)
                        : new ItemStack(Items.SHIELD);
            }
            case GRUMPY -> {
                return random.nextFloat() < 0.4f ? new ItemStack(Items.BREAD, random.nextInt(3) + 1)
                        : new ItemStack(Items.IRON_INGOT, random.nextInt(2) + 1);
            }
            case GREEDY -> {
                return random.nextFloat() < 0.7f ? new ItemStack(Items.EMERALD, random.nextInt(3) + 1)
                        : new ItemStack(Items.GOLD_INGOT, random.nextInt(2) + 1);
            }
            case ROMANTIC -> {
                return random.nextFloat() < 0.5f ? new ItemStack(Items.ROSE_BUSH)
                        : new ItemStack(Items.BOOK);
            }
            case WISE -> {
                return random.nextFloat() < 0.4f ? new ItemStack(Items.ENCHANTED_BOOK)
                        : new ItemStack(Items.BOOK, random.nextInt(2) + 1);
            }
            default -> {
                return new ItemStack(Items.COOKIE);
            }
        }
    }
}