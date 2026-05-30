package com.javic.slimpatch.client.cutscene;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.client.gui.FamilyCutsceneOverlayScreen;
import com.javic.slimpatch.sounds.HumanVillagerSounds;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = SlimPatch.MODID, value = Dist.CLIENT)
public final class FamilyCutsceneController {
    private static final int DURATION_TICKS = 200;
    private static final int FADE_IN_TICKS = 45;
    private static final int VISIBLE_SCENE_TICKS = 75;
    private static final int FADE_TO_BLACK_TICKS = 70;
    private static final int VANILLA_MUSIC_RESTART_DELAY_TICKS = 100;
    private static final int VANILLA_MUSIC_STOP_INTERVAL_TICKS = 5;
    private static final int SKIP_HINT_TICKS = 70;

    private static boolean active;
    private static int villagerEntityId = -1;
    private static String villagerName = "";
    private static int remainingTicks;
    private static CameraType previousCameraType;
    private static Entity previousCameraEntity;
    private static float previousYRot;
    private static float previousXRot;
    private static float previousYHeadRot;
    private static float previousYBodyRot;
    private static boolean previousHideGui;
    private static WeddingCutsceneMusicInstance musicInstance;
    private static int vanillaMusicRestartDelay;
    private static int vanillaMusicStopCooldown;
    private static int skipHintTicks;

    private FamilyCutsceneController() {
    }

