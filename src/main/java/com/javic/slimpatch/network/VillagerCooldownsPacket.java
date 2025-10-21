package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerCooldownsPacket implements CustomPacketPayload {

    private final UUID uuid;
    private final int entityId;
    private final Map<String, Integer> cooldowns;

    public VillagerCooldownsPacket(UUID uuid, int entityId, Map<String, Integer> cooldowns) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.cooldowns = cooldowns;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_cooldowns");
    public static final Type<VillagerCooldownsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerCooldownsPacket> CODEC =
            StreamCodec.of(VillagerCooldownsPacket::encode, VillagerCooldownsPacket::decode);

    private static void encode(FriendlyByteBuf buf, VillagerCooldownsPacket packet) {
        buf.writeUUID(packet.uuid);
        buf.writeVarInt(packet.entityId);
        buf.writeVarInt(packet.cooldowns.size());
        for (Map.Entry<String, Integer> entry : packet.cooldowns.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    private static VillagerCooldownsPacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        int entityId = buf.readVarInt();
        int size = buf.readVarInt();
        Map<String, Integer> cooldowns = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String option = buf.readUtf();
            int seconds = buf.readVarInt();
            cooldowns.put(option, seconds);
        }
        return new VillagerCooldownsPacket(uuid, entityId, cooldowns);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerCooldownsPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            SlimPatch.LOGGER.debug("[SlimPatch] Recibidos cooldowns para aldeano UUID={} (ID={}) → {}",
                    msg.uuid, msg.entityId, msg.cooldowns);

            try {
                Class<?> storageClass = Class.forName("com.javic.slimpatch.network.VillagerCooldownsStorage");
                var method = storageClass.getMethod("setCooldowns", UUID.class, Map.class);
                method.invoke(null, msg.uuid, msg.cooldowns);
            } catch (Throwable ignored) {
            }
        });
    }
}