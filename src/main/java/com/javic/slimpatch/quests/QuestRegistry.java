package com.javic.slimpatch.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestRegistry {

    private static final List<Quest> REGISTERED_QUESTS = new ArrayList<>();

    public static void register(Quest quest) {
        REGISTERED_QUESTS.add(quest);
    }

    public static List<Quest> getAll() {
        return Collections.unmodifiableList(REGISTERED_QUESTS);
    }

    public static Quest getRandom(java.util.Random random) {
        if (REGISTERED_QUESTS.isEmpty()) return null;
        return REGISTERED_QUESTS.get(random.nextInt(REGISTERED_QUESTS.size()));
    }

    public static Quest getById(String id) {
        for (Quest quest : REGISTERED_QUESTS) {
            if (quest.getId().equals(id)) {
                return quest;
            }
        }
        return null;
    }
}