package com.javic.slimpatch.familytree;

import net.minecraft.network.FriendlyByteBuf;

public class FamilyTreeEdgePayload {

    public enum EdgeType {
        PARENT_CHILD,
        SPOUSE,
        FORMER_SPOUSE
    }

    private final String fromNodeId;
    private final String toNodeId;
    private final EdgeType edgeType;

    public FamilyTreeEdgePayload(String fromNodeId, String toNodeId, EdgeType edgeType) {
        this.fromNodeId = fromNodeId == null ? "" : fromNodeId;
        this.toNodeId = toNodeId == null ? "" : toNodeId;
        this.edgeType = edgeType == null ? EdgeType.PARENT_CHILD : edgeType;
    }

    public String getFromNodeId() {
        return this.fromNodeId;
    }

    public String getToNodeId() {
        return this.toNodeId;
    }

    public EdgeType getEdgeType() {
        return this.edgeType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.fromNodeId);
        buf.writeUtf(this.toNodeId);
        buf.writeEnum(this.edgeType);
    }

    public static FamilyTreeEdgePayload decode(FriendlyByteBuf buf) {
        return new FamilyTreeEdgePayload(buf.readUtf(), buf.readUtf(), buf.readEnum(EdgeType.class));
    }
}
