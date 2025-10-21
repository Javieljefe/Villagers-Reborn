package com.javic.slimpatch.client.gui;

import com.javic.slimpatch.client.ClientSkinTheme;
import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.data.WorldSkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.fml.loading.FMLEnvironment;

public class SkinSelectionHandler {

    private static boolean checked = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || checked) return;
        checked = true;

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

                String configTheme = SlimPatchConfig.SERVER.skinType.get();
                if (configTheme != null && !configTheme.isEmpty() && !configTheme.equals(theme)) {
                    data.setTheme(configTheme);
                    data.setDirty();
                    theme = configTheme;
                }

                ClientSkinTheme.setTheme(theme);
            }
            return;
        }

        if (!FMLEnvironment.production) return;
        String configTheme = SlimPatchConfig.SERVER.skinType.get();
        if (configTheme != null && !configTheme.isEmpty()) {
            ClientSkinTheme.setTheme(configTheme);
        } else {
            ClientSkinTheme.setTheme("modern");
        }

        System.out.println("[SlimPatch] Aplicando tema de skins desde config del servidor: " + ClientSkinTheme.getTheme());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        checked = false;
        ClientSkinTheme.setTheme("");
        com.javic.slimpatch.network.VillagerCooldownsStorage.clear();
    }
}