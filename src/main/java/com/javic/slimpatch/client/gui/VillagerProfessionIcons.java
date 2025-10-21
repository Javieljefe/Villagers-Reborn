package com.javic.slimpatch.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.Map;

public class VillagerProfessionIcons {

    // Nombres visibles de las profesiones vanilla
    public static final Map<VillagerProfession, String> NAMES = Map.ofEntries(
            Map.entry(VillagerProfession.ARMORER, "Armorer"),
            Map.entry(VillagerProfession.BUTCHER, "Butcher"),
            Map.entry(VillagerProfession.CARTOGRAPHER, "Cartographer"),
            Map.entry(VillagerProfession.CLERIC, "Cleric"),
            Map.entry(VillagerProfession.FARMER, "Farmer"),
            Map.entry(VillagerProfession.FISHERMAN, "Fisherman"),
            Map.entry(VillagerProfession.FLETCHER, "Fletcher"),
            Map.entry(VillagerProfession.LEATHERWORKER, "Leatherworker"),
            Map.entry(VillagerProfession.LIBRARIAN, "Librarian"),
            Map.entry(VillagerProfession.MASON, "Mason"),
            Map.entry(VillagerProfession.SHEPHERD, "Shepherd"),
            Map.entry(VillagerProfession.TOOLSMITH, "Toolsmith"),
            Map.entry(VillagerProfession.WEAPONSMITH, "Weaponsmith"),
            Map.entry(VillagerProfession.NITWIT, "Nitwit"),
            Map.entry(VillagerProfession.NONE, "Unemployed")
    );

    // Iconos de las profesiones vanilla
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

    private static final String MODDED_NAME = "Modded Profession";
    private static final ResourceLocation MODDED_ICON =
            ResourceLocation.fromNamespaceAndPath("slimpatch", "textures/gui/professions/job_modded.png");

    // Devuelve el nombre
    public static String getName(VillagerProfession profession) {
        if (NAMES.containsKey(profession)) {
            return NAMES.get(profession);
        }
        // Profesión de otro mod
        return MODDED_NAME;
    }

    // Devuelve el icono
    public static ResourceLocation getIcon(VillagerProfession profession) {
        if (ICONS.containsKey(profession)) {
            return ICONS.get(profession);
        }
        // Profesión de otro mod
        return MODDED_ICON;
    }
}