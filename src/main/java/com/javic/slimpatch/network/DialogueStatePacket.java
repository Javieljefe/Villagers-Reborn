package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.entity.VillagerCooldownData;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.SpouseCookingHandler;
import com.javic.slimpatch.entity.VillagerRelationshipData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DialogueStatePacket implements CustomPacketPayload {

    private static final double MAX_DIALOGUE_DISTANCE_SQR = 64.0D;

    private final int entityId;
    private final boolean active;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "dialogue_state");
    public static final Type<DialogueStatePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DialogueStatePacket> CODEC =
            StreamCodec.of(DialogueStatePacket::encode, DialogueStatePacket::decode);

    public DialogueStatePacket(int entityId, boolean active) {
        this.entityId = entityId;
        this.active = active;
    }

    private static void encode(FriendlyByteBuf buf, DialogueStatePacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeBoolean(packet.active);
    }

    private static DialogueStatePacket decode(FriendlyByteBuf buf) {
        return new DialogueStatePacket(buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueStatePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.entityId);
            if (!(entity instanceof Villager villager)) return;

            if (msg.active) {
                if (player.distanceToSqr(villager) > MAX_DIALOGUE_DISTANCE_SQR) return;
                DialogueManager.startDialogue(villager, player);
                if (villager instanceof MaleVillagerEntity male) {
                    VillagerCooldownData.syncDialogueCooldownsToPlayer(male, player, male.getOptionCooldowns());
                } else if (villager instanceof FemaleVillagerEntity female) {
                    VillagerCooldownData.syncDialogueCooldownsToPlayer(female, player, female.getOptionCooldowns());
                }
                float relationship = villager instanceof MaleVillagerEntity male
                        ? VillagerRelationshipData.getRelationshipForPlayer(male, player.getUUID(), male.getRelationship())
                        : villager instanceof FemaleVillagerEntity female
                        ? VillagerRelationshipData.getRelationshipForPlayer(female, player.getUUID(), female.getRelationship())
                        : 0.5f;
                float goldenRelationship = villager instanceof MaleVillagerEntity male
                        ? VillagerRelationshipData.getGoldenRelationshipForPlayer(male, player.getUUID(), male.getGoldenRelationship())
                        : villager instanceof FemaleVillagerEntity female
                        ? VillagerRelationshipData.getGoldenRelationshipForPlayer(female, player.getUUID(), female.getGoldenRelationship())
                        : 0.0f;
                ModNetworking.sendToClient(new RelationshipSyncPacket(villager.getId(), relationship, goldenRelationship), player);
                String introLine = DialogueManager.getRandomLine("Intro", villager, player, true);
                ModNetworking.sendToClient(new DialogueResultPacket(villager.getId(), "Intro", true, introLine), player);				
                if (villager instanceof FamilyVillager familyVillager) {
                    String lineKey = SpouseCookingHandler.tryServeMeal(villager, familyVillager, player);
                    if (lineKey != null) {
                        ModNetworking.sendToClient(new SpouseCookingLinePacket(villager.getId(), lineKey), player);
                    }
                }
            } else {
                DialogueManager.endDialogue(villager, player);
            }
        });
    }
}
