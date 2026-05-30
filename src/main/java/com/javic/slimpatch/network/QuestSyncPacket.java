package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class QuestSyncPacket implements CustomPacketPayload {

    private final int villagerId;
    private final String questId;
    private final boolean hasQuest;

    public QuestSyncPacket(int villagerId, String questId, boolean hasQuest) {
        this.villagerId = villagerId;
        this.questId = questId;
        this.hasQuest = hasQuest;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "quest_sync");
    public static final Type<QuestSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, QuestSyncPacket> CODEC =
            StreamCodec.of(QuestSyncPacket::encode, QuestSyncPacket::decode);

    private static void encode(FriendlyByteBuf buf, QuestSyncPacket packet) {
        buf.writeVarInt(packet.villagerId);
        buf.writeUtf(packet.questId);
        buf.writeBoolean(packet.hasQuest);
    }

    private static QuestSyncPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        String quest = buf.readUtf();
        boolean hasQuest = buf.readBoolean();
        return new QuestSyncPacket(id, quest, hasQuest);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QuestSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Object minecraft = getClientMinecraft();
            if (minecraft == null) return;

            Object level;
            try {
                level = minecraft.getClass().getField("level").get(minecraft);
            } catch (Exception e) {
                return;
            }

            if (level == null) return;

            Entity entity;
            try {
                entity = (Entity) level.getClass().getMethod("getEntity", int.class).invoke(level, msg.villagerId);
            } catch (Exception e) {
                return;
            }

            if (!(entity instanceof Villager villager)) return;

            villager.getPersistentData().putBoolean("HasQuest", msg.hasQuest);
            if (msg.hasQuest) {
                villager.getPersistentData().putString("QuestId", msg.questId);
            } else {
                villager.getPersistentData().remove("QuestId");
            }

            try {
                Object player = minecraft.getClass().getField("player").get(minecraft);
                if (player != null) {
                    String text = "[Sync] " + msg.questId + " (HasQuest=" + msg.hasQuest + ")";
                    player.getClass()
                            .getMethod("displayClientMessage", Component.class, boolean.class)
                            .invoke(player, Component.literal(text), true);
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static Object getClientMinecraft() {
        try {
            return Class.forName("net.minecraft.client.Minecraft")
                    .getMethod("getInstance")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
