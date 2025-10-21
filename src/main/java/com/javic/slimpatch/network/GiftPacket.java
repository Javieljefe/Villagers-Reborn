package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GiftPacket implements CustomPacketPayload {

    private final int entityId;

    public GiftPacket(int entityId) {
        this.entityId = entityId;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "gift");
    public static final Type<GiftPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, GiftPacket> CODEC =
            StreamCodec.of(GiftPacket::encode, GiftPacket::decode);

    private static void encode(FriendlyByteBuf buf, GiftPacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static GiftPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        return new GiftPacket(entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GiftPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (entity instanceof MaleVillagerEntity male) {
                male.giveGiftToPlayer(player);
            } else if (entity instanceof FemaleVillagerEntity female) {
                female.giveGiftToPlayer(player);
            }
        });
    }
}