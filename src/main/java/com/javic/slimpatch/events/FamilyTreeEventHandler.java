package com.javic.slimpatch.events;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = SlimPatch.MODID)
public class FamilyTreeEventHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (event.getEntity() instanceof MaleVillagerEntity maleVillager) {
            FamilyTreeTracker.upsertVillager(serverLevel.getServer(), maleVillager);
        } else if (event.getEntity() instanceof FemaleVillagerEntity femaleVillager) {
            FamilyTreeTracker.upsertVillager(serverLevel.getServer(), femaleVillager);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (event.getEntity() instanceof MaleVillagerEntity || event.getEntity() instanceof FemaleVillagerEntity) {
            FamilyTreeTracker.markVillagerDead(serverLevel.getServer(), event.getEntity().getUUID());
        }
    }
}
