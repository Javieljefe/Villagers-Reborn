package com.javic.slimpatch.client.camera;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.client.gui.VillagerDialogueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = SlimPatch.MODID, value = Dist.CLIENT)
public final class DialogueThirdPersonCameraHandler {
    private static final float MAX_PITCH_UP_OFFSET = 18.0F;
    private static final float MAX_PITCH_DOWN_OFFSET = 12.0F;

    private static float yawOffset;
    private static float pitchOffset;

    private DialogueThirdPersonCameraHandler() {
    }

    public static void update(float yawDelta, float pitchDelta, float sensitivity, float deltaTicks) {
        if (deltaTicks <= 0.0F) {
            return;
        }

        yawOffset = Mth.wrapDegrees(yawOffset + yawDelta * sensitivity * deltaTicks);
        pitchOffset = Mth.clamp(pitchOffset - pitchDelta * sensitivity * deltaTicks, -MAX_PITCH_DOWN_OFFSET, MAX_PITCH_UP_OFFSET);
    }

    public static void reset() {
        yawOffset = 0.0F;
        pitchOffset = 0.0F;
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof VillagerDialogueScreen)) {
            return;
        }
        if (minecraft.options == null || minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (minecraft.player == null || event.getCamera().getEntity() != minecraft.player) {
            return;
        }

        event.setYaw(event.getYaw() + yawOffset);
        event.setPitch(Mth.clamp(event.getPitch() + pitchOffset, -90.0F, 90.0F));
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof VillagerDialogueScreen) {
            reset();
        }
    }
}
