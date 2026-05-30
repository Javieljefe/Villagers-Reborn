package com.javic.slimpatch.client;

import com.javic.slimpatch.quests.data.PlayerQuestData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class ClientQuestDataAccess {

    private ClientQuestDataAccess() {
    }

    public static PlayerQuestData getClientInstance() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;

        if (mc.hasSingleplayerServer()) {
            MinecraftServer server = mc.getSingleplayerServer();
            if (server != null) {
                ServerLevel level = server.overworld();
                return level.getDataStorage().computeIfAbsent(
                        new SavedData.Factory<>(PlayerQuestData::new, PlayerQuestData::load),
                        mc.player.getUUID().toString() + "_quests"
                );
            }
        }

        return null;
    }
}
