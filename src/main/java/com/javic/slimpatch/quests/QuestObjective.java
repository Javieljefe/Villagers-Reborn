package com.javic.slimpatch.quests;

import net.minecraft.server.level.ServerPlayer;

public abstract class QuestObjective {

    protected boolean completed;

    public abstract boolean isCompleted(ServerPlayer player);

    public abstract void onProgress(ServerPlayer player);

    public boolean isFinished() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
    }
}