package com.javic.slimpatch.events;

import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.network.ModNetworking;
import com.javic.slimpatch.network.SkinThemeSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "slimpatch")
public class SkinThemeSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;
        if (!player.getServer().isDedicatedServer() && !player.getServer().isPublished()) return;

        String theme = SlimPatchConfig.SERVER.skinType.get();
        if (theme == null || theme.isEmpty()) {
            theme = "modern";
        }

        ModNetworking.sendToClient(new SkinThemeSyncPacket(theme), player);
    }
}
