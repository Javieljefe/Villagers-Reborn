package com.javic.slimpatch.network;

import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerCooldownData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.util.Map;
import java.util.UUID;

public class ServerCooldownTracker {

    private static int tickCounter = 0;

    public static void init() {
        NeoForge.EVENT_BUS.register(ServerCooldownTracker.class);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        for (Map.Entry<UUID, UUID> entry : DialogueManager.getActiveDialogues().entrySet()) {
            Villager villager = findVillagerByUUID(server, entry.getKey());
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getValue());
            if (villager == null || player == null) continue;

            if (villager instanceof MaleVillagerEntity male) {
                VillagerCooldownData.syncDialogueCooldownsToPlayer(male, player, male.getOptionCooldowns());
            } else if (villager instanceof FemaleVillagerEntity female) {
                VillagerCooldownData.syncDialogueCooldownsToPlayer(female, player, female.getOptionCooldowns());
            }
        }
    }

    private static Villager findVillagerByUUID(MinecraftServer server, UUID uuid) {
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e instanceof Villager v) return v;
        }
        return null;
    }
}
