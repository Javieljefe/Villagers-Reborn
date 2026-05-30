package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfirmDivorcePacket implements CustomPacketPayload {

    private static final double MAX_DIVORCE_DISTANCE_SQR = 64.0D;

    private final int entityId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "confirm_divorce");
    public static final Type<ConfirmDivorcePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ConfirmDivorcePacket> CODEC =
            StreamCodec.of(ConfirmDivorcePacket::encode, ConfirmDivorcePacket::decode);

    public ConfirmDivorcePacket(int entityId) {
        this.entityId = entityId;
    }

    private static void encode(FriendlyByteBuf buf, ConfirmDivorcePacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static ConfirmDivorcePacket decode(FriendlyByteBuf buf) {
        return new ConfirmDivorcePacket(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfirmDivorcePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.entityId);
            if (entity == null || player.distanceToSqr(entity) > MAX_DIVORCE_DISTANCE_SQR) return;

            if (entity instanceof MaleVillagerEntity male) {
                male.confirmDivorce(player);
            } else if (entity instanceof FemaleVillagerEntity female) {
                female.confirmDivorce(player);
            }
        });
    }
}
