package com.javic.slimpatch.quests;

import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.quests.data.PlayerQuestData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

public class QuestDialogueHelper {

    public static String getDialogueForVillager(Villager villager, ServerPlayer player) {
        if (villager == null || player == null) return "...";

        Quest quest = QuestManager.getQuestForVillager(villager);
        if (quest == null) return DialogueManager.getRandomLine("Default", villager);

        PlayerQuestData data = PlayerQuestData.get(player);
        QuestStatus status = data.getStatus(quest.getId());

        switch (status) {
            case AVAILABLE -> {
                if (quest.getStatus() == QuestStatus.AVAILABLE)
                    return DialogueManager.getRandomLine("QuestOffer", villager);
                else
                    return DialogueManager.getRandomLine("Default", villager);
            }
            case ACTIVE -> {
                return DialogueManager.getRandomLine("QuestProgress", villager);
            }
            case COMPLETED -> {
                return DialogueManager.getRandomLine("QuestComplete", villager);
            }
            default -> {
                return DialogueManager.getRandomLine("Default", villager);
            }
        }
    }

    public static void onQuestAccepted(Villager villager, ServerPlayer player, Quest quest) {
        if (villager == null || player == null || quest == null) return;
        QuestManager.startQuest(player, quest);
    }

    public static void onQuestCompleted(Villager villager, ServerPlayer player, Quest quest) {
        if (villager == null || player == null || quest == null) return;
        QuestManager.completeQuest(player, quest, player.serverLevel());
    }
}