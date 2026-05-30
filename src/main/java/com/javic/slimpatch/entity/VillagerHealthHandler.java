package com.javic.slimpatch.entity;

import com.javic.slimpatch.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class VillagerHealthHandler {

    public static final String BONUS_HEALTH_TAG = "SlimPatchBonusHealth";

    private VillagerHealthHandler() {
    }

    public static double getConfiguredHealthPerGoldenCarrot() {
        return Config.HEALTH_PER_GOLDEN_CARROT.get();
    }

    public static double getConfiguredMaxBonusHealth() {
        return Config.MAX_BONUS_HEALTH.get();
    }

    public static void reapplyBonusHealth(Villager villager, CommandableVillager commandableVillager) {
        AttributeInstance maxHealth = villager.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        double bonusHealth = clampBonusHealth(commandableVillager.getBonusHealth());
        double baseHealth = resolveBaseMaxHealth(commandableVillager, maxHealth);
        commandableVillager.setBonusHealth((float) bonusHealth);
        maxHealth.setBaseValue(baseHealth + bonusHealth);
        if (villager.getHealth() > villager.getMaxHealth()) {
            villager.setHealth(villager.getMaxHealth());
        }
    }

    public static InteractionResult tryHandleGoldenCarrotInteraction(Villager villager, CommandableVillager commandableVillager, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.GOLDEN_CARROT)) {
            return InteractionResult.PASS;
        }

        if (villager.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        double currentBonus = commandableVillager.getBonusHealth();
        double maxBonus = getConfiguredMaxBonusHealth();
        if (currentBonus >= maxBonus) {
            serverPlayer.displayClientMessage(Component.translatable("slimpatch.message.villager_max_health_already"), true);
            return InteractionResult.CONSUME;
        }

        AttributeInstance maxHealth = villager.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return InteractionResult.CONSUME;
        }

        double increase = Math.min(getConfiguredHealthPerGoldenCarrot(), maxBonus - currentBonus);
        double baseHealth = resolveBaseMaxHealth(commandableVillager, maxHealth);
        double updatedBonus = currentBonus + increase;
        commandableVillager.setBonusHealth((float) updatedBonus);
        maxHealth.setBaseValue(baseHealth + updatedBonus);
        villager.setHealth(Math.min(villager.getMaxHealth(), villager.getHealth() + (float) increase));

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }

        villager.level().playSound(null, villager.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.8F, 1.1F);
        serverPlayer.displayClientMessage(Component.translatable("slimpatch.message.villager_max_health_increased"), true);
        return InteractionResult.CONSUME;
    }

    private static double clampBonusHealth(float bonusHealth) {
        return Math.max(0.0D, Math.min(getConfiguredMaxBonusHealth(), bonusHealth));
    }

    private static double resolveBaseMaxHealth(CommandableVillager commandableVillager, AttributeInstance maxHealth) {
        double baseHealth = commandableVillager.getBaseMaxHealth();
        if (baseHealth <= 0.0D) {
            baseHealth = Math.max(1.0D, maxHealth.getBaseValue());
            commandableVillager.setBaseMaxHealth(baseHealth);
        }
        return baseHealth;
    }
}
