package com.javic.slimpatch.entity;

import com.javic.slimpatch.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DivorcePapersHandler {

    private DivorcePapersHandler() {
    }

    public static boolean isDivorcePapers(ItemStack stack) {
        return stack.is(ModItems.DIVORCE_PAPERS.get());
    }

    public static ItemStack findDivorcePapersStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isDivorcePapers(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (isDivorcePapers(offHand)) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
