package com.javic.slimpatch.dialogue;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Goal que bloquea movimiento y hace que el aldeano mire al jugador
 * mientras está en un diálogo (replica la lógica del trading vanilla).
 */
public class DialogueGoal extends Goal {

    private final Villager villager;

    public DialogueGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return DialogueManager.isInDialogue(villager);
    }

    @Override
    public boolean canContinueToUse() {
        return DialogueManager.isInDialogue(villager);
    }

    @Override
    public void start() {
        this.villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        UUID playerId = DialogueManager.getDialoguePlayer(villager);
        if (playerId != null && villager.level().getPlayerByUUID(playerId) instanceof Player player) {
            this.villager.getNavigation().stop();
            this.villager.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    @Override
    public void stop() {
        // Cuando termina el diálogo, se libera en DialogueManager.onClose()
        this.villager.getNavigation().stop();
    }
}