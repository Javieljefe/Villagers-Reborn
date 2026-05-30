package com.javic.slimpatch.quests.objectives;

import com.javic.slimpatch.quests.QuestObjective;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public class TameAnimalObjective extends QuestObjective {

    private final EntityType<?> targetAnimal;
    private boolean tamed;

    public TameAnimalObjective(EntityType<?> targetAnimal) {
        this.targetAnimal = targetAnimal;
        this.tamed = false;
    }

    public EntityType<?> getTargetAnimal() {
        return targetAnimal;
    }

    public void markTamed(ServerPlayer player) {
        if (!tamed) {
            tamed = true;
            markCompleted();
        }
    }

    @Override
    public void onProgress(ServerPlayer player) {
    }

    @Override
    public boolean isCompleted(ServerPlayer player) {
        return completed || tamed;
    }
}