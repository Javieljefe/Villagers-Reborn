package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.data.WorldSkinData;
import com.javic.slimpatch.entity.BirthScreenData;
import com.javic.slimpatch.entity.FamilyBirthHandler;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerPersonality;
import com.javic.slimpatch.entity.VillagerRelationshipStage;
import com.javic.slimpatch.util.MultiplayerSkinStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfirmBirthPacket implements CustomPacketPayload {

    private static final double MAX_BIRTH_DISTANCE_SQR = 64.0D;

    private final int spouseEntityId;
    private final String childName;
    private final int selectedSkinId;
    private final String selectedPersonality;
    private final String childGender;
    private final boolean customSkin;
    private final String customSkinInput;
    private final byte[] customSkinPngData;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "confirm_birth");
    public static final Type<ConfirmBirthPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ConfirmBirthPacket> CODEC =
            StreamCodec.of(ConfirmBirthPacket::encode, ConfirmBirthPacket::decode);

    public ConfirmBirthPacket(int spouseEntityId, String childName, int selectedSkinId, String selectedPersonality, String childGender, boolean customSkin, String customSkinInput, byte[] customSkinPngData) {
        this.spouseEntityId = spouseEntityId;
        this.childName = childName;
        this.selectedSkinId = selectedSkinId;
        this.selectedPersonality = selectedPersonality;
        this.childGender = childGender;
        this.customSkin = customSkin;
        this.customSkinInput = customSkinInput;
        this.customSkinPngData = customSkinPngData;
    }

    private static void encode(FriendlyByteBuf buf, ConfirmBirthPacket packet) {
        buf.writeVarInt(packet.spouseEntityId);
        buf.writeUtf(packet.childName);
        buf.writeVarInt(packet.selectedSkinId);
        buf.writeUtf(packet.selectedPersonality);
        buf.writeUtf(packet.childGender);
        buf.writeBoolean(packet.customSkin);
        buf.writeUtf(packet.customSkinInput == null ? "" : packet.customSkinInput);
        buf.writeByteArray(packet.customSkinPngData == null ? new byte[0] : packet.customSkinPngData);
    }

    private static ConfirmBirthPacket decode(FriendlyByteBuf buf) {
        return new ConfirmBirthPacket(buf.readVarInt(), buf.readUtf(), buf.readVarInt(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readUtf(), buf.readByteArray());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfirmBirthPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.spouseEntityId);
            if (!(entity instanceof Villager villager)) return;
            if (!(entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) return;
            if (!(entity instanceof FamilyVillager familyVillager)) return;
            if (player.distanceToSqr(villager) > MAX_BIRTH_DISTANCE_SQR) return;
            if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.MARRIED) return;
            if (!player.getUUID().equals(familyVillager.getSpousePlayerUuid())) return;
            if (!familyVillager.isExpectingChild() || player.level().getGameTime() < familyVillager.getExpectingEndsAt()) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_not_ready", ""), player);
                return;
            }

            String childName = BirthScreenData.sanitizeChildName(msg.childName);
            if (!BirthScreenData.isValidChildName(childName)) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_name", ""), player);
                return;
            }

            VillagerPersonality selectedPersonality;
            try {
                selectedPersonality = VillagerPersonality.valueOf(msg.selectedPersonality);
            } catch (IllegalArgumentException e) {
                selectedPersonality = null;
            }
            if (!BirthScreenData.isValidPersonality(selectedPersonality)) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_personality", ""), player);
                return;
            }

            String childGender = BirthScreenData.normalizeGender(msg.childGender);
            CompoundTag pendingGenders = villager.getPersistentData().getCompound(RequestBirthScreenPacket.BIRTH_SCREEN_PENDING_GENDERS_TAG);
            String storedGender = pendingGenders.getString(player.getUUID().toString());
            if (storedGender.isEmpty()) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_skin", ""), player);
                return;
            }
            String expectedGender = BirthScreenData.normalizeGender(storedGender);
            if (!childGender.equals(expectedGender)) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_skin", ""), player);
                return;
            }
            String skinType = BirthScreenData.normalizeSkinType(WorldSkinData.get(player.serverLevel()).getTheme());
            if (msg.customSkin) {
                if ((player.getServer() == null || player.getServer().isDedicatedServer() || player.getServer().isPublished()) && !MultiplayerSkinStorage.isEnabled()) {
                    ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_skin", ""), player);
                    return;
                }
                if (MultiplayerSkinStorage.validatePng(msg.customSkinPngData) != null) {
                    ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_skin", ""), player);
                    return;
                }
                MultiplayerSkinStorage.sanitizeSavedInput(msg.customSkinInput);
            } else if (!BirthScreenData.isValidSkin(childGender, skinType, msg.selectedSkinId)) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, "slimpatch.message.birth_invalid_skin", ""), player);
                return;
            }

            String failureMessage = FamilyBirthHandler.spawnChild(player, villager, familyVillager, childName, childGender, selectedPersonality, msg.selectedSkinId, msg.customSkin, msg.customSkinInput, msg.customSkinPngData);
            if (failureMessage != null) {
                ModNetworking.sendToClient(new BirthConfirmResultPacket(false, failureMessage, ""), player);
                return;
            }

            ModNetworking.sendToClient(new BirthConfirmResultPacket(true, "slimpatch.message.birth_success", childName), player);
        });
    }
}
