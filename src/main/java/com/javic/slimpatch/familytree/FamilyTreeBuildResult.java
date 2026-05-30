package com.javic.slimpatch.familytree;

import java.util.List;
import java.util.UUID;

public class FamilyTreeBuildResult {

    private final UUID rootVillagerUuid;
    private final List<FamilyTreeNodePayload> nodes;
    private final List<FamilyTreeEdgePayload> edges;
    private final boolean truncated;

    public FamilyTreeBuildResult(UUID rootVillagerUuid, List<FamilyTreeNodePayload> nodes, List<FamilyTreeEdgePayload> edges, boolean truncated) {
        this.rootVillagerUuid = rootVillagerUuid;
        this.nodes = nodes;
        this.edges = edges;
        this.truncated = truncated;
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
}
