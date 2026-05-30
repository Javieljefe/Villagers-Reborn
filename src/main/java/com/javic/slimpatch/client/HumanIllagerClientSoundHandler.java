package com.javic.slimpatch.client;

import com.javic.slimpatch.Config;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = "slimpatch", value = Dist.CLIENT)
public class HumanIllagerClientSoundHandler {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!Config.CUSTOM_ILLAGER_SOUNDS.get()) return;

        SoundInstance sound = event.getSound();
        if (sound == null) return;

        String name = event.getName();
        if (name == null) return;

        if (name.startsWith("entity.pillager.") ||
            name.startsWith("entity.vindicator.") ||
            name.startsWith("entity.evoker.")) {

            if (name.contains("ambient") || name.contains("hurt") || name.contains("death")) {
                event.setSound(null);
            }
        }
    }
}
