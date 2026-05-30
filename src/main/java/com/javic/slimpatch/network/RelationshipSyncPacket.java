package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.VillagerRelationshipData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RelationshipSyncPacket implements CustomPacketPayload {

    private final int entityId;
    private final float relationship;
    private final float goldenRelationship;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "relationship_sync");
    public static final Type<RelationshipSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RelationshipSyncPacket> CODEC =
            StreamCodec.of(RelationshipSyncPacket::encode, RelationshipSyncPacket::decode);

    public RelationshipSyncPacket(int entityId, float relationship) {
        this(entityId, relationship, 0.0F);
    }

    public RelationshipSyncPacket(int entityId, float relationship, float goldenRelationship) {
        this.entityId = entityId;
        this.relationship = relationship;
        this.goldenRelationship = goldenRelationship;
    }

    private static void encode(FriendlyByteBuf buf, RelationshipSyncPacket packet) {
        buf.writeVarInt(packet.entityId);
        buf.writeFloat(packet.relationship);
        buf.writeFloat(packet.goldenRelationship);
    }

    private static RelationshipSyncPacket decode(FriendlyByteBuf buf) {
        return new RelationshipSyncPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RelationshipSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Object minecraft = getClientMinecraft();
            if (minecraft == null) return;

            Object level;
            try {
                level = minecraft.getClass().getField("level").get(minecraft);
            } catch (Exception e) {
                return;
            }

            if (level == null) return;

            Entity entity;
            try {
                entity = (Entity) level.getClass().getMethod("getEntity", int.class).invoke(level, msg.entityId);
            } catch (Exception e) {
                return;
            }

            if (entity instanceof Villager villager) {
                VillagerRelationshipData.setClientRelationship(villager, msg.relationship);
                VillagerRelationshipData.setClientGoldenRelationship(villager, msg.goldenRelationship);
            }
        });
    }

    private static Object getClientMinecraft() {
        try {
            return Class.forName("net.minecraft.client.Minecraft")
                    .getMethod("getInstance")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