    public static boolean start(int entityId, String targetVillagerName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof LocalPlayer player) || minecraft.level == null || minecraft.level.getEntity(entityId) == null) {
            return false;
        }

        if (active) {
            finish(false);
        }

        active = true;
        villagerEntityId = entityId;
        villagerName = targetVillagerName;
        remainingTicks = DURATION_TICKS;
        vanillaMusicRestartDelay = 0;
        vanillaMusicStopCooldown = 0;
        skipHintTicks = 0;
        WeddingMusicDuckingHandler.keepDuckedForCutscene();
        previousCameraType = minecraft.options.getCameraType();
        previousCameraEntity = minecraft.getCameraEntity();
        previousYRot = player.getYRot();
        previousXRot = player.getXRot();
        previousYHeadRot = player.yHeadRot;
        previousYBodyRot = player.yBodyRot;
        previousHideGui = minecraft.options.hideGui;
        playFamilyMusic(minecraft);
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraft.options.hideGui = true;
        if (minecraft.getCameraEntity() != player) {
            minecraft.setCameraEntity(player);
        }
        minecraft.setScreen(new FamilyCutsceneOverlayScreen());
        return true;
    }

    public static boolean isActive() {
        return active;
    }

    public static int getVillagerEntityId() {
        return villagerEntityId;
    }

    public static float getProgress() {
        return active ? 1.0F - (float) remainingTicks / (float) DURATION_TICKS : 0.0F;
    }

    public static int getOverlayAlpha() {
        if (!active) {
            return 0;
        }

        int elapsedTicks = DURATION_TICKS - remainingTicks;
        if (elapsedTicks < FADE_IN_TICKS) {
            float progress = (float) elapsedTicks / (float) FADE_IN_TICKS;
            float eased = 1.0F - progress * progress * (3.0F - 2.0F * progress);
            return (int) (eased * 255.0F);
        }

        if (elapsedTicks < VISIBLE_SCENE_TICKS) {
            return 0;
        }

        int fadeTicks = elapsedTicks - VISIBLE_SCENE_TICKS;
        if (fadeTicks < FADE_TO_BLACK_TICKS) {
            float progress = (float) fadeTicks / (float) FADE_TO_BLACK_TICKS;
            float eased = progress * progress * (3.0F - 2.0F * progress);
            return (int) (eased * 255.0F);
        }

        return 255;
    }

    public static boolean shouldShowSkipHint() {
        return active && skipHintTicks > 0;
    }

    public static void handleSkipRequest() {
        if (!active) {
            return;
        }
        if (skipHintTicks > 0) {
            finish(true);
            return;
        }
        skipHintTicks = SKIP_HINT_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickVanillaMusicRestart();

        if (!active) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof LocalPlayer player) || minecraft.level == null) {
            finish(false);
            return;
        }
        if (!player.isAlive()) {
            finish(true);
            return;
        }

        Entity villager = minecraft.level.getEntity(villagerEntityId);
        if (villager == null || villager.level() != player.level()) {
            finish(true);
            return;
        }
        if (minecraft.screen != null && !(minecraft.screen instanceof FamilyCutsceneOverlayScreen)) {
            finish(true);
            return;
        }

        if (skipHintTicks > 0) {
            skipHintTicks--;
        }

        stopVanillaMusicDuringCutscene(minecraft);

        orientPlayerTowards(player, villager);
        orientVillagerTowards(player, villager);

        remainingTicks--;
        if (remainingTicks <= 0) {
            finish(true);
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!active || Minecraft.getInstance().player != event.getEntity()) {
            return;
        }
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().leftImpulse = 0.0F;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    public static void finish(boolean showMessage) {
        if (!active) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.setYRot(previousYRot);
            minecraft.player.setXRot(previousXRot);
            minecraft.player.yHeadRot = previousYHeadRot;
            minecraft.player.yBodyRot = previousYBodyRot;
            if (showMessage) {
                minecraft.player.displayClientMessage(Component.translatable("slimpatch.message.family_started", villagerName), false);
            }
        }
        if (minecraft.options != null && previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
        }
        if (minecraft.options != null) {
            minecraft.options.hideGui = previousHideGui;
        }
        if (previousCameraEntity != null) {
            minecraft.setCameraEntity(previousCameraEntity);
        }
        if (minecraft.screen instanceof FamilyCutsceneOverlayScreen) {
            minecraft.setScreen(null);
        }
        stopFamilyMusic(minecraft);
        WeddingMusicDuckingHandler.restore();
        scheduleVanillaMusicRestart();

        active = false;
        villagerEntityId = -1;
        villagerName = "";
        remainingTicks = 0;
        skipHintTicks = 0;
        previousCameraType = null;
        previousCameraEntity = null;
    }

    private static void stopVanillaMusicDuringCutscene(Minecraft minecraft) {
        if (vanillaMusicStopCooldown > 0) {
            vanillaMusicStopCooldown--;
            return;
        }

        minecraft.getMusicManager().stopPlaying();
        vanillaMusicStopCooldown = VANILLA_MUSIC_STOP_INTERVAL_TICKS;
    }

    private static void scheduleVanillaMusicRestart() {
        vanillaMusicRestartDelay = VANILLA_MUSIC_RESTART_DELAY_TICKS;
    }

    private static void tickVanillaMusicRestart() {
        if (vanillaMusicRestartDelay <= 0) {
            return;
        }

        vanillaMusicRestartDelay--;
        if (vanillaMusicRestartDelay > 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options == null) {
            return;
        }
        if (minecraft.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MUSIC) <= 0.0F) {
            return;
        }

        Music music = minecraft.getSituationalMusic();
        if (music != null) {
            minecraft.getMusicManager().startPlaying(music);
        }
    }

    private static void playFamilyMusic(Minecraft minecraft) {
        stopFamilyMusic(minecraft);
        if (minecraft.player == null || minecraft.level == null || minecraft.getSoundManager() == null) {
            return;
        }
        SoundEvent soundEvent = switch (minecraft.level.random.nextInt(4)) {
            case 1 -> HumanVillagerSounds.familyCutscene2();
            case 2 -> HumanVillagerSounds.familyCutscene3();
            case 3 -> HumanVillagerSounds.familyCutscene4();
            default -> HumanVillagerSounds.familyCutscene1();
        };
        musicInstance = new WeddingCutsceneMusicInstance(soundEvent);
        minecraft.getSoundManager().play(musicInstance);
    }

    private static void stopFamilyMusic(Minecraft minecraft) {
        if (musicInstance == null || minecraft.getSoundManager() == null) {
            musicInstance = null;
            return;
        }
        musicInstance.stopPlayback();
        minecraft.getSoundManager().stop(musicInstance);
        musicInstance = null;
    }

    private static void orientPlayerTowards(LocalPlayer player, Entity villager) {
        double dx = villager.getX() - player.getX();
        double dy = villager.getEyeY() - player.getEyeY();
        double dz = villager.getZ() - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        player.setYRot(Mth.rotLerp(0.25F, player.getYRot(), targetYaw));
        player.setXRot(Mth.lerp(0.25F, player.getXRot(), targetPitch * 0.35F));
        player.yHeadRot = player.getYRot();
        player.yBodyRot = player.getYRot();
    }

    private static void orientVillagerTowards(LocalPlayer player, Entity villager) {
        double dx = player.getX() - villager.getX();
        double dz = player.getZ() - villager.getZ();
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        villager.setYRot(Mth.rotLerp(0.3F, villager.getYRot(), targetYaw));
        if (villager instanceof net.minecraft.world.entity.LivingEntity living) {
            living.yHeadRot = villager.getYRot();
            living.yBodyRot = villager.getYRot();
        }
    }
}
