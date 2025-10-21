package com.javic.slimpatch.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
    }
}