package com.javic.slimpatch.quests.objectives;

import com.javic.slimpatch.quests.QuestObjective;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public class KillMobsObjective extends QuestObjective {

    private final EntityType<?> targetMob;
    private final int requiredKills;
    private int currentKills;

    public KillMobsObjective(EntityType<?> targetMob, int requiredKills) {
        this.targetMob = targetMob;
        this.requiredKills = requiredKills;
        this.currentKills = 0;
    }

    public EntityType<?> getTargetMob() {
        return targetMob;
    }

    @Override
    public void onProgress(ServerPlayer player) {
        currentKills++;
        if (currentKills >= requiredKills) markCompleted();
    }

    @Override
    public boolean isCompleted(ServerPlayer player) {
        return completed;
    }
}