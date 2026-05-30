package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.entity.VillagerCommandState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VillagerCommandPacket implements CustomPacketPayload {

    private static final double MAX_COMMAND_DISTANCE_SQR = 64.0D;

    public enum Action {
        COMMAND,
        SET_HOME,
        CLEAR_HOME,
        STOP_FOLLOWING,
        MOVE_FREELY
    }

    private final int entityId;
    private final Action action;
    private final VillagerCommandState commandState;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_command");
    public static final Type<VillagerCommandPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerCommandPacket> CODEC =
            StreamCodec.of(VillagerCommandPacket::encode, VillagerCommandPacket::decode);

    public VillagerCommandPacket(int entityId, VillagerCommandState commandState) {
        this(entityId, Action.COMMAND, commandState);
    }

    public VillagerCommandPacket(int entityId, Action action) {
        this(entityId, action, VillagerCommandState.NONE);
    }

    private VillagerCommandPacket(int entityId, Action action, VillagerCommandState commandState) {
        this.entityId = entityId;
        this.action = action;
        this.commandState = commandState;
    }

    private static void encode(FriendlyByteBuf buf, VillagerCommandPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeEnum(packet.action);
        buf.writeEnum(packet.commandState);
    }

    private static VillagerCommandPacket decode(FriendlyByteBuf buf) {
        return new VillagerCommandPacket(buf.readVarInt(), buf.readEnum(Action.class), buf.readEnum(VillagerCommandState.class));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerCommandPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof Villager villager) || !(entity instanceof CommandableVillager commandableVillager)) return;
            if (player.distanceToSqr(villager) > MAX_COMMAND_DISTANCE_SQR) return;
            if (!VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, player)) return;

            switch (msg.action) {
                case SET_HOME -> {
                    VillagerCommandHandler.setHome(villager, commandableVillager, player);
                    player.displayClientMessage(Component.translatable("slimpatch.message.home_set"), true);
                }
                case CLEAR_HOME -> {
                    VillagerCommandHandler.clearHome(villager, commandableVillager);
                    player.displayClientMessage(Component.translatable("slimpatch.message.home_cleared"), true);
                }
                case STOP_FOLLOWING -> {
                    VillagerCommandHandler.stopFollowing(villager, commandableVillager);
                }
                case MOVE_FREELY -> {
                    VillagerCommandHandler.moveFreely(villager, commandableVillager);
                }
                case COMMAND -> {
                    if (msg.commandState == VillagerCommandState.NONE) {
                        VillagerCommandHandler.resetCommand(villager, commandableVillager);
                    } else {
                        VillagerCommandHandler.applyCommand(villager, commandableVillager, msg.commandState, player);
                    }
                }
            }
        });
    }
}
