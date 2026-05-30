package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DialogueResultPacket implements CustomPacketPayload {

    private final int entityId;
    private final String option;
    private final boolean success;
    private final String line;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "dialogue_result");
    public static final Type<DialogueResultPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DialogueResultPacket> CODEC =
            StreamCodec.of(DialogueResultPacket::encode, DialogueResultPacket::decode);

    public DialogueResultPacket(int entityId, String option, boolean success) {
        this(entityId, option, success, "");
    }

    public DialogueResultPacket(int entityId, String option, boolean success, String line) {
        this.entityId = entityId;
        this.option = option;
        this.success = success;
        this.line = line == null ? "" : line;
    }

    private static void encode(FriendlyByteBuf buf, DialogueResultPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.option);
        buf.writeBoolean(packet.success);
        buf.writeUtf(packet.line);
    }

    private static DialogueResultPacket decode(FriendlyByteBuf buf) {
        return new DialogueResultPacket(buf.readVarInt(), buf.readUtf(), buf.readBoolean(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueResultPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Object minecraft = Class.forName("net.minecraft.client.Minecraft")
                        .getMethod("getInstance")
                        .invoke(null);
                Object screen = minecraft.getClass().getField("screen").get(minecraft);
                if (screen == null || !screen.getClass().getName().equals("com.javic.slimpatch.client.gui.VillagerDialogueScreen")) {
                    return;
                }
                screen.getClass()
                        .getMethod("showDialogueResult", int.class, String.class, boolean.class, String.class)
                        .invoke(screen, msg.entityId, msg.option, msg.success, msg.line);
            } catch (Exception ignored) {
            }
        });
    }
}