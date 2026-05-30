package com.javic.slimpatch.entity;

import com.javic.slimpatch.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;

public final class BabyGenderCharmHandler {

    private BabyGenderCharmHandler() {
    }

    public static InteractionResult handleUseOnVillager(ServerPlayer player, ItemStack stack, Villager target) {
        if (!(target instanceof FemaleVillagerEntity femaleVillager) || !(target instanceof FamilyVillager familyVillager)) {
            player.displayClientMessage(Component.translatable("slimpatch.message.baby_gender_charm_wrong_target"), true);
            return InteractionResult.SUCCESS;
        }
        if (familyVillager.getAgeStage() != VillagerAgeStage.ADULT || !familyVillager.isExpectingChild()) {
            player.displayClientMessage(Component.translatable("slimpatch.message.baby_gender_charm_not_pregnant"), true);
            return InteractionResult.SUCCESS;
        }

        String forcedGender;
        String messageKey;
        if (stack.is(ModItems.SUN_CHARM.get())) {
            forcedGender = "male";
            messageKey = "slimpatch.message.baby_gender_charm_male_selected";
        } else if (stack.is(ModItems.MOON_CHARM.get())) {
            forcedGender = "female";
            messageKey = "slimpatch.message.baby_gender_charm_female_selected";
        } else {
            player.displayClientMessage(Component.translatable("slimpatch.message.baby_gender_charm_wrong_target"), true);
            return InteractionResult.SUCCESS;
        }

        familyVillager.setForcedBabyGender(forcedGender);
        if (!player.getAbilities().instabuild) {
            stack.consume(1, player);
        }
        player.displayClientMessage(Component.translatable(messageKey), true);
        return InteractionResult.SUCCESS;
    }

    public static String resolveChildGender(FamilyVillager familyVillager, String fallbackGender) {
        if (familyVillager == null) {
            return BirthScreenData.normalizeGender(fallbackGender);
        }
        String forcedGender = familyVillager.getForcedBabyGender();
        if ("male".equals(forcedGender) || "female".equals(forcedGender)) {
            return forcedGender;
        }
        return BirthScreenData.normalizeGender(fallbackGender);
    }
}
