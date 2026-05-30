package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.events.MultiplayerSkinSyncHandler;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import com.javic.slimpatch.util.MultiplayerSkinStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

public class VillagerCustomSkinUploadPacket implements CustomPacketPayload {

    private static final double MAX_EDIT_DISTANCE_SQR = 64.0D;

    private final int entityId;
    private final String savedSkinInput;
    private final byte[] pngData;
    private final boolean clear;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_custom_skin_upload");
    public static final Type<VillagerCustomSkinUploadPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerCustomSkinUploadPacket> CODEC =
            StreamCodec.of(VillagerCustomSkinUploadPacket::encode, VillagerCustomSkinUploadPacket::decode);

    public VillagerCustomSkinUploadPacket(int entityId, String savedSkinInput, byte[] pngData, boolean clear) {
        this.entityId = entityId;
        this.savedSkinInput = savedSkinInput;
        this.pngData = pngData;
        this.clear = clear;
    }

    private static void encode(FriendlyByteBuf buf, VillagerCustomSkinUploadPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.savedSkinInput == null ? "" : packet.savedSkinInput);
        buf.writeBoolean(packet.clear);
        buf.writeByteArray(packet.pngData == null ? new byte[0] : packet.pngData);
    }

    private static VillagerCustomSkinUploadPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        String savedSkinInput = buf.readUtf();
        boolean clear = buf.readBoolean();
        byte[] pngData = buf.readByteArray();
        return new VillagerCustomSkinUploadPacket(entityId, savedSkinInput, pngData, clear);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerCustomSkinUploadPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!MultiplayerSkinStorage.isEnabled()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("slimpatch.message.custom_skin_disabled"), true);
                return;
            }

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof Villager villager) || !(entity instanceof CommandableVillager commandableVillager)) return;
            if (player.distanceToSqr(villager) > MAX_EDIT_DISTANCE_SQR) return;
            if (!VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, player)) return;

            try {
                if (msg.clear) {
                    MultiplayerSkinStorage.deleteSkin(villager.getUUID());
                    applySkinMetadata(villager, "", "");
                    FamilyTreeTracker.onVillagerSkinChanged(level.getServer(), villager);
                    MultiplayerSkinSyncHandler.broadcastSkin(villager, null, "", true);
                    return;
                }

                String validationError = MultiplayerSkinStorage.validatePng(msg.pngData);
                if (validationError != null) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(validationError), true);
                    return;
                }

                MultiplayerSkinStorage.saveSkin(villager.getUUID(), msg.pngData);
                String savedInput = MultiplayerSkinStorage.sanitizeSavedInput(msg.savedSkinInput);
                applySkinMetadata(villager, savedInput, MultiplayerSkinStorage.getStoredSkinPath(villager.getUUID()));
                FamilyTreeTracker.onVillagerSkinChanged(level.getServer(), villager);
                MultiplayerSkinSyncHandler.broadcastSkin(villager, msg.pngData, savedInput, false);
            } catch (IOException e) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("slimpatch.message.custom_skin_store_failed"), true);
            }
        });
    }

    private static void applySkinMetadata(Villager villager, String savedInput, String customSkinPath) {
        if (villager instanceof MaleVillagerEntity maleVillager) {
            maleVillager.setSavedSkinInput(savedInput);
            maleVillager.setCustomSkinPath(customSkinPath);
        } else if (villager instanceof FemaleVillagerEntity femaleVillager) {
            femaleVillager.setSavedSkinInput(savedInput);
            femaleVillager.setCustomSkinPath(customSkinPath);
        } else {
            if (savedInput.isEmpty()) {
                villager.getPersistentData().remove("SavedSkinInput");
            } else {
                villager.getPersistentData().putString("SavedSkinInput", savedInput);
            }
            if (customSkinPath.isEmpty()) {
                villager.getPersistentData().remove("CustomSkinPath");
            } else {
                villager.getPersistentData().putString("CustomSkinPath", customSkinPath);
            }
        }
    }
}
