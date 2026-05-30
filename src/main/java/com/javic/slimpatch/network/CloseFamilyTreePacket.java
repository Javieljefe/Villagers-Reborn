package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FamilyTreeViewHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CloseFamilyTreePacket implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "close_family_tree");
    public static final Type<CloseFamilyTreePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, CloseFamilyTreePacket> CODEC = StreamCodec.of(CloseFamilyTreePacket::encode, CloseFamilyTreePacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, CloseFamilyTreePacket packet) {
    }

    private static CloseFamilyTreePacket decode(FriendlyByteBuf buf) {
        return new CloseFamilyTreePacket();
    }

    public static void handle(CloseFamilyTreePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                FamilyTreeViewHandler.endViewing(player);
            }
        });
    }
}
