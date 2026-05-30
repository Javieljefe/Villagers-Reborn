package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerCooldownData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RelationshipPacket implements CustomPacketPayload {

    private static final double MAX_RELATIONSHIP_DISTANCE_SQR = 64.0D;

    private final int entityId;
    private final String option;

    public RelationshipPacket(int entityId, String option) {
        this.entityId = entityId;
        this.option = option;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "relationship");
    public static final Type<RelationshipPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RelationshipPacket> CODEC =
            StreamCodec.of(RelationshipPacket::encode, RelationshipPacket::decode);

    private static void encode(FriendlyByteBuf buf, RelationshipPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeUtf(packet.option);
    }

    private static RelationshipPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        String option = buf.readUtf();
        return new RelationshipPacket(entityId, option);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RelationshipPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof net.minecraft.world.entity.npc.Villager villager)) return;
            if (player.distanceToSqr(villager) > MAX_RELATIONSHIP_DISTANCE_SQR) return;
            if (!player.getUUID().equals(DialogueManager.getDialoguePlayer(villager))) return;
            if (VillagerCooldownData.isDialogueOptionOnCooldown(villager, player.getUUID(), msg.option)) return;
            if (msg.option.equalsIgnoreCase("Flirt") && !com.javic.slimpatch.entity.VillagerFamilyData.canUseRomanticInteraction(villager, player)) return;

            boolean success;

            if (entity instanceof MaleVillagerEntity male) {
                success = DialogueManager.calculateSuccess(male.getPersonality(), msg.option);
                male.applyRelationshipChange(player, msg.option, success);
            } else if (entity instanceof FemaleVillagerEntity female) {
                success = DialogueManager.calculateSuccess(female.getPersonality(), msg.option);
                female.applyRelationshipChange(player, msg.option, success);
            } else {
                return;
            }

            String line = DialogueManager.getRandomLine(msg.option, villager, player, success);
            ModNetworking.sendToClient(new DialogueResultPacket(villager.getId(), msg.option, success, line), player);
        });
    }
}
