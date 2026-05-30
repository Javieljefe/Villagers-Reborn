package com.javic.slimpatch.entity;

import com.javic.slimpatch.config.GiftPoolConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class GiftReactionHandler {

    public static ItemStack getRandomGift(VillagerPersonality personality, RandomSource random) {
        GiftPoolConfig.GiftSlot slot = rollSlot(random);

        switch (personality) {
            case FRIENDLY -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(5) + 2);
                    case UNCOMMON -> stack(personality, slot, 1);
                    case RARE -> stack(personality, slot, random.nextInt(5) + 1);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(3) + 1);
                    case LEGENDARY -> stack(personality, slot, random.nextInt(3) + 1);
                };
            }
            case MEAN -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(4) + 2);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(7) + 4);
                    case RARE -> stack(personality, slot, random.nextInt(5) + 1);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(6) + 1);
                    case LEGENDARY -> stack(personality, slot, random.nextInt(4) + 1);
                };
            }
            case SHY -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(2) + 1);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(5) + 2);
                    case RARE -> stack(personality, slot, random.nextInt(3) + 1);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(2) + 1);
                    case LEGENDARY -> shyLegendaryGift(random);
                };
            }
            case BRAVE -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(11) + 5);
                    case UNCOMMON -> stack(personality, slot, 1);
                    case RARE -> braveRareGift(random);
                    case VERY_RARE -> stack(personality, slot, 1);
                    case LEGENDARY -> stack(personality, slot, 1);
                };
            }
            case GRUMPY -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(6) + 3);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(3) + 1);
                    case RARE -> stack(personality, slot, random.nextInt(3) + 1);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(4) + 1);
                    case LEGENDARY -> grumpyLegendaryGift(random);
                };
            }
            case GREEDY -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(3) + 2);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(7) + 3);
                    case RARE -> stack(personality, slot, random.nextInt(3) + 1);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(6) + 1);
                    case LEGENDARY -> stack(personality, slot, random.nextInt(3) + 1);
                };
            }
            case ROMANTIC -> {
                return switch (slot) {
                    case COMMON -> romanticCommonGift(random);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(6) + 3);
                    case RARE -> stack(personality, slot, random.nextInt(5) + 2);
                    case VERY_RARE -> stack(personality, slot, random.nextInt(4) + 1);
                    case LEGENDARY -> stack(personality, slot, 1);
                };
            }
            case WISE -> {
                return switch (slot) {
                    case COMMON -> stack(personality, slot, random.nextInt(8) + 1);
                    case UNCOMMON -> stack(personality, slot, random.nextInt(9) + 4);
                    case RARE -> wiseRareGift(random);
                    case VERY_RARE -> wiseVeryRareGift(random);
                    case LEGENDARY -> stack(personality, slot, 1);
                };
            }
            default -> {
                return new ItemStack(Items.COOKIE);
            }
        }
    }

    private static GiftPoolConfig.GiftSlot rollSlot(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.42f) return GiftPoolConfig.GiftSlot.COMMON;
        if (roll < 0.69f) return GiftPoolConfig.GiftSlot.UNCOMMON;
        if (roll < 0.86f) return GiftPoolConfig.GiftSlot.RARE;
        if (roll < 0.95f) return GiftPoolConfig.GiftSlot.VERY_RARE;
        return GiftPoolConfig.GiftSlot.LEGENDARY;
    }

    private static ItemStack stack(VillagerPersonality personality, GiftPoolConfig.GiftSlot slot, int count) {
        return new ItemStack(GiftPoolConfig.getItem(personality, slot), count);
    }

    private static ItemStack shyLegendaryGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.SHY, GiftPoolConfig.GiftSlot.LEGENDARY);
        if (item != Items.POTION) {
            return new ItemStack(item, random.nextInt(2) + 1);
        }
        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(2) + 1);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.INVISIBILITY));
        return potionStack;
    }

    private static ItemStack braveRareGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.BRAVE, GiftPoolConfig.GiftSlot.RARE);
        if (item != Items.POTION) {
            return new ItemStack(item, random.nextInt(3) + 1);
        }
        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(random.nextBoolean() ? Potions.STRENGTH : Potions.FIRE_RESISTANCE));
        return potionStack;
    }

    private static ItemStack grumpyLegendaryGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.GRUMPY, GiftPoolConfig.GiftSlot.LEGENDARY);
        if (item != Items.DIAMOND_CHESTPLATE) {
            return new ItemStack(item);
        }
        ItemStack[] armorPieces = {
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS)
        };
        return armorPieces[random.nextInt(armorPieces.length)];
    }

    private static ItemStack romanticCommonGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.ROMANTIC, GiftPoolConfig.GiftSlot.COMMON);
        if (item != Items.POPPY) {
            return new ItemStack(item, random.nextInt(3) + 1);
        }
        return new ItemStack(random.nextBoolean() ? Items.PINK_TULIP : Items.POPPY, random.nextInt(3) + 1);
    }

    private static ItemStack wiseRareGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.WISE, GiftPoolConfig.GiftSlot.RARE);
        if (item != Items.POTION) {
            return new ItemStack(item, random.nextInt(3) + 1);
        }
        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.FIRE_RESISTANCE));
        return potionStack;
    }

    private static ItemStack wiseVeryRareGift(RandomSource random) {
        Item item = GiftPoolConfig.getItem(VillagerPersonality.WISE, GiftPoolConfig.GiftSlot.VERY_RARE);
        if (item != Items.POTION) {
            return new ItemStack(item, random.nextInt(3) + 1);
        }
        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.REGENERATION));
        return potionStack;
    }
}
