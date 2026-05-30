package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FamilyDialogueLinePacket implements CustomPacketPayload {

    private final int entityId;
    private final String lineKey;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "family_dialogue_line");
    public static final Type<FamilyDialogueLinePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, FamilyDialogueLinePacket> CODEC =
            StreamCodec.of(FamilyDialogueLinePacket::encode, FamilyDialogueLinePacket::decode);

    public FamilyDialogueLinePacket(int entityId, String lineKey) {
        this.entityId = entityId;
        this.lineKey = lineKey;
    }

    private static void encode(FriendlyByteBuf buf, FamilyDialogueLinePacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.lineKey);
    }

    private static FamilyDialogueLinePacket decode(FriendlyByteBuf buf) {
        return new FamilyDialogueLinePacket(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FamilyDialogueLinePacket msg, IPayloadContext ctx) {
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
                        .getMethod("showFamilyLine", int.class, String.class)
                        .invoke(screen, msg.entityId, msg.lineKey);
            } catch (Exception ignored) {
            }
        });
    }
}
