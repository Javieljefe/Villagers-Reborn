package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.familytree.FamilyTreeBuildResult;
import com.javic.slimpatch.familytree.FamilyTreeEdgePayload;
import com.javic.slimpatch.familytree.FamilyTreeNodePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FamilyTreeDataPacket implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "family_tree_data");
    public static final Type<FamilyTreeDataPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, FamilyTreeDataPacket> CODEC = StreamCodec.of(FamilyTreeDataPacket::encode, FamilyTreeDataPacket::decode);

    private final UUID rootVillagerUuid;
    private final List<FamilyTreeNodePayload> nodes;
    private final List<FamilyTreeEdgePayload> edges;
    private final boolean truncated;

    public FamilyTreeDataPacket(UUID rootVillagerUuid, List<FamilyTreeNodePayload> nodes, List<FamilyTreeEdgePayload> edges, boolean truncated) {
        this.rootVillagerUuid = rootVillagerUuid;
        this.nodes = nodes;
        this.edges = edges;
        this.truncated = truncated;
    }

    public FamilyTreeDataPacket(FamilyTreeBuildResult result) {
        this(result.getRootVillagerUuid(), result.getNodes(), result.getEdges(), result.isTruncated());
    }

    public UUID getRootVillagerUuid() {
        return this.rootVillagerUuid;
    }

    public List<FamilyTreeNodePayload> getNodes() {
        return this.nodes;
    }

    public List<FamilyTreeEdgePayload> getEdges() {
        return this.edges;
    }

    public boolean isTruncated() {
        return this.truncated;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, FamilyTreeDataPacket packet) {
        buf.writeUUID(packet.rootVillagerUuid);
        buf.writeVarInt(packet.nodes.size());
        for (FamilyTreeNodePayload node : packet.nodes) {
            node.encode(buf);
        }
        buf.writeVarInt(packet.edges.size());
        for (FamilyTreeEdgePayload edge : packet.edges) {
            edge.encode(buf);
        }
        buf.writeBoolean(packet.truncated);
    }

    private static FamilyTreeDataPacket decode(FriendlyByteBuf buf) {
        UUID rootVillagerUuid = buf.readUUID();
        int nodeCount = buf.readVarInt();
        List<FamilyTreeNodePayload> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(FamilyTreeNodePayload.decode(buf));
        }
        int edgeCount = buf.readVarInt();
        List<FamilyTreeEdgePayload> edges = new ArrayList<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            edges.add(FamilyTreeEdgePayload.decode(buf));
        }
        return new FamilyTreeDataPacket(rootVillagerUuid, nodes, edges, buf.readBoolean());
    }

    public static void handle(FamilyTreeDataPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Object minecraft = Class.forName("net.minecraft.client.Minecraft")
                        .getMethod("getInstance")
                        .invoke(null);
                Class<?> screenClass = Class.forName("com.javic.slimpatch.client.gui.familytree.FamilyTreeScreen");
                Object screen = screenClass
                        .getConstructor(java.util.UUID.class, java.util.List.class, java.util.List.class, boolean.class)
                        .newInstance(msg.getRootVillagerUuid(), msg.getNodes(), msg.getEdges(), msg.isTruncated());
                minecraft.getClass()
                        .getMethod("setScreen", Class.forName("net.minecraft.client.gui.screens.Screen"))
                        .invoke(minecraft, screen);
            } catch (Exception ignored) {
            }
        });
    }
}
