package com.javic.slimpatch.network;

import com.javic.slimpatch.Config;
import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FamilyCutsceneServerHandler;
import com.javic.slimpatch.entity.FamilyPregnancyHandler;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StartFamilyPacket implements CustomPacketPayload {

    private static final double MAX_FAMILY_DISTANCE_SQR = 64.0D;

    private final int entityId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "start_family");
    public static final Type<StartFamilyPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, StartFamilyPacket> CODEC =
            StreamCodec.of(StartFamilyPacket::encode, StartFamilyPacket::decode);

    public StartFamilyPacket(int entityId) {
        this.entityId = entityId;
    }

    private static void encode(FriendlyByteBuf buf, StartFamilyPacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static StartFamilyPacket decode(FriendlyByteBuf buf) {
        return new StartFamilyPacket(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartFamilyPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Entity entity = player.serverLevel().getEntity(msg.entityId);
            if (!(entity instanceof Villager villager)) return;
            if (!(entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) return;
            if (!(entity instanceof FamilyVillager familyVillager)) return;
            if (player.distanceToSqr(villager) > MAX_FAMILY_DISTANCE_SQR) return;

            FamilyPregnancyHandler.Result result = FamilyPregnancyHandler.startFamily(villager, familyVillager, player);
            if (result.success()) {
                if (Config.ENABLE_FAMILY_CUTSCENE.get()) {
                    FamilyCutsceneServerHandler.start(villager, player);
                }
                ModNetworking.sendToClient(new StartFamilyCutscenePacket(villager.getId(), villager.getName().getString()), player);
            } else {
                player.displayClientMessage(Component.translatable(result.messageKey()), true);
            }
        });
    }
}
