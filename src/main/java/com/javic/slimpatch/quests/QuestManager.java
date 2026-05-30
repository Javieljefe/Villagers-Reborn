package com.javic.slimpatch.quests;

import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.quests.data.PlayerQuestData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

import java.util.Random;

public class QuestManager {

    private static final Random random = new Random();

    public static boolean assignQuestToVillager(Villager villager) {
        return false;
    }

    private static Quest getAvailableRandomQuest() {
        return null;
    }

    public static void clearQuest(Villager villager) {
    }

    public static Quest getQuestForVillager(Villager villager) {
        return null;
    }

    public static void startQuest(ServerPlayer player, Quest quest) {
    }

    public static void completeQuest(ServerPlayer player, Quest quest, ServerLevel level) {
    }

    public static Villager getVillagerForQuest(Quest quest, ServerLevel level) {
        return null;
    }
}
