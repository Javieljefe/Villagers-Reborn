package com.javic.slimpatch.network;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerFamilyData;
import com.javic.slimpatch.quests.Quest;
import com.javic.slimpatch.quests.QuestManager;
import com.javic.slimpatch.quests.data.PlayerQuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class QuestPacket implements CustomPacketPayload {

    private final String questId;
    private final String action;

    public QuestPacket(String questId, String action) {
        this.questId = questId;
        this.action = action;
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "quest_action");
    public static final Type<QuestPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, QuestPacket> CODEC =
            StreamCodec.of(QuestPacket::encode, QuestPacket::decode);

    private static void encode(FriendlyByteBuf buf, QuestPacket packet) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.action);
    }

    private static QuestPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String action = buf.readUtf();
        return new QuestPacket(questId, action);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QuestPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                if (player.getServer() != null && (player.getServer().isDedicatedServer() || player.getServer().isPublished())) {
                    return;
                }
                Quest quest = com.javic.slimpatch.quests.QuestRegistry.getById(msg.questId);
                if (quest == null) return;

                Villager villager = com.javic.slimpatch.quests.QuestManager.getVillagerForQuest(quest, player.serverLevel());
                if (villager == null) return;

                switch (msg.action.toLowerCase()) {
                    case "accept" -> {
                        QuestManager.startQuest(player, quest);
                        villager.getPersistentData().putBoolean("HasQuest", true);
                        villager.getPersistentData().putString("QuestId", quest.getId());

                        PacketDistributor.sendToPlayer(player,
                                new QuestSyncPacket(villager.getId(), quest.getId(), true));

                        if (Config.CUSTOM_VILLAGER_SOUNDS.get() && villager instanceof MaleVillagerEntity)
                            villager.level().playSound(null, villager.blockPosition(),
                                    com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((MaleVillagerEntity) villager));
                        else if (Config.CUSTOM_VILLAGER_SOUNDS.get() && villager instanceof FemaleVillagerEntity)
                            villager.level().playSound(null, villager.blockPosition(),
                                    com.javic.slimpatch.sounds.HumanVillagerSounds.femaleReactionPositive(),
                                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((FemaleVillagerEntity) villager));
                    }

                    case "decline" -> {
                        villager.getPersistentData().putBoolean("HasQuest", false);
                        villager.getPersistentData().remove("QuestId");

                        PacketDistributor.sendToPlayer(player,
                                new QuestSyncPacket(villager.getId(), "", false));

                        villager.level().playSound(null, villager.blockPosition(),
                                net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }

                    case "cancel" -> {
                        PlayerQuestData data = PlayerQuestData.get(player);
                        data.getActiveQuests().remove(quest.getId());
                        villager.getPersistentData().putBoolean("HasQuest", false);
                        villager.getPersistentData().remove("QuestId");

                        PacketDistributor.sendToPlayer(player,
                                new QuestSyncPacket(villager.getId(), "", false));

                        villager.level().playSound(null, villager.blockPosition(),
                                net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }

                    case "complete" -> {
                        if (quest.checkCompletion(player)) {
                            QuestManager.completeQuest(player, quest, player.serverLevel());
                            villager.getPersistentData().putBoolean("HasQuest", false);
                            villager.getPersistentData().remove("QuestId");

                            PacketDistributor.sendToPlayer(player,
                                    new QuestSyncPacket(villager.getId(), quest.getId(), false));

                            if (Config.CUSTOM_VILLAGER_SOUNDS.get() && villager instanceof MaleVillagerEntity)
                                villager.level().playSound(null, villager.blockPosition(),
                                        com.javic.slimpatch.sounds.HumanVillagerSounds.maleReactionPositive(),
                                        net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((MaleVillagerEntity) villager));
                            else if (Config.CUSTOM_VILLAGER_SOUNDS.get() && villager instanceof FemaleVillagerEntity)
                                villager.level().playSound(null, villager.blockPosition(),
                                        com.javic.slimpatch.sounds.HumanVillagerSounds.femaleReactionPositive(),
                                        net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, VillagerFamilyData.getAgeStagePitch((FemaleVillagerEntity) villager));
                        }
                    }
                }
            }
        });
    }
}
