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

public class RelationshipPacket implements CustomPacketPayload {

    private final int entityId;
    private final String option;
    private final boolean success;

    public RelationshipPacket(int entityId, String option, boolean success) {
        this.entityId = entityId;
        this.option = option;
        this.success = success;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "relationship");
    public static final Type<RelationshipPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RelationshipPacket> CODEC =
            StreamCodec.of(RelationshipPacket::encode, RelationshipPacket::decode);

    private static void encode(FriendlyByteBuf buf, RelationshipPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.option);
        buf.writeBoolean(packet.success);
    }

    private static RelationshipPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        String option = buf.readUtf();
        boolean success = buf.readBoolean();
        return new RelationshipPacket(entityId, option, success);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RelationshipPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);

            if (entity instanceof MaleVillagerEntity male) {
                male.applyRelationshipChange(msg.option, msg.success);
            } else if (entity instanceof FemaleVillagerEntity female) {
                female.applyRelationshipChange(msg.option, msg.success);
            }
        });
    }
}