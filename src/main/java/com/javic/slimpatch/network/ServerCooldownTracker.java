package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.dialogue.DialogueManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 * 🔹 ServerCooldownTracker
 * ============================================================
 * - ÚNICA fuente de decremento de cooldowns (1 s por segundo real).
 * - Los clientes ya no aplican su propio tick; sólo muestran los valores.
 * - Envía actualizaciones al jugador en diálogo cuando cambian.
 * - Usa VillagerCooldownsStorage como almacenamiento global persistente.
 */
public class ServerCooldownTracker {

    private static int tickCounter = 0;

    public static void init() {
        NeoForge.EVENT_BUS.register(ServerCooldownTracker.class);
        SlimPatch.LOGGER.info("[SlimPatch] 🔄 ServerCooldownTracker registrado en NeoForge.");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        SlimPatch.LOGGER.info("[SlimPatch] 🟢 ServerCooldownTracker iniciado correctamente.");
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return; // 1 vez por segundo (20 ticks)

        Map<UUID, Map<String, Integer>> allCooldowns = new HashMap<>(VillagerCooldownsStorage.getAll());
        if (allCooldowns.isEmpty()) return;

        Iterator<Map.Entry<UUID, Map<String, Integer>>> it = allCooldowns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Map<String, Integer>> entry = it.next();
            UUID villagerUUID = entry.getKey();
            Map<String, Integer> cooldowns = entry.getValue();

            boolean changed = false;
            Map<String, Integer> updated = new HashMap<>();

            for (Map.Entry<String, Integer> c : cooldowns.entrySet()) {
                int oldValue = c.getValue();
                int newValue = Math.max(0, oldValue - 1);
                updated.put(c.getKey(), newValue);
                if (newValue != oldValue) changed = true;
            }

            if (!changed) continue;

            VillagerCooldownsStorage.setCooldowns(villagerUUID, updated);

            Villager villager = findVillagerByUUID(server, villagerUUID);
            if (villager != null) {
                villager.getPersistentData().putInt("slimpatch_cooldown_last_tick",
                        (int) (System.currentTimeMillis() / 1000L));

                UUID playerUUID = DialogueManager.getDialoguePlayer(villager);
                if (playerUUID != null) {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        PacketDistributor.sendToPlayer(
                                player,
                                new VillagerCooldownsPacket(villager.getUUID(), villager.getId(), updated)
                        );
                    }
                }
            }
        }
    }

    /**
     * Busca una entidad Villager por UUID en todos los niveles cargados.
     */
    private static Villager findVillagerByUUID(MinecraftServer server, UUID uuid) {
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e instanceof Villager v) return v;
        }
        return null;
    }
}