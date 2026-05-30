package com.javic.slimpatch.quests.data;

import com.javic.slimpatch.quests.Quest;
import com.javic.slimpatch.quests.QuestRegistry;
import com.javic.slimpatch.quests.QuestStatus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerQuestData extends SavedData {

    private final Set<String> activeQuests = new HashSet<>();
    private final Set<String> completedQuests = new HashSet<>();

    public static PlayerQuestData get(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(PlayerQuestData::new, PlayerQuestData::load),
                player.getUUID().toString() + "_quests"
        );
    }

    public static PlayerQuestData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerQuestData data = new PlayerQuestData();
        ListTag active = tag.getList("ActiveQuests", 8);
        ListTag completed = tag.getList("CompletedQuests", 8);

        for (int i = 0; i < active.size(); i++) {
            data.activeQuests.add(active.getString(i));
        }
        for (int i = 0; i < completed.size(); i++) {
            data.completedQuests.add(completed.getString(i));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag active = new ListTag();
        ListTag completed = new ListTag();

        for (String id : activeQuests) {
            active.add(StringTag.valueOf(id));
        }
        for (String id : completedQuests) {
            completed.add(StringTag.valueOf(id));
        }

        tag.put("ActiveQuests", active);
        tag.put("CompletedQuests", completed);
        return tag;
    }

    public void startQuest(String questId) {
        if (!activeQuests.contains(questId) && !completedQuests.contains(questId)) {
            activeQuests.add(questId);
            setDirty();
        }
    }

    public void completeQuest(String questId) {
        if (activeQuests.remove(questId)) {
            completedQuests.add(questId);
            setDirty();
        }
    }

    public boolean isActive(String questId) {
        return activeQuests.contains(questId);
    }

    public boolean isCompleted(String questId) {
        return completedQuests.contains(questId);
    }

    public QuestStatus getStatus(String questId) {
        if (completedQuests.contains(questId)) return QuestStatus.COMPLETED;
        if (activeQuests.contains(questId)) return QuestStatus.ACTIVE;
        return QuestStatus.AVAILABLE;
    }

    public Set<String> getActiveQuests() {
        return activeQuests;
    }

    public Set<String> getCompletedQuests() {
        return completedQuests;
    }

    public void syncPlayerQuests(ServerPlayer player) {
        for (String id : activeQuests) {
            Quest quest = QuestRegistry.getAll().stream().filter(q -> q.getId().equals(id)).findFirst().orElse(null);
            if (quest != null) quest.setStatus(QuestStatus.ACTIVE);
        }
        for (String id : completedQuests) {
            Quest quest = QuestRegistry.getAll().stream().filter(q -> q.getId().equals(id)).findFirst().orElse(null);
            if (quest != null) quest.setStatus(QuestStatus.COMPLETED);
        }
    }

    public List<Quest> getActiveQuestObjects() {
        return activeQuests.stream()
                .map(id -> QuestRegistry.getAll().stream()
                        .filter(q -> q.getId().equals(id))
                        .findFirst()
                        .orElse(null))
                .filter(q -> q != null)
                .collect(Collectors.toList());
    }
}
