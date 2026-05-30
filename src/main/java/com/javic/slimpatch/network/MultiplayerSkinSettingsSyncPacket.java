package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MultiplayerSkinSettingsSyncPacket implements CustomPacketPayload {

    private final boolean allowMultiplayerCustomSkins;
    private final int maxCustomSkinSizeKb;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "multiplayer_skin_settings_sync");
    public static final Type<MultiplayerSkinSettingsSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MultiplayerSkinSettingsSyncPacket> CODEC =
            StreamCodec.of(MultiplayerSkinSettingsSyncPacket::encode, MultiplayerSkinSettingsSyncPacket::decode);

    public MultiplayerSkinSettingsSyncPacket(boolean allowMultiplayerCustomSkins, int maxCustomSkinSizeKb) {
        this.allowMultiplayerCustomSkins = allowMultiplayerCustomSkins;
        this.maxCustomSkinSizeKb = maxCustomSkinSizeKb;
    }

    private static void encode(FriendlyByteBuf buf, MultiplayerSkinSettingsSyncPacket packet) {
        buf.writeBoolean(packet.allowMultiplayerCustomSkins);
        buf.writeVarInt(packet.maxCustomSkinSizeKb);
    }

    private static MultiplayerSkinSettingsSyncPacket decode(FriendlyByteBuf buf) {
        return new MultiplayerSkinSettingsSyncPacket(buf.readBoolean(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultiplayerSkinSettingsSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> settingsClass = Class.forName("com.javic.slimpatch.client.ClientMultiplayerSkinSettings");
                settingsClass.getMethod("set", boolean.class, int.class)
                        .invoke(null, msg.allowMultiplayerCustomSkins, msg.maxCustomSkinSizeKb);
            } catch (Exception ignored) {
            }
        });
    }
}
