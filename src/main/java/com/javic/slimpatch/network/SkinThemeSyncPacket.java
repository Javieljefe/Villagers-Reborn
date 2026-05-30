package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SkinThemeSyncPacket implements CustomPacketPayload {

    private final String theme;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "skin_theme_sync");
    public static final Type<SkinThemeSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SkinThemeSyncPacket> CODEC =
            StreamCodec.of(SkinThemeSyncPacket::encode, SkinThemeSyncPacket::decode);

    public SkinThemeSyncPacket(String theme) {
        this.theme = theme;
    }

    private static void encode(FriendlyByteBuf buf, SkinThemeSyncPacket packet) {
        buf.writeUtf(packet.theme);
    }

    private static SkinThemeSyncPacket decode(FriendlyByteBuf buf) {
        return new SkinThemeSyncPacket(buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkinThemeSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> clientSkinThemeClass = Class.forName("com.javic.slimpatch.client.ClientSkinTheme");
                clientSkinThemeClass.getMethod("setTheme", String.class).invoke(null, msg.theme);
            } catch (Exception e) {
            }
        });
    }
}
