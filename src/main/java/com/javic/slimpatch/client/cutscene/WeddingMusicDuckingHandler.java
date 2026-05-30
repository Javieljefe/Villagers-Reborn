package com.javic.slimpatch.client.cutscene;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = SlimPatch.MODID, value = Dist.CLIENT)
public final class WeddingMusicDuckingHandler {
    private static final float DUCKED_FACTOR = 0.0F;
    private static final float STEP = 1.0F / 70.0F;

    private static float currentFactor = 1.0F;
    private static float targetFactor = 1.0F;
    private static boolean proposalDucking;
    private static boolean cutsceneDucking;
    private static boolean stoppedVanillaMusic;

    private WeddingMusicDuckingHandler() {
    }

    public static void startProposalDucking() {
        proposalDucking = true;
        targetFactor = DUCKED_FACTOR;
    }

    public static void keepDuckedForCutscene() {
        proposalDucking = false;
        cutsceneDucking = true;
        targetFactor = DUCKED_FACTOR;
        stoppedVanillaMusic = false;
    }

    public static void restore() {
        proposalDucking = false;
        cutsceneDucking = false;
        targetFactor = 1.0F;
        stoppedVanillaMusic = false;
        applyVolume();
    }

    public static void cleanup() {
        proposalDucking = false;
        cutsceneDucking = false;
        targetFactor = 1.0F;
        currentFactor = 1.0F;
        stoppedVanillaMusic = false;
        applyVolume();
    }

    public static boolean isCutsceneDucking() {
        return cutsceneDucking;
    }

    public static boolean isProposalDucking() {
        return proposalDucking;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (currentFactor < targetFactor) {
            currentFactor = Math.min(targetFactor, currentFactor + STEP);
            applyVolume();
        } else if (currentFactor > targetFactor) {
            currentFactor = Math.max(targetFactor, currentFactor - STEP);
            applyVolume();
        } else if (currentFactor == 1.0F && targetFactor == 1.0F) {
            applyVolume();
        }

        if (cutsceneDucking) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                restore();
                return;
            }
            if (!stoppedVanillaMusic) {
                minecraft.getMusicManager().stopPlaying();
                stoppedVanillaMusic = true;
            }
        }
    }

    private static void applyVolume() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null || minecraft.getSoundManager() == null) {
            return;
        }
        float baseVolume = minecraft.options.getSoundSourceVolume(SoundSource.MUSIC);
        minecraft.getSoundManager().updateSourceVolume(SoundSource.MUSIC, baseVolume * currentFactor);
    }
}