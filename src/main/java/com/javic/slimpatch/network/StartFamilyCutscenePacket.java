package com.javic.slimpatch.network;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StartFamilyCutscenePacket implements CustomPacketPayload {

    private final int entityId;
    private final String villagerName;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "start_family_cutscene");
    public static final Type<StartFamilyCutscenePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, StartFamilyCutscenePacket> CODEC =
            StreamCodec.of(StartFamilyCutscenePacket::encode, StartFamilyCutscenePacket::decode);

    public StartFamilyCutscenePacket(int entityId, String villagerName) {
        this.entityId = entityId;
        this.villagerName = villagerName;
    }

    private static void encode(FriendlyByteBuf buf, StartFamilyCutscenePacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.villagerName);
    }

    private static StartFamilyCutscenePacket decode(FriendlyByteBuf buf) {
        return new StartFamilyCutscenePacket(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartFamilyCutscenePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!Config.ENABLE_FAMILY_CUTSCENE.get()) {
                showFamilySuccessFallback(msg.villagerName);
                return;
            }
            try {
                Class<?> controllerClass = Class.forName("com.javic.slimpatch.client.cutscene.FamilyCutsceneController");
                boolean started = (boolean) controllerClass.getMethod("start", int.class, String.class).invoke(null, msg.entityId, msg.villagerName);
                if (!started) {
                    showFamilySuccessFallback(msg.villagerName);
                }
            } catch (Exception ignored) {
                showFamilySuccessFallback(msg.villagerName);
            }
        });
    }

    private static void showFamilySuccessFallback(String villagerName) {
        try {
            Class.forName("com.javic.slimpatch.client.cutscene.WeddingMusicDuckingHandler")
                    .getMethod("restore")
                    .invoke(null);
            Object minecraft = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
            Object player = minecraft.getClass().getField("player").get(minecraft);
            if (player != null) {
                Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
                Object message = componentClass
                        .getMethod("translatable", String.class, Object[].class)
                        .invoke(null, "slimpatch.message.family_started", new Object[]{villagerName});
                player.getClass().getMethod("displayClientMessage", componentClass, boolean.class).invoke(player, message, false);
            }
        } catch (Exception ignored) {
        }
    }
}
