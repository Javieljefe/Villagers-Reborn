package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StartWeddingCutscenePacket implements CustomPacketPayload {

    private final int entityId;
    private final String villagerName;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "start_wedding_cutscene");
    public static final Type<StartWeddingCutscenePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, StartWeddingCutscenePacket> CODEC =
            StreamCodec.of(StartWeddingCutscenePacket::encode, StartWeddingCutscenePacket::decode);

    public StartWeddingCutscenePacket(int entityId, String villagerName) {
        this.entityId = entityId;
        this.villagerName = villagerName;
    }

    private static void encode(FriendlyByteBuf buf, StartWeddingCutscenePacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.villagerName);
    }

    private static StartWeddingCutscenePacket decode(FriendlyByteBuf buf) {
        return new StartWeddingCutscenePacket(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartWeddingCutscenePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!Config.ENABLE_WEDDING_CUTSCENE.get()) {
                showMarriageSuccessFallback(msg.villagerName);
                return;
            }
            try {
                Class<?> controllerClass = Class.forName("com.javic.slimpatch.client.cutscene.WeddingCutsceneController");
                boolean started = (boolean) controllerClass.getMethod("start", int.class, String.class).invoke(null, msg.entityId, msg.villagerName);
                if (!started) {
                    showMarriageSuccessFallback(msg.villagerName);
                }
            } catch (Exception ignored) {
                showMarriageSuccessFallback(msg.villagerName);
            }
        });
    }

    private static void showMarriageSuccessFallback(String villagerName) {
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
                        .invoke(null, "slimpatch.message.marriage_success", new Object[]{villagerName});
                player.getClass().getMethod("displayClientMessage", componentClass, boolean.class).invoke(player, message, true);
            }
        } catch (Exception ignored) {
        }
    }
}
