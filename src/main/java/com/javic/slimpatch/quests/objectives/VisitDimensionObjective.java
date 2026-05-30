package com.javic.slimpatch.quests.objectives;

import com.javic.slimpatch.quests.QuestObjective;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class VisitDimensionObjective extends QuestObjective {

    private final String targetDimension;
    private boolean visited;

    public VisitDimensionObjective(String targetDimension) {
        this.targetDimension = targetDimension;
        this.visited = false;
    }

    public ResourceKey<Level> getTargetDimension() {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(targetDimension));
    }

    @Override
    public void onProgress(ServerPlayer player) {
        visited = true;
        markCompleted();
    }

    @Override
    public boolean isCompleted(ServerPlayer player) {
        return completed;
    }
}