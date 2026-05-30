package com.javic.slimpatch.quests;

public class Quest {

    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final QuestObjective objective;
    private QuestStatus status;

    public Quest(String id, String name, String description, QuestType type, QuestObjective objective) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.objective = objective;
        this.status = QuestStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public QuestObjective getObjective() {
        return objective;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public boolean checkCompletion(net.minecraft.server.level.ServerPlayer player) {
        if (objective.isCompleted(player)) {
            status = QuestStatus.COMPLETED;
            return true;
        }
        return false;
    }
}