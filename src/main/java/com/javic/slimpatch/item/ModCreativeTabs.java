package com.javic.slimpatch.item;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SlimPatch.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VILLAGERS_REBORN =
            CREATIVE_MODE_TABS.register("villagers_reborn",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.slimpatch.villagers_reborn"))
                            .icon(() -> new ItemStack(ModItems.ROMANTIC_BOUQUET.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.ROMANTIC_BOUQUET.get());
                                output.accept(ModItems.IRON_WEDDING_RING.get());
                                output.accept(ModItems.GOLD_WEDDING_RING.get());
                                output.accept(ModItems.EMERALD_WEDDING_RING.get());
                                output.accept(ModItems.DIAMOND_WEDDING_RING.get());
                                output.accept(ModItems.DIVORCE_PAPERS.get());
                                output.accept(ModItems.FAMILY_CHARM.get());
                                output.accept(ModItems.SUN_CHARM.get());
                                output.accept(ModItems.MOON_CHARM.get());
                                output.accept(ModItems.MALE_VILLAGER_SPAWN_EGG.get());
                                output.accept(ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
                            })
                            .build());

    private ModCreativeTabs() {
    }
}
