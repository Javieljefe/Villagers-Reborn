package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientSkinTheme;
import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.data.WorldSkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class SkinSelectionHandler {

    private static boolean checked = false;
    private static boolean published = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.getSingleplayerServer() != null) {
            boolean isPublished = mc.getSingleplayerServer().isPublished();

            if (!checked || published != isPublished) {
                checked = true;
                published = isPublished;

                if (isPublished) {
                    applyHostedTheme();
                } else {
                    applySingleplayerTheme(mc);
                }
            }

            return;
        }

        checked = true;
    }

    private static void applySingleplayerTheme(Minecraft mc) {
        if (mc.getSingleplayerServer() != null) {
            ServerLevel serverLevel = mc.getSingleplayerServer().overworld();
            if (serverLevel != null) {
                WorldSkinData data = WorldSkinData.get(serverLevel);
                String theme = data.getTheme();
                boolean guiShown = data.isGuiShown();

                if (!guiShown && (theme == null || theme.isEmpty())) {
                    mc.setScreen(new SkinSelectionScreen());
                    data.setGuiShown(true);
                    data.setDirty();
                    return;
                }

                if (theme != null && !theme.isEmpty()) {
                    ClientSkinTheme.setTheme(theme);
                } else {
                    ClientSkinTheme.clear();
                }
            }
        }
    }

    private static void applyHostedTheme() {
        String configTheme = SlimPatchConfig.SERVER.skinType.get();
        if (configTheme != null && !configTheme.isEmpty()) {
            ClientSkinTheme.setTheme(configTheme);
        } else {
            ClientSkinTheme.setTheme("modern");
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        checked = false;
        published = false;
        ClientSkinTheme.clear();
        com.javic.slimpatch.network.VillagerCooldownsStorage.clear();
    }
}
