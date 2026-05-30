package com.javic.slimpatch.events;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.HumanZombieVillagerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

@EventBusSubscriber(modid = SlimPatch.MODID)
public class ZombieVillagerDataHandler {

    @SubscribeEvent
    public static void onConversionPre(LivingConversionEvent.Pre event) {
        if (event.getEntity() instanceof HumanZombieVillagerEntity) {
            event.setCanceled(true);
        }
    }
}
