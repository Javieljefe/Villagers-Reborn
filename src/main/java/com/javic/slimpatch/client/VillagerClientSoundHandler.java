package com.javic.slimpatch.client;

import com.javic.slimpatch.Config;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = "slimpatch", value = Dist.CLIENT)
public class VillagerClientSoundHandler {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!Config.CUSTOM_VILLAGER_SOUNDS.get()) return;
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        String name = event.getName();

        if (name.startsWith("entity.villager.") || name.startsWith("entity.wandering_trader.")) {

            if (name.equals("entity.villager.ambient") || name.equals("entity.villager.celebrate")
                    || name.equals("entity.wandering_trader.ambient") || name.equals("entity.wandering_trader.trade")) {
                event.setSound(null);
                return;
            }

            if (name.equals(SoundEvents.VILLAGER_NO.getLocation().getPath())
                    || name.equals(SoundEvents.VILLAGER_YES.getLocation().getPath())) {
                event.setSound(null);
            }
        }
    }
}
