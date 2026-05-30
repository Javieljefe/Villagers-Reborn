package com.javic.slimpatch.entity;

import com.javic.slimpatch.dialogue.DialogueManager;
import net.minecraft.world.entity.npc.Villager;

public final class VillagerPassiveHealHandler {

    private static final int DAMAGE_COOLDOWN_TICKS = 100;
    private static final int HEAL_INTERVAL_TICKS = 50;
    private static final float HEAL_AMOUNT = 2.0F;

    private VillagerPassiveHealHandler() {
    }

    public static void recordDamage(Villager villager, CommandableVillager commandableVillager) {
        commandableVillager.setLastDamageTick(villager.tickCount);
    }

    public static void tickHealing(Villager villager, CommandableVillager commandableVillager) {
        if (villager.level().isClientSide || !villager.isAlive() || villager.isSleeping()) {
            return;
        }
        if (villager.getHealth() >= villager.getMaxHealth()) {
            return;
        }
        if (DialogueManager.isInDialogue(villager) || VillagerCombatHandler.isActivelyDefending(villager, commandableVillager)) {
            return;
        }

        int safeTicks = villager.tickCount - commandableVillager.getLastDamageTick();
        if (safeTicks < DAMAGE_COOLDOWN_TICKS) {
            return;
        }
        if ((safeTicks - DAMAGE_COOLDOWN_TICKS) % HEAL_INTERVAL_TICKS != 0) {
            return;
        }

        villager.heal(Math.min(HEAL_AMOUNT, villager.getMaxHealth() - villager.getHealth()));
    }
}
