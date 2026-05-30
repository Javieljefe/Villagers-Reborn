package com.javic.slimpatch;

import com.javic.slimpatch.menu.VillagerEquipmentMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SlimPatch.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VillagerEquipmentMenu>> VILLAGER_EQUIPMENT =
            MENUS.register("villager_equipment", () -> IMenuTypeExtension.create((containerId, playerInventory, data) -> new VillagerEquipmentMenu(containerId, playerInventory)));
}
