package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenBirthScreenPacket implements CustomPacketPayload {

    private final int spouseEntityId;
    private final String villagerName;
    private final String childGender;
    private final String initialPersonality;
    private final String skinType;
    private final int initialSkinId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "open_birth_screen");
    public static final Type<OpenBirthScreenPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenBirthScreenPacket> CODEC =
            StreamCodec.of(OpenBirthScreenPacket::encode, OpenBirthScreenPacket::decode);

    public OpenBirthScreenPacket(int spouseEntityId, String villagerName, String childGender, String initialPersonality, String skinType, int initialSkinId) {
        this.spouseEntityId = spouseEntityId;
        this.villagerName = villagerName;
        this.childGender = childGender;
        this.initialPersonality = initialPersonality;
        this.skinType = skinType;
        this.initialSkinId = initialSkinId;
    }

    private static void encode(FriendlyByteBuf buf, OpenBirthScreenPacket packet) {
        buf.writeVarInt(packet.spouseEntityId);
        buf.writeUtf(packet.villagerName);
        buf.writeUtf(packet.childGender);
        buf.writeUtf(packet.initialPersonality);
        buf.writeUtf(packet.skinType);
        buf.writeVarInt(packet.initialSkinId);
    }

    private static OpenBirthScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenBirthScreenPacket(buf.readVarInt(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenBirthScreenPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Object minecraft = Class.forName("net.minecraft.client.Minecraft")
                        .getMethod("getInstance")
                        .invoke(null);
                Class<?> screenClass = Class.forName("com.javic.slimpatch.client.gui.BirthChildScreen");
                Object screen = screenClass
                        .getConstructor(int.class, String.class, String.class, String.class, String.class, int.class)
                        .newInstance(msg.spouseEntityId, msg.villagerName, msg.childGender, msg.initialPersonality, msg.skinType, msg.initialSkinId);
                minecraft.getClass()
                        .getMethod("setScreen", Class.forName("net.minecraft.client.gui.screens.Screen"))
                        .invoke(minecraft, screen);
            } catch (Exception ignored) {
            }
        });
    }
}
