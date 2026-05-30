package com.javic.slimpatch.quests.objectives;

import com.javic.slimpatch.quests.QuestObjective;
import net.minecraft.server.level.ServerPlayer;

public class RelationshipObjective extends QuestObjective {

    private final int requiredLevel;
    private int currentLevel;

    public RelationshipObjective(int requiredLevel) {
        this.requiredLevel = requiredLevel;
        this.currentLevel = 0;
    }

    public void onProgress(int newLevel) {
        currentLevel = Math.max(currentLevel, newLevel);
        if (currentLevel >= requiredLevel) markCompleted();
    }

    @Override
    public void onProgress(ServerPlayer player) {
    }

    @Override
    public boolean isCompleted(ServerPlayer player) {
        return completed;
    }
}