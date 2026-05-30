package com.javic.slimpatch.network;

import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.network.chat.Component;
import com.javic.slimpatch.SlimPatch;

import java.util.UUID;

public class VillagerNamePacket implements CustomPacketPayload {

    private static final double MAX_NAME_DISTANCE_SQR = 64.0D;

    public static final Type<VillagerNamePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_name_packet"));
    public static final StreamCodec<FriendlyByteBuf, VillagerNamePacket> CODEC = CustomPacketPayload.codec(VillagerNamePacket::encode, VillagerNamePacket::new);

    private final UUID villagerId;
    private final String newName;

    public VillagerNamePacket(UUID villagerId, String newName) {
        this.villagerId = villagerId;
        this.newName = newName;
    }

    public VillagerNamePacket(FriendlyByteBuf buf) {
        this.villagerId = buf.readUUID();
        this.newName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(villagerId);
        buf.writeUtf(newName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerNamePacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
                Entity entity = level.getEntity(msg.villagerId);
                if (entity instanceof Villager villager) {
                    if (player.distanceToSqr(villager) > MAX_NAME_DISTANCE_SQR) {
                        return;
                    }
                    if (entity instanceof CommandableVillager commandableVillager
                            && !VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, player)) {
                        return;
                    }
                    villager.setCustomName(Component.literal(msg.newName));
                    villager.getPersistentData().putString("SavedName", msg.newName);
                    FamilyTreeTracker.onVillagerNameChanged(level.getServer(), villager);
                }
            }
        });
    }
}
