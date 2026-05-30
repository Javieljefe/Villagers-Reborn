package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.data.WorldSkinData;
import com.javic.slimpatch.entity.BabyGenderCharmHandler;
import com.javic.slimpatch.entity.BirthScreenData;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerRelationshipStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestBirthScreenPacket implements CustomPacketPayload {

    private static final double MAX_BIRTH_DISTANCE_SQR = 64.0D;
    public static final String BIRTH_SCREEN_PENDING_GENDERS_TAG = "SlimPatchBirthScreenPendingGenders";

    private final int entityId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "request_birth_screen");
    public static final Type<RequestBirthScreenPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RequestBirthScreenPacket> CODEC =
            StreamCodec.of(RequestBirthScreenPacket::encode, RequestBirthScreenPacket::decode);

    public RequestBirthScreenPacket(int entityId) {
        this.entityId = entityId;
    }

    private static void encode(FriendlyByteBuf buf, RequestBirthScreenPacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static RequestBirthScreenPacket decode(FriendlyByteBuf buf) {
        return new RequestBirthScreenPacket(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestBirthScreenPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.entityId);
            if (!(entity instanceof Villager villager)) return;
            if (!(entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) return;
            if (!(entity instanceof FamilyVillager familyVillager)) return;
            if (player.distanceToSqr(villager) > MAX_BIRTH_DISTANCE_SQR) return;
            if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.MARRIED) return;
            if (!player.getUUID().equals(familyVillager.getSpousePlayerUuid())) return;
            if (!familyVillager.isExpectingChild() || player.level().getGameTime() < familyVillager.getExpectingEndsAt()) {
                player.displayClientMessage(Component.translatable("slimpatch.message.birth_not_ready"), true);
                return;
            }

            ServerLevel serverLevel = player.serverLevel();
            long genderRoll = serverLevel.random.nextLong()
                    ^ player.getUUID().getMostSignificantBits()
                    ^ player.getUUID().getLeastSignificantBits()
                    ^ villager.getUUID().getMostSignificantBits()
                    ^ villager.getUUID().getLeastSignificantBits()
                    ^ serverLevel.getGameTime();
            String childGender = BabyGenderCharmHandler.resolveChildGender(familyVillager, (genderRoll & 1L) == 0L ? "male" : "female");
            String skinType = BirthScreenData.normalizeSkinType(WorldSkinData.get(serverLevel).getTheme());
            int initialSkinId = BirthScreenData.getRandomSkinId(childGender, skinType, serverLevel.random);
            String initialPersonality = BirthScreenData.getRandomPersonality(serverLevel.random).name();
            CompoundTag pendingGenders = villager.getPersistentData().getCompound(BIRTH_SCREEN_PENDING_GENDERS_TAG);
            pendingGenders.putString(player.getUUID().toString(), childGender);
            villager.getPersistentData().put(BIRTH_SCREEN_PENDING_GENDERS_TAG, pendingGenders);
            ModNetworking.sendToClient(new OpenBirthScreenPacket(villager.getId(), villager.getName().getString(), childGender, initialPersonality, skinType, initialSkinId), player);
        });
    }
}
