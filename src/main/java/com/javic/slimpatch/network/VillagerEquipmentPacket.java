package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.entity.VillagerEquipmentHolder;
import com.javic.slimpatch.menu.VillagerEquipmentMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VillagerEquipmentPacket implements CustomPacketPayload {

    private static final double MAX_OPEN_DISTANCE_SQR = 64.0D;

    private final int entityId;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_equipment");
    public static final Type<VillagerEquipmentPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VillagerEquipmentPacket> CODEC =
            StreamCodec.of(VillagerEquipmentPacket::encode, VillagerEquipmentPacket::decode);

    public VillagerEquipmentPacket(int entityId) {
        this.entityId = entityId;
    }

    private static void encode(FriendlyByteBuf buf, VillagerEquipmentPacket packet) {
        buf.writeVarInt(packet.entityId);
    }

    private static VillagerEquipmentPacket decode(FriendlyByteBuf buf) {
        return new VillagerEquipmentPacket(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerEquipmentPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof Villager villager) || !(entity instanceof VillagerEquipmentHolder equipmentHolder) || !(entity instanceof LivingEntity livingEntity)) return;
            if (player.distanceToSqr(villager) > MAX_OPEN_DISTANCE_SQR) return;
            if (entity instanceof CommandableVillager commandableVillager && !VillagerCommandHandler.canUseProtectedAction(villager, commandableVillager, player)) return;

            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, serverPlayer) -> new VillagerEquipmentMenu(containerId, playerInventory, livingEntity, equipmentHolder.getEquipmentInventory()),
                    villager.getDisplayName()
            ));
        });
    }
}
