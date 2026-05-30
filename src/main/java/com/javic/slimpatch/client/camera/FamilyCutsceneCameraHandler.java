package com.javic.slimpatch.client.camera;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.client.cutscene.FamilyCutsceneController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = SlimPatch.MODID, value = Dist.CLIENT)
public final class FamilyCutsceneCameraHandler {

    private FamilyCutsceneCameraHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!FamilyCutsceneController.isActive()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getCamera().getEntity() != minecraft.player) {
            return;
        }

        float progress = FamilyCutsceneController.getProgress();
        float entry = Mth.clamp(progress / 0.65F, 0.0F, 1.0F);
        float smoothEntry = entry * entry * entry * (entry * (entry * 6.0F - 15.0F) + 10.0F);
        float drift = (float) Math.sin(progress * Math.PI * 2.0D) * 1.8F;
        float yawOffset = Mth.lerp(smoothEntry, 18.0F, 44.0F) + drift;
        float pitchOffset = Mth.lerp(smoothEntry, -5.0F, -8.0F + (float) Math.sin(progress * Math.PI) * -2.0F);
        float rollOffset = smoothEntry * (float) Math.sin(progress * Math.PI * 2.0D) * 0.35F;

        event.setYaw(event.getYaw() + yawOffset);
        event.setPitch(Mth.clamp(event.getPitch() + pitchOffset, -90.0F, 90.0F));
        event.setRoll(rollOffset);
    }
}
