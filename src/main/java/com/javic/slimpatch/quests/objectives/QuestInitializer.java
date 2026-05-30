package com.javic.slimpatch.quests.objectives;

import com.javic.slimpatch.quests.Quest;
import com.javic.slimpatch.quests.QuestRegistry;
import com.javic.slimpatch.quests.QuestStatus;
import com.javic.slimpatch.quests.QuestType;
import net.minecraft.world.entity.EntityType;

public class QuestInitializer {

    public static void registerAll() {
        QuestRegistry.register(new Quest(
                "quest_kill_zombies",
                "Zombie Hunter",
                "Defeat 10 zombies to protect the village.",
                QuestType.KILL,
                new KillMobsObjective(EntityType.ZOMBIE, 10)
        ));

        QuestRegistry.register(new Quest(
                "quest_kill_skeletons",
                "Bone Breaker",
                "Defeat 10 skeletons haunting the fields.",
                QuestType.KILL,
                new KillMobsObjective(EntityType.SKELETON, 10)
        ));

        QuestRegistry.register(new Quest(
                "quest_kill_ender_dragon",
                "Dragon Slayer",
                "Defeat the mighty Ender Dragon and restore peace.",
                QuestType.KILL,
                new KillMobsObjective(EntityType.ENDER_DRAGON, 1)
        ));

        QuestRegistry.register(new Quest(
                "quest_travel_nether",
                "Through the Portal",
                "Travel to the Nether dimension and return safely.",
                QuestType.EXPLORE,
                new VisitDimensionObjective("minecraft:the_nether")
        ));

        QuestRegistry.register(new Quest(
                "quest_travel_end",
                "End Explorer",
                "Travel to the End and return alive.",
                QuestType.EXPLORE,
                new VisitDimensionObjective("minecraft:the_end")
        ));

        QuestRegistry.register(new Quest(
                "quest_relationship_friend",
                "Village Bonds",
                "Increase your relationship with a villager to 5 hearts.",
                QuestType.RELATIONSHIP,
                new RelationshipObjective(5)
        ));

        QuestRegistry.register(new Quest(
                "quest_tame_wolf",
                "Loyal Companion",
                "Tame a wolf to be your faithful friend.",
                QuestType.TAME,
                new TameAnimalObjective(EntityType.WOLF)
        ));
    }
}