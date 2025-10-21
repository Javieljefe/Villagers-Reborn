package com.javic.slimpatch.events;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.HumanWanderingTraderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Random;

/**
 * 🌍 Spawner natural para el comerciante humano errante.
 * Compatible con NeoForge 1.21.1.
 * ✅ Permite hasta 10 comerciantes activos simultáneamente.
 * 💬 Muestra un mensaje de chat al jugador más cercano al spawn.
 */
@EventBusSubscriber(modid = SlimPatch.MODID)
public class HumanWanderingTraderSpawner {

    private static final int CHECK_INTERVAL_TICKS = 168000; // ~7 dias de juego
    private static final int MAX_ACTIVE_TRADERS = 20; // hasta 20 comerciantes activos
    private static final int SPAWN_RADIUS = 48;
    private static int tickCounter = 0;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL_TICKS) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension() != Level.OVERWORLD) continue;

            // Contar traders activos (busca en todo el mundo con AABB muy grande)
            AABB searchArea = new AABB(-10000, -64, -10000, 10000, 512, 10000);
            List<HumanWanderingTraderEntity> traders = level.getEntitiesOfClass(HumanWanderingTraderEntity.class, searchArea);
            if (traders.size() >= MAX_ACTIVE_TRADERS) continue;

            // Seleccionar un jugador aleatorio
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) continue;
            ServerPlayer player = players.get(random.nextInt(players.size()));

            // Posición aleatoria cerca del jugador
            int dx = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
            int dz = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
            BlockPos pos = player.blockPosition().offset(dx, 0, dz);
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);

            // Validar terreno
            BlockState blockBelow = level.getBlockState(surfacePos.below());
            boolean isSolid = blockBelow.isSolid();
            int light = level.getMaxLocalRawBrightness(surfacePos);
            if (!isSolid || light < 8) continue;

            // Crear y spawnear
            HumanWanderingTraderEntity trader = ModEntities.HUMAN_TRADER_NATURAL.get().create(level);
            if (trader == null) continue;

            trader.moveTo(Vec3.atBottomCenterOf(surfacePos));
            trader.finalizeSpawn(level, level.getCurrentDifficultyAt(surfacePos), MobSpawnType.NATURAL, null);
            level.addFreshEntity(trader);

            // 💬 Mensaje en chat solo para el jugador cercano
            player.displayClientMessage(Component.literal("§eA merchant has arrived nearby."), false);

            SlimPatch.LOGGER.info("[SlimPatch] 🧳 Comerciante humano errante spawneado en {} ({})",
                    surfacePos, level.dimension().location());
        }
    }
}