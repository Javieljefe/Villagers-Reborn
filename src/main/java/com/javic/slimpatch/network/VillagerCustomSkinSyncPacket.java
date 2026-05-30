package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class VillagerCustomSkinSyncPacket implements CustomPacketPayload {

    private final int entityId;
    private final UUID villagerUuid;
    private final String savedSkinInput;
    private final byte[] pngData;
    private final boolean clear;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_custom_skin_sync");
    public static final Type<VillagerCustomSkinSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerCustomSkinSyncPacket> CODEC =
            StreamCodec.of(VillagerCustomSkinSyncPacket::encode, VillagerCustomSkinSyncPacket::decode);

    public VillagerCustomSkinSyncPacket(int entityId, UUID villagerUuid, String savedSkinInput, byte[] pngData, boolean clear) {
        this.entityId = entityId;
        this.villagerUuid = villagerUuid;
        this.savedSkinInput = savedSkinInput;
        this.pngData = pngData;
        this.clear = clear;
    }

    private static void encode(FriendlyByteBuf buf, VillagerCustomSkinSyncPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUUID(packet.villagerUuid);
        buf.writeUtf(packet.savedSkinInput == null ? "" : packet.savedSkinInput);
        buf.writeBoolean(packet.clear);
        buf.writeByteArray(packet.pngData == null ? new byte[0] : packet.pngData);
    }

    private static VillagerCustomSkinSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        java.util.UUID villagerUuid = buf.readUUID();
        String savedSkinInput = buf.readUtf();
        boolean clear = buf.readBoolean();
        byte[] pngData = buf.readByteArray();
        return new VillagerCustomSkinSyncPacket(entityId, villagerUuid, savedSkinInput, pngData, clear);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerCustomSkinSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> cacheClass = Class.forName("com.javic.slimpatch.client.ClientMultiplayerSkinCache");
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
                Object level = minecraftClass.getField("level").get(minecraft);
                if (level == null) {
                    return;
                }

                Object entity = level.getClass().getMethod("getEntity", int.class).invoke(level, msg.entityId);
                if (entity == null) {
                    return;
                }

                String cachedPath = "";
                if (msg.clear) {
                    cacheClass.getMethod("clearSkin", UUID.class).invoke(null, msg.villagerUuid);
                } else {
                    cachedPath = (String) cacheClass.getMethod("storeSkin", UUID.class, byte[].class).invoke(null, msg.villagerUuid, msg.pngData);
                }

                if (entity instanceof com.javic.slimpatch.entity.MaleVillagerEntity maleVillager) {
                    maleVillager.setSavedSkinInput(msg.savedSkinInput);
                    maleVillager.setCustomSkinPath(cachedPath);
                } else if (entity instanceof com.javic.slimpatch.entity.FemaleVillagerEntity femaleVillager) {
                    femaleVillager.setSavedSkinInput(msg.savedSkinInput);
                    femaleVillager.setCustomSkinPath(cachedPath);
                } else if (entity instanceof net.minecraft.world.entity.npc.Villager villager) {
                    if (msg.savedSkinInput.isEmpty()) {
                        villager.getPersistentData().remove("SavedSkinInput");
                    } else {
                        villager.getPersistentData().putString("SavedSkinInput", msg.savedSkinInput);
                    }
                    if (cachedPath.isEmpty()) {
                        villager.getPersistentData().remove("CustomSkinPath");
                    } else {
                        villager.getPersistentData().putString("CustomSkinPath", cachedPath);
                    }
                }
            } catch (Exception e) {
            }
        });
    }
}
