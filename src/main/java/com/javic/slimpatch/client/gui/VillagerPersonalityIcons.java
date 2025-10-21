package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.entity.VillagerPersonality;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class VillagerPersonalityIcons {

    public static final Map<VillagerPersonality, String> NAMES = Map.ofEntries(
            Map.entry(VillagerPersonality.FRIENDLY, "Friendly"),
            Map.entry(VillagerPersonality.MEAN, "Mean"),
            Map.entry(VillagerPersonality.SHY, "Shy"),
            Map.entry(VillagerPersonality.BRAVE, "Brave"),
            Map.entry(VillagerPersonality.GRUMPY, "Grumpy"),
            Map.entry(VillagerPersonality.GREEDY, "Greedy"),
            Map.entry(VillagerPersonality.ROMANTIC, "Romantic"),
            Map.entry(VillagerPersonality.WISE, "Wise")
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
        return NAMES.get(personality);
    }

    public static ResourceLocation getIcon(VillagerPersonality personality) {
        return ICONS.get(personality);
    }
}