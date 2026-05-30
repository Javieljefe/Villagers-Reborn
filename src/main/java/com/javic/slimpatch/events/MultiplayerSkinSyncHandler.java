package com.javic.slimpatch.events;

import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.MultiplayerSkinSettingsSyncPacket;
import com.javic.slimpatch.network.VillagerCustomSkinSyncPacket;
import com.javic.slimpatch.util.MultiplayerSkinStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.IOException;

@EventBusSubscriber(modid = "slimpatch")
public class MultiplayerSkinSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;
        if (!player.getServer().isDedicatedServer() && !player.getServer().isPublished()) return;

        ModNetworking.sendToClient(
                new MultiplayerSkinSettingsSyncPacket(
                        SlimPatchConfig.SERVER.allowMultiplayerCustomSkins.get(),
                        SlimPatchConfig.SERVER.maxCustomSkinSizeKb.get()
                ),
                player
        );
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        sendExistingSkin(villager, player);
    }

    public static void broadcastSkin(Villager villager, byte[] pngData, String savedSkinInput, boolean clear) {
        if (villager.getServer() == null) {
            return;
        }

        for (ServerPlayer player : villager.getServer().getPlayerList().getPlayers()) {
            if (player.level() == villager.level()) {
                ModNetworking.sendToClient(
                        new VillagerCustomSkinSyncPacket(villager.getId(), villager.getUUID(), savedSkinInput, pngData, clear),
                        player
                );
            }
        }
    }

    public static void sendExistingSkin(Villager villager, ServerPlayer player) {
        if (!villager.getPersistentData().contains("CustomSkinPath")) {
            return;
        }

        try {
            byte[] pngData = MultiplayerSkinStorage.readSkin(villager.getUUID());
            if (pngData == null || pngData.length == 0) {
                return;
            }

            ModNetworking.sendToClient(
                    new VillagerCustomSkinSyncPacket(
                            villager.getId(),
                            villager.getUUID(),
                            villager.getPersistentData().getString("SavedSkinInput"),
                            pngData,
                            false
                    ),
                    player
            );
        } catch (IOException ignored) {
        }
    }
}
