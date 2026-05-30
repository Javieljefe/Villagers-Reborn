package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerRelationshipStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FamilyStatusPacket implements CustomPacketPayload {

    private static final double MAX_FAMILY_DISTANCE_SQR = 64.0D;

    private final int entityId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "family_status");
    public static final Type<FamilyStatusPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, FamilyStatusPacket> CODEC =
            StreamCodec.of(FamilyStatusPacket::encode, FamilyStatusPacket::decode);

    public FamilyStatusPacket(int entityId) {
        this.entityId = entityId;
    }

    private static void encode(FriendlyByteBuf buf, FamilyStatusPacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static FamilyStatusPacket decode(FriendlyByteBuf buf) {
        return new FamilyStatusPacket(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FamilyStatusPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.entityId);
            if (!(entity instanceof Villager villager)) return;
            if (!(entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) return;
            if (!(entity instanceof FamilyVillager familyVillager)) return;
            if (player.distanceToSqr(villager) > MAX_FAMILY_DISTANCE_SQR) return;
            if (!player.getUUID().equals(DialogueManager.getDialoguePlayer(villager))) return;
            if (familyVillager.getRelationshipStage() != VillagerRelationshipStage.MARRIED) return;
            if (!player.getUUID().equals(familyVillager.getSpousePlayerUuid())) {
                player.displayClientMessage(Component.translatable("slimpatch.message.family_not_spouse"), true);
                return;
            }
            if (!familyVillager.isExpectingChild()) return;

            long remainingTicks = familyVillager.getExpectingEndsAt() - villager.level().getGameTime();
            if (remainingTicks <= 0L) {
                ModNetworking.sendToClient(new FamilyBirthReadyPacket(villager.getId(), "slimpatch.dialogue.family.birth_ready"), player);
                return;
            }
            String lineKey;
            if (remainingTicks > 16000L) {
                lineKey = "slimpatch.dialogue.family.status_early";
            } else if (remainingTicks > 8000L) {
                lineKey = "slimpatch.dialogue.family.status_mid";
            } else {
                lineKey = "slimpatch.dialogue.family.status_soon";
            }

            ModNetworking.sendToClient(new FamilyDialogueLinePacket(villager.getId(), lineKey), player);
        });
    }
}
