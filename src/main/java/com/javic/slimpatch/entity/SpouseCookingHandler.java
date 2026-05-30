package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SpouseCookingHandler {

    private static final ItemStack[] FOOD_POOL = new ItemStack[]{
            new ItemStack(Items.PUMPKIN_PIE),
            new ItemStack(Items.MUSHROOM_STEW),
            new ItemStack(Items.RABBIT_STEW),
            new ItemStack(Items.BEETROOT_SOUP),
            new ItemStack(Items.CAKE)
    };

    private SpouseCookingHandler() {
    }

    public static String tryServeMeal(Villager villager, FamilyVillager familyVillager, net.minecraft.server.level.ServerPlayer player) {
        if (!Config.SPOUSE_COOKING_ENABLED.get()) {
            return null;
        }
        if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.MARRIED) {
            return null;
        }
        if (!player.getUUID().equals(familyVillager.getSpousePlayerUuid())) {
            return null;
        }
        if (VillagerCooldownData.hasSpouseMealCooldown(villager, player.getUUID())) {
            return null;
        }

        ItemStack food = FOOD_POOL[villager.getRandom().nextInt(FOOD_POOL.length)].copy();
        boolean added = player.addItem(food);
        if (!added) {
            player.drop(food, false);
        }
        villager.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        VillagerCooldownData.setSpouseMealCooldown(villager, player.getUUID(), Config.SPOUSE_COOKING_COOLDOWN_TICKS.get());

        return getCookingLineKey(villager);
    }

    public static void markMarriageCooldownUsed(Villager villager, Player player) {
        if (villager == null || player == null) {
            return;
        }
        VillagerCooldownData.setSpouseMealCooldown(villager, player.getUUID(), Config.SPOUSE_COOKING_COOLDOWN_TICKS.get());
    }

    private static String getCookingLineKey(Villager villager) {
        VillagerPersonality personality = villager instanceof MaleVillagerEntity male
                ? male.getPersonality()
                : villager instanceof FemaleVillagerEntity female
                ? female.getPersonality()
                : VillagerPersonality.FRIENDLY;
        return switch (personality) {
            case FRIENDLY -> "slimpatch.spouse_cooking.friendly";
            case MEAN -> "slimpatch.spouse_cooking.mean";
            case SHY -> "slimpatch.spouse_cooking.shy";
            case BRAVE -> "slimpatch.spouse_cooking.brave";
            case GRUMPY -> "slimpatch.spouse_cooking.grumpy";
            case GREEDY -> "slimpatch.spouse_cooking.greedy";
            case ROMANTIC -> "slimpatch.spouse_cooking.romantic";
            case WISE -> "slimpatch.spouse_cooking.wise";
        };
    }
}
