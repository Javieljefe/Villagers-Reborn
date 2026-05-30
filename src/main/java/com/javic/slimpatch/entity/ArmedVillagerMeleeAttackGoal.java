package com.javic.slimpatch.entity;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.npc.Villager;

public class ArmedVillagerMeleeAttackGoal extends MeleeAttackGoal {

    private final Villager villager;
    private final CommandableVillager commandableVillager;

    public ArmedVillagerMeleeAttackGoal(Villager villager, CommandableVillager commandableVillager, double speedModifier) {
        super(villager, speedModifier, false);
        this.villager = villager;
        this.commandableVillager = commandableVillager;
    }

    @Override
    public boolean canUse() {
        return VillagerCombatHandler.canMeleeAttack(this.villager, this.commandableVillager) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return VillagerCombatHandler.canMeleeAttack(this.villager, this.commandableVillager) && super.canContinueToUse();
    }

    @Override
    public void stop() {
        super.stop();
        this.villager.setAggressive(false);
    }
}
