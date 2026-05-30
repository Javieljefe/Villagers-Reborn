package com.javic.slimpatch.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ModNetworking {

    public static final String PROTOCOL = "1";

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);

        registrar.playToClient(
                VillagerCooldownsPacket.TYPE,
                VillagerCooldownsPacket.CODEC,
                VillagerCooldownsPacket::handle
        );

        registrar.playToClient(
                DialogueResultPacket.TYPE,
                DialogueResultPacket.CODEC,
                DialogueResultPacket::handle
        );

        registrar.playToClient(
                SpouseCookingLinePacket.TYPE,
                SpouseCookingLinePacket.CODEC,
                SpouseCookingLinePacket::handle
        );

        registrar.playToClient(
                StartWeddingCutscenePacket.TYPE,
                StartWeddingCutscenePacket.CODEC,
                StartWeddingCutscenePacket::handle
        );

        registrar.playToClient(
                OpenMarriageProposalScreenPacket.TYPE,
                OpenMarriageProposalScreenPacket.CODEC,
                OpenMarriageProposalScreenPacket::handle
        );

        registrar.playToClient(
                OpenDivorceConfirmationScreenPacket.TYPE,
                OpenDivorceConfirmationScreenPacket.CODEC,
                OpenDivorceConfirmationScreenPacket::handle
        );

        registrar.playToClient(
                StartFamilyCutscenePacket.TYPE,
                StartFamilyCutscenePacket.CODEC,
                StartFamilyCutscenePacket::handle
        );

        registrar.playToClient(
                FamilyDialogueLinePacket.TYPE,
                FamilyDialogueLinePacket.CODEC,
                FamilyDialogueLinePacket::handle
        );

        registrar.playToClient(
                FamilyBirthReadyPacket.TYPE,
                FamilyBirthReadyPacket.CODEC,
                FamilyBirthReadyPacket::handle
        );

        registrar.playToClient(
                OpenBirthScreenPacket.TYPE,
                OpenBirthScreenPacket.CODEC,
                OpenBirthScreenPacket::handle
        );

        registrar.playToClient(
                BirthConfirmResultPacket.TYPE,
                BirthConfirmResultPacket.CODEC,
                BirthConfirmResultPacket::handle
        );

        registrar.playToClient(
                FamilyTreeDataPacket.TYPE,
                FamilyTreeDataPacket.CODEC,
                FamilyTreeDataPacket::handle
        );

        registrar.playToServer(
                ConfirmMarriageProposalPacket.TYPE,
                ConfirmMarriageProposalPacket.CODEC,
                ConfirmMarriageProposalPacket::handle
        );

        registrar.playToServer(
                ConfirmDivorcePacket.TYPE,
                ConfirmDivorcePacket.CODEC,
                ConfirmDivorcePacket::handle
        );

        registrar.playToServer(
                RelationshipPacket.TYPE,
                RelationshipPacket.CODEC,
                RelationshipPacket::handle
        );

        registrar.playToServer(
                GiftPacket.TYPE,
                GiftPacket.CODEC,
                GiftPacket::handle
        );

        registrar.playToServer(
                QuestPacket.TYPE,
                QuestPacket.CODEC,
                QuestPacket::handle
        );

        registrar.playToServer(
                VillagerNamePacket.TYPE,
                VillagerNamePacket.CODEC,
                VillagerNamePacket::handle
        );

        registrar.playToServer(
                VillagerEditDataPacket.TYPE,
                VillagerEditDataPacket.CODEC,
                VillagerEditDataPacket::handle
        );

        registrar.playToServer(
                VillagerCustomSkinUploadPacket.TYPE,
                VillagerCustomSkinUploadPacket.CODEC,
                VillagerCustomSkinUploadPacket::handle
        );

        registrar.playToServer(
                VillagerCommandPacket.TYPE,
                VillagerCommandPacket.CODEC,
                VillagerCommandPacket::handle
        );

        registrar.playToServer(
                VillagerEquipmentPacket.TYPE,
                VillagerEquipmentPacket.CODEC,
                VillagerEquipmentPacket::handle
        );

        registrar.playToServer(
                VillagerCombatModePacket.TYPE,
                VillagerCombatModePacket.CODEC,
                VillagerCombatModePacket::handle
        );

        registrar.playToServer(
                VillagerFollowModePacket.TYPE,
                VillagerFollowModePacket.CODEC,
                VillagerFollowModePacket::handle
        );

        registrar.playToServer(
                VillagerArmorVisibilityPacket.TYPE,
                VillagerArmorVisibilityPacket.CODEC,
                VillagerArmorVisibilityPacket::handle
        );

        registrar.playToServer(
                VillagerMutePacket.TYPE,
                VillagerMutePacket.CODEC,
                VillagerMutePacket::handle
        );

        registrar.playToServer(
                DialogueStatePacket.TYPE,
                DialogueStatePacket.CODEC,
                DialogueStatePacket::handle
        );

        registrar.playToServer(
                StartFamilyPacket.TYPE,
                StartFamilyPacket.CODEC,
                StartFamilyPacket::handle
        );

        registrar.playToServer(
                FamilyStatusPacket.TYPE,
                FamilyStatusPacket.CODEC,
                FamilyStatusPacket::handle
        );

        registrar.playToServer(
                RequestBirthScreenPacket.TYPE,
                RequestBirthScreenPacket.CODEC,
                RequestBirthScreenPacket::handle
        );

        registrar.playToServer(
                ConfirmBirthPacket.TYPE,
                ConfirmBirthPacket.CODEC,
                ConfirmBirthPacket::handle
        );

        registrar.playToServer(
                RequestFamilyTreePacket.TYPE,
                RequestFamilyTreePacket.CODEC,
                RequestFamilyTreePacket::handle
        );

        registrar.playToServer(
                CloseFamilyTreePacket.TYPE,
                CloseFamilyTreePacket.CODEC,
                CloseFamilyTreePacket::handle
        );

        registrar.playToClient(
                QuestSyncPacket.TYPE,
                QuestSyncPacket.CODEC,
                QuestSyncPacket::handle
        );

        registrar.playToClient(
                SkinThemeSyncPacket.TYPE,
                SkinThemeSyncPacket.CODEC,
                SkinThemeSyncPacket::handle
        );

        registrar.playToClient(
                RelationshipSyncPacket.TYPE,
                RelationshipSyncPacket.CODEC,
                RelationshipSyncPacket::handle
        );

        registrar.playToClient(
                VillagerCustomSkinSyncPacket.TYPE,
                VillagerCustomSkinSyncPacket.CODEC,
                VillagerCustomSkinSyncPacket::handle
        );

        registrar.playToClient(
                MultiplayerSkinSettingsSyncPacket.TYPE,
                MultiplayerSkinSettingsSyncPacket.CODEC,
                MultiplayerSkinSettingsSyncPacket::handle
        );
    }

    public static void sendToClient(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
