package com.javic.slimpatch.events;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.entity.HumanPillagerEntity;
import com.javic.slimpatch.sounds.HumanIllagerSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = "slimpatch")
public class HumanIllagerSoundHandler {

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (!Config.CUSTOM_ILLAGER_SOUNDS.get()) return;
        if (event.getLevel().isClientSide()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof HumanPillagerEntity humanPillager)) return;

        if (humanPillager.getPersistentData().getBoolean("slimpatch_spawn_sound_played")) return;
        humanPillager.getPersistentData().putBoolean("slimpatch_spawn_sound_played", true);

        event.getLevel().getServer().execute(() -> {
            boolean isFemale = humanPillager.isFemale();
            event.getLevel().playSound(null, humanPillager.blockPosition(),
                    isFemale ? HumanIllagerSounds.femaleAmbient() : HumanIllagerSounds.maleAmbient(),
                    SoundSource.HOSTILE, 1.0F, 1.0F);
        });
    }
}
