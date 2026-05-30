package com.javic.slimpatch.item;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SlimPatch.MODID);

    public static final DeferredHolder<Item, GenderedVillagerEggItem> MALE_VILLAGER_SPAWN_EGG =
            ITEMS.register("male_villager_spawn_egg",
                    () -> new GenderedVillagerEggItem(
                            ModEntities.MALE_VILLAGER.get(),
                            true,
                            new Item.Properties()
                    ));

    public static final DeferredHolder<Item, GenderedVillagerEggItem> FEMALE_VILLAGER_SPAWN_EGG =
            ITEMS.register("female_villager_spawn_egg",
                    () -> new GenderedVillagerEggItem(
                            ModEntities.FEMALE_VILLAGER.get(),
                            false,
                            new Item.Properties()
                    ));

    public static final DeferredHolder<Item, Item> ROMANTIC_BOUQUET =
            ITEMS.register("romantic_bouquet", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_WEDDING_RING =
            ITEMS.register("iron_wedding_ring", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLD_WEDDING_RING =
            ITEMS.register("gold_wedding_ring", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> EMERALD_WEDDING_RING =
            ITEMS.register("emerald_wedding_ring", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_WEDDING_RING =
            ITEMS.register("diamond_wedding_ring", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIVORCE_PAPERS =
            ITEMS.register("divorce_papers", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, FamilyCharmItem> FAMILY_CHARM =
            ITEMS.register("family_charm", () -> new FamilyCharmItem(new Item.Properties()));

    public static final DeferredHolder<Item, BabyGenderCharmItem> SUN_CHARM =
            ITEMS.register("sun_charm", () -> new BabyGenderCharmItem(new Item.Properties()));

    public static final DeferredHolder<Item, BabyGenderCharmItem> MOON_CHARM =
            ITEMS.register("moon_charm", () -> new BabyGenderCharmItem(new Item.Properties()));
}
