package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.entity.VillagerPersonality;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class VillagerPersonalityIcons {

    public static final Map<VillagerPersonality, String> NAMES = Map.ofEntries(
            Map.entry(VillagerPersonality.FRIENDLY, "slimpatch.personality.friendly"),
            Map.entry(VillagerPersonality.MEAN, "slimpatch.personality.mean"),
            Map.entry(VillagerPersonality.SHY, "slimpatch.personality.shy"),
            Map.entry(VillagerPersonality.BRAVE, "slimpatch.personality.brave"),
            Map.entry(VillagerPersonality.GRUMPY, "slimpatch.personality.grumpy"),
            Map.entry(VillagerPersonality.GREEDY, "slimpatch.personality.greedy"),
            Map.entry(VillagerPersonality.ROMANTIC, "slimpatch.personality.romantic"),
            Map.entry(VillagerPersonality.WISE, "slimpatch.personality.wise")
    );

    public static final Map<VillagerPersonality, ResourceLocation> ICONS = Map.ofEntries(
            Map.entry(VillagerPersonality.FRIENDLY, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_friendly.png")),
            Map.entry(VillagerPersonality.MEAN, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_mean.png")),
            Map.entry(VillagerPersonality.SHY, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_shy.png")),
            Map.entry(VillagerPersonality.BRAVE, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_brave.png")),
            Map.entry(VillagerPersonality.GRUMPY, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_grumpy.png")),
            Map.entry(VillagerPersonality.GREEDY, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_greedy.png")),
            Map.entry(VillagerPersonality.ROMANTIC, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_romantic.png")),
            Map.entry(VillagerPersonality.WISE, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/personalities/personality_wise.png"))
    );

    public static String getName(VillagerPersonality personality) {
        return Component.translatable(NAMES.get(personality)).getString();
    }

    public static ResourceLocation getIcon(VillagerPersonality personality) {
        return ICONS.get(personality);
    }
}
