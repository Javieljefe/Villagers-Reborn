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
}