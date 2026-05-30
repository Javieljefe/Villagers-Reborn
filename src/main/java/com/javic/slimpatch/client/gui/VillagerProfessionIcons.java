package com.javic.slimpatch.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.Map;

public class VillagerProfessionIcons {

    public static final Map<VillagerProfession, String> NAMES = Map.ofEntries(
            Map.entry(VillagerProfession.ARMORER, "entity.minecraft.villager.armorer"),
            Map.entry(VillagerProfession.BUTCHER, "entity.minecraft.villager.butcher"),
            Map.entry(VillagerProfession.CARTOGRAPHER, "entity.minecraft.villager.cartographer"),
            Map.entry(VillagerProfession.CLERIC, "entity.minecraft.villager.cleric"),
            Map.entry(VillagerProfession.FARMER, "entity.minecraft.villager.farmer"),
            Map.entry(VillagerProfession.FISHERMAN, "entity.minecraft.villager.fisherman"),
            Map.entry(VillagerProfession.FLETCHER, "entity.minecraft.villager.fletcher"),
            Map.entry(VillagerProfession.LEATHERWORKER, "entity.minecraft.villager.leatherworker"),
            Map.entry(VillagerProfession.LIBRARIAN, "entity.minecraft.villager.librarian"),
            Map.entry(VillagerProfession.MASON, "entity.minecraft.villager.mason"),
            Map.entry(VillagerProfession.SHEPHERD, "entity.minecraft.villager.shepherd"),
            Map.entry(VillagerProfession.TOOLSMITH, "entity.minecraft.villager.toolsmith"),
            Map.entry(VillagerProfession.WEAPONSMITH, "entity.minecraft.villager.weaponsmith"),
            Map.entry(VillagerProfession.NITWIT, "entity.minecraft.villager.nitwit"),
            Map.entry(VillagerProfession.NONE, "entity.minecraft.villager.none")
    );

    public static final Map<VillagerProfession, ResourceLocation> ICONS = Map.ofEntries(
            Map.entry(VillagerProfession.ARMORER,       ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_armorer.png")),
            Map.entry(VillagerProfession.BUTCHER,      ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_butcher.png")),
            Map.entry(VillagerProfession.CARTOGRAPHER, ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_cartographer.png")),
            Map.entry(VillagerProfession.CLERIC,       ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_cleric.png")),
            Map.entry(VillagerProfession.FARMER,       ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_farmer.png")),
            Map.entry(VillagerProfession.FISHERMAN,    ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_fisherman.png")),
            Map.entry(VillagerProfession.FLETCHER,     ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_fletcher.png")),
            Map.entry(VillagerProfession.LEATHERWORKER,ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_leatherworker.png")),
            Map.entry(VillagerProfession.LIBRARIAN,    ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_librarian.png")),
            Map.entry(VillagerProfession.MASON,        ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_mason.png")),
            Map.entry(VillagerProfession.SHEPHERD,     ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_shepherd.png")),
            Map.entry(VillagerProfession.TOOLSMITH,    ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_toolsmith.png")),
            Map.entry(VillagerProfession.WEAPONSMITH,  ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_weaponsmith.png")),
            Map.entry(VillagerProfession.NITWIT,       ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_nitwit.png")),
            Map.entry(VillagerProfession.NONE,         ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_unemployed.png"))
    );

    private static final String MODDED_NAME = "slimpatch.profession.modded";
    private static final ResourceLocation MODDED_ICON =
            ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_modded.png");

    public static String getName(VillagerProfession profession) {
        if (NAMES.containsKey(profession)) {
            return Component.translatable(NAMES.get(profession)).getString();
        }
        return Component.translatable(MODDED_NAME).getString();
    }

    public static ResourceLocation getIcon(VillagerProfession profession) {
        if (ICONS.containsKey(profession)) {
            return ICONS.get(profession);
        }
        return MODDED_ICON;
    }
}
