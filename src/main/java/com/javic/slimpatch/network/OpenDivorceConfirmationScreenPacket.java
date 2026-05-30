package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenDivorceConfirmationScreenPacket implements CustomPacketPayload {

    private final int entityId;
    private final String villagerName;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "open_divorce_confirmation_screen");
    public static final Type<OpenDivorceConfirmationScreenPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenDivorceConfirmationScreenPacket> CODEC =
            StreamCodec.of(OpenDivorceConfirmationScreenPacket::encode, OpenDivorceConfirmationScreenPacket::decode);

    public OpenDivorceConfirmationScreenPacket(int entityId, String villagerName) {
        this.entityId = entityId;
        this.villagerName = villagerName;
    }

    private static void encode(FriendlyByteBuf buf, OpenDivorceConfirmationScreenPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.villagerName);
    }

    private static OpenDivorceConfirmationScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenDivorceConfirmationScreenPacket(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenDivorceConfirmationScreenPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Object minecraft = Class.forName("net.minecraft.client.Minecraft")
                        .getMethod("getInstance")
                        .invoke(null);
                Class<?> screenClass = Class.forName("com.javic.slimpatch.client.gui.DivorceConfirmationScreen");
                Object screen = screenClass.getConstructor(int.class, String.class).newInstance(msg.entityId, msg.villagerName);
                minecraft.getClass()
                        .getMethod("setScreen", Class.forName("net.minecraft.client.gui.screens.Screen"))
                        .invoke(minecraft, screen);
            } catch (Exception ignored) {
            }
        });
    }
}
