package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FamilyTreeViewHandler;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.familytree.FamilyTreeGraphBuilder;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestFamilyTreePacket implements CustomPacketPayload {

    private static final double MAX_DISTANCE_SQR = 64.0D;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "request_family_tree");
    public static final Type<RequestFamilyTreePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RequestFamilyTreePacket> CODEC = StreamCodec.of(RequestFamilyTreePacket::encode, RequestFamilyTreePacket::decode);

    private final int villagerEntityId;

    public RequestFamilyTreePacket(int villagerEntityId) {
        this.villagerEntityId = villagerEntityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, RequestFamilyTreePacket packet) {
        buf.writeVarInt(packet.villagerEntityId);
    }

    private static RequestFamilyTreePacket decode(FriendlyByteBuf buf) {
        return new RequestFamilyTreePacket(buf.readVarInt());
    }

    public static void handle(RequestFamilyTreePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(msg.villagerEntityId);
            if (!(entity instanceof Villager villager)) {
                return;
            }
            if (!(entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) {
                return;
            }
            if (player.distanceToSqr(villager) > MAX_DISTANCE_SQR) {
                return;
            }
            FamilyTreeViewHandler.beginViewing(player, villager);
            FamilyTreeTracker.upsertVillager(player.serverLevel().getServer(), villager);
            ModNetworking.sendToClient(new FamilyTreeDataPacket(FamilyTreeGraphBuilder.build(player.serverLevel().getServer(), villager.getUUID())), player);
        });
    }
}
