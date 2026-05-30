package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCombatMode;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.menu.VillagerEquipmentMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VillagerCombatModePacket implements CustomPacketPayload {

    private static final double MAX_COMMAND_DISTANCE_SQR = 64.0D;

    private final int entityId;
    private final VillagerCombatMode combatMode;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_combat_mode");
    public static final Type<VillagerCombatModePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerCombatModePacket> CODEC =
            StreamCodec.of(VillagerCombatModePacket::encode, VillagerCombatModePacket::decode);

    public VillagerCombatModePacket(int entityId, VillagerCombatMode combatMode) {
        this.entityId = entityId;
        this.combatMode = combatMode == null ? VillagerCombatMode.DEFENSIVE : combatMode;
    }

    private static void encode(FriendlyByteBuf buf, VillagerCombatModePacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeEnum(packet.combatMode);
    }

    private static VillagerCombatModePacket decode(FriendlyByteBuf buf) {
        return new VillagerCombatModePacket(buf.readVarInt(), buf.readEnum(VillagerCombatMode.class));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerCombatModePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof VillagerEquipmentMenu equipmentMenu)) return;
            if (!equipmentMenu.isForVillager(msg.entityId) || !equipmentMenu.stillValid(player)) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof Villager villager) || !(entity instanceof CommandableVillager commandableVillager)) return;
            if (player.distanceToSqr(villager) > MAX_COMMAND_DISTANCE_SQR) return;
            if (!VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, player)) return;

            commandableVillager.setCombatMode(msg.combatMode);
        });
    }
}
