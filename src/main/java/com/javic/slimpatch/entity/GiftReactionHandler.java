package com.javic.slimpatch.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class GiftReactionHandler {

    @SuppressWarnings("unchecked")
    private static Registry<Enchantment> getEnchantmentRegistry() {
        return (Registry<Enchantment>) BuiltInRegistries.REGISTRY.get(Registries.ENCHANTMENT.location());
    }

    public static ItemStack getRandomGift(VillagerPersonality personality, RandomSource random) {
        var enchantRegistry = getEnchantmentRegistry();

        switch (personality) {
            case FRIENDLY -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.BREAD, random.nextInt(5) + 2);
                else if (roll < 0.65f) return new ItemStack(Items.CAKE);
                else if (roll < 0.83f) return new ItemStack(Items.EMERALD, random.nextInt(5) + 1);
                else if (roll < 0.93f) return new ItemStack(Items.HONEY_BOTTLE, random.nextInt(3) + 1);
                else return new ItemStack(Items.GOLDEN_APPLE, random.nextInt(3) + 1);
            }

            case MEAN -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.ROTTEN_FLESH, random.nextInt(4) + 2);
                else if (roll < 0.65f) return new ItemStack(Items.COAL, random.nextInt(7) + 4);
                else if (roll < 0.83f) return new ItemStack(Items.TNT, random.nextInt(5) + 1);
                else if (roll < 0.93f) return new ItemStack(Items.IRON_INGOT, random.nextInt(6) + 1);
                else return new ItemStack(Items.NETHERITE_SCRAP, random.nextInt(3) + 1);
            }

            case SHY -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.PUMPKIN_PIE, random.nextInt(2) + 1);
                else if (roll < 0.65f) return new ItemStack(Items.APPLE, random.nextInt(5) + 2);
                else if (roll < 0.83f) return new ItemStack(Items.HONEY_BOTTLE, random.nextInt(3) + 1);
                else if (roll < 0.93f) return new ItemStack(Items.DIAMOND, random.nextInt(2) + 1);
                else {
                    ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(2) + 1);
                    potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.INVISIBILITY));
                    return potionStack;
                }
            }

            case BRAVE -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.ARROW, random.nextInt(11) + 5);
                else if (roll < 0.65f) return new ItemStack(Items.SHIELD);
                else if (roll < 0.83f) {
                    if (random.nextBoolean()) {
                        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
                        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.STRENGTH));
                        return potionStack;
                    } else {
                        ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
                        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.FIRE_RESISTANCE));
                        return potionStack;
                    }
                } else if (roll < 0.93f) return new ItemStack(Items.DIAMOND_SWORD);
                else return new ItemStack(Items.TOTEM_OF_UNDYING);
            }

            case GRUMPY -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.POTATO, random.nextInt(6) + 3);
                else if (roll < 0.65f) return new ItemStack(Items.IRON_INGOT, random.nextInt(3) + 1);
                else if (roll < 0.83f) return new ItemStack(Items.EMERALD, random.nextInt(3) + 1);
                else if (roll < 0.93f) return new ItemStack(Items.ENDER_PEARL, random.nextInt(4) + 1);
                else {
                    ItemStack[] armorPieces = {
                        new ItemStack(Items.DIAMOND_HELMET),
                        new ItemStack(Items.DIAMOND_CHESTPLATE),
                        new ItemStack(Items.DIAMOND_LEGGINGS),
                        new ItemStack(Items.DIAMOND_BOOTS)
                    };
                    return armorPieces[random.nextInt(armorPieces.length)];
                }
            }

            case GREEDY -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.COPPER_INGOT, random.nextInt(3) + 2);
                else if (roll < 0.65f) return new ItemStack(Items.GOLD_NUGGET, random.nextInt(7) + 3);
                else if (roll < 0.83f) return new ItemStack(Items.GOLD_INGOT, random.nextInt(3) + 1);
                else if (roll < 0.93f) return new ItemStack(Items.EMERALD, random.nextInt(6) + 1);
                else return new ItemStack(Items.DIAMOND, random.nextInt(3) + 1);
            }

            case ROMANTIC -> {
                float roll = random.nextFloat();
                if (roll < 0.40f)
                    return new ItemStack(random.nextBoolean() ? Items.PINK_TULIP : Items.POPPY, random.nextInt(3) + 1);
                else if (roll < 0.65f) return new ItemStack(Items.APPLE, random.nextInt(6) + 3);
                else if (roll < 0.83f) return new ItemStack(Items.EMERALD, random.nextInt(5) + 2);
                else if (roll < 0.93f) return new ItemStack(Items.BLAZE_POWDER, random.nextInt(4) + 1);
                else return new ItemStack(Items.HEART_OF_THE_SEA);
            }

            case WISE -> {
                float roll = random.nextFloat();
                if (roll < 0.40f) return new ItemStack(Items.BOOK, random.nextInt(8) + 1);
                else if (roll < 0.65f) return new ItemStack(Items.LAPIS_LAZULI, random.nextInt(9) + 4);
                else if (roll < 0.83f) {
                    ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
                    potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.FIRE_RESISTANCE));
                    return potionStack;
                } else if (roll < 0.93f) {
                    ItemStack potionStack = new ItemStack(Items.POTION, random.nextInt(3) + 1);
                    potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.REGENERATION));
                    return potionStack;
                } else {
                    return new ItemStack(Items.SHULKER_SHELL, 1);
                }
            }

            default -> {
                return new ItemStack(Items.COOKIE);
            }
        }
    }
}