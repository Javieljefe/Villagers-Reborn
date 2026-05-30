package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FamilyBirthReadyPacket implements CustomPacketPayload {

    private final int entityId;
    private final String lineKey;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "family_birth_ready");
    public static final Type<FamilyBirthReadyPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, FamilyBirthReadyPacket> CODEC =
            StreamCodec.of(FamilyBirthReadyPacket::encode, FamilyBirthReadyPacket::decode);

    public FamilyBirthReadyPacket(int entityId, String lineKey) {
        this.entityId = entityId;
        this.lineKey = lineKey;
    }

    private static void encode(FriendlyByteBuf buf, FamilyBirthReadyPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.lineKey);
    }

    private static FamilyBirthReadyPacket decode(FriendlyByteBuf buf) {
        return new FamilyBirthReadyPacket(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FamilyBirthReadyPacket msg, IPayloadContext ctx) {
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
                        .getMethod("showBirthReadyPrompt", int.class, String.class)
                        .invoke(screen, msg.entityId, msg.lineKey);
            } catch (Exception ignored) {
            }
        });
    }
}
