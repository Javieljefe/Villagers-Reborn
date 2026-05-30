package com.javic.slimpatch.entity;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SlimPatch.MODID)
public final class FamilyTreeViewHandler {

    private static final Map<UUID, ViewState> ACTIVE = new HashMap<>();

    private FamilyTreeViewHandler() {
    }

    public static void beginViewing(ServerPlayer player, Villager villager) {
        if (player == null || villager == null || !(villager instanceof CommandableVillager commandableVillager)) {
            return;
        }
        endViewing(player);
        VillagerCommandHandler.TemporaryCommandStateSnapshot snapshot = VillagerCommandHandler.createTemporaryStaySnapshot(commandableVillager);
        boolean temporaryStayApplied = VillagerCommandHandler.beginTemporaryStay(villager, commandableVillager);
        ACTIVE.put(player.getUUID(), new ViewState(villager.getUUID(), snapshot, temporaryStayApplied));
    }

    public static void endViewing(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ViewState state = ACTIVE.remove(player.getUUID());
        if (state == null) {
            return;
        }
        if (!state.temporaryStayApplied()) {
            return;
        }
        if (!(player.serverLevel().getEntity(state.villagerUuid()) instanceof Villager villager)) {
            return;
        }
        if (!(villager instanceof CommandableVillager commandableVillager)) {
            return;
        }
        VillagerCommandHandler.restoreTemporaryStay(villager, commandableVillager, state.snapshot());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            endViewing(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            endViewing(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE.clear();
    }

    private record ViewState(UUID villagerUuid, VillagerCommandHandler.TemporaryCommandStateSnapshot snapshot, boolean temporaryStayApplied) {
    }
}
