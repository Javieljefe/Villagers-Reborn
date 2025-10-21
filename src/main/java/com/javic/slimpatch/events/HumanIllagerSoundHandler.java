package com.javic.slimpatch.events;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.HumanPillagerEntity;
import com.javic.slimpatch.sounds.HumanIllagerSounds;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = "slimpatch")
public class HumanIllagerSoundHandler {

    // ============================================================
    // 🎧 Bloquea sonidos vanilla de Illagers
    // ============================================================
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        String name = event.getName();
        if (name == null) return;

        if (name.startsWith("entity.pillager.") ||
            name.startsWith("entity.vindicator.") ||
            name.startsWith("entity.evoker.")) {

            if (name.contains("ambient") || name.contains("hurt") || name.contains("death")) {
                event.setSound(null);
                SlimPatch.LOGGER.debug("[SlimPatch] Sonido vanilla bloqueado ({}).", name);
            }
        }
    }

    // ============================================================
    // 🧍 Sonido al spawnear (solo inicial)
    // ============================================================
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
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

            SlimPatch.LOGGER.debug("[SlimPatch] Sonido inicial reproducido ({}).",
                    isFemale ? "femaleAmbient" : "maleAmbient");
        });
    }
}