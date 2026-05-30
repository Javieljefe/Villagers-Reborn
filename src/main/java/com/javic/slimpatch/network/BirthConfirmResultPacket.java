package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BirthConfirmResultPacket implements CustomPacketPayload {

    private final boolean success;
    private final String translationKey;
    private final String translationArgument;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "birth_confirm_result");
    public static final Type<BirthConfirmResultPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BirthConfirmResultPacket> CODEC =
            StreamCodec.of(BirthConfirmResultPacket::encode, BirthConfirmResultPacket::decode);

    public BirthConfirmResultPacket(boolean success, String translationKey, String translationArgument) {
        this.success = success;
        this.translationKey = translationKey;
        this.translationArgument = translationArgument;
    }

    private static void encode(FriendlyByteBuf buf, BirthConfirmResultPacket packet) {
        buf.writeBoolean(packet.success);
        buf.writeUtf(packet.translationKey);
        buf.writeUtf(packet.translationArgument == null ? "" : packet.translationArgument);
    }

    private static BirthConfirmResultPacket decode(FriendlyByteBuf buf) {
        return new BirthConfirmResultPacket(buf.readBoolean(), buf.readUtf(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BirthConfirmResultPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Object minecraft = Class.forName("net.minecraft.client.Minecraft")
                        .getMethod("getInstance")
                        .invoke(null);
                Object screen = minecraft.getClass().getField("screen").get(minecraft);
                if (screen != null && screen.getClass().getName().equals("com.javic.slimpatch.client.gui.BirthChildScreen")) {
                    screen.getClass().getMethod("handleConfirmResult", boolean.class, String.class, String.class).invoke(screen, msg.success, msg.translationKey, msg.translationArgument);
                    return;
                }
                Object player = minecraft.getClass().getField("player").get(minecraft);
                if (player != null) {
                    Object component = Class.forName("net.minecraft.network.chat.Component")
                            .getMethod("translatable", String.class, Object[].class)
                            .invoke(null, msg.translationKey, msg.translationArgument.isEmpty() ? new Object[0] : new Object[]{msg.translationArgument});
                    player.getClass().getMethod("displayClientMessage", Class.forName("net.minecraft.network.chat.Component"), boolean.class)
                            .invoke(player, component, true);
                }
            } catch (Exception ignored) {
            }
        });
    }
}
