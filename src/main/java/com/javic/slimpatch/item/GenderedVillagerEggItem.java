package com.javic.slimpatch.item;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.component.DataComponents;

import java.util.Random;

public class GenderedVillagerEggItem extends SpawnEggItem {

    private final boolean isMale;
    private static final int MALE_SKINS = 35;
    private static final int FEMALE_SKINS = 35;

    public GenderedVillagerEggItem(EntityType<? extends Mob> type,
                                   boolean isMale,
                                   Item.Properties props) {
        super(type, 0, 0, props);
        this.isMale = isMale;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();

        CompoundTag entityTag = new CompoundTag();
        CompoundTag slimpatchData = new CompoundTag();

        int skinIndex = isMale
                ? new Random().nextInt(MALE_SKINS) + 1
                : new Random().nextInt(FEMALE_SKINS) + 1;

        slimpatchData.putInt("slimpatch_skin", skinIndex);
        slimpatchData.putString("slimpatch_gender", isMale ? "male" : "female");
        slimpatchData.putBoolean("slimpatch_forced", true);

        entityTag.put("ForgeData", slimpatchData);

        CompoundTag root = new CompoundTag();
        root.put("EntityTag", entityTag);

        stack.set(DataComponents.ENTITY_DATA, CustomData.of(root));

        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        SlimPatch.LOGGER.info("[SlimPatch] Huevo {} usado en posición {} en dimensión {}",
                isMale ? "male" : "female",
                context.getClickedPos(),
                context.getLevel().dimension().location());

        return super.useOn(context);
    }
}