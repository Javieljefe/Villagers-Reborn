package com.javic.slimpatch.familytree;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FamilyTreeGraphBuilder {

    public static final int MAX_NODES = 128;
    public static final int MAX_DEPTH = 8;
    public static final int MAX_EDGES = 192;

    private FamilyTreeGraphBuilder() {
    }

    public static FamilyTreeBuildResult build(MinecraftServer server, UUID rootVillagerUuid) {
        if (server == null || rootVillagerUuid == null) {
            return new FamilyTreeBuildResult(rootVillagerUuid, List.of(), List.of(), false);
        }
        FamilyTreeSavedData data = FamilyTreeSavedData.get(server);
        FamilyTreeVillagerSnapshot root = data.getVillager(rootVillagerUuid);
        if (root == null) {
            return new FamilyTreeBuildResult(rootVillagerUuid, List.of(), List.of(), false);
        }

        Map<String, FamilyTreeNodePayload> nodes = new LinkedHashMap<>();
        List<FamilyTreeEdgePayload> edges = new ArrayList<>();
        Set<String> queuedVillagers = new HashSet<>();
        Set<String> processedVillagers = new HashSet<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        Deque<QueuedVillager> queue = new ArrayDeque<>();
        boolean truncated = false;

        String rootNodeId = villagerNodeId(rootVillagerUuid);
        queue.addLast(new QueuedVillager(rootVillagerUuid, 0));
        queuedVillagers.add(rootNodeId);

        while (!queue.isEmpty()) {
            QueuedVillager current = queue.removeFirst();
            String currentNodeId = villagerNodeId(current.villagerUuid());
            if (!processedVillagers.add(currentNodeId)) {
                continue;
            }
            FamilyTreeVillagerSnapshot snapshot = data.getVillager(current.villagerUuid());
            if (snapshot == null) {
                continue;
            }
            addVillagerNode(nodes, snapshot, false);
            if (nodes.size() >= MAX_NODES) {
                truncated = true;
                break;
            }
            addPlayerRelation(nodes, edges, edgeKeys, snapshot.getSpousePlayerUuid(), snapshot.getSpousePlayerName(), currentNodeId, FamilyTreeEdgePayload.EdgeType.SPOUSE, data);
            if (edges.size() >= MAX_EDGES) {
                truncated = true;
                break;
            }
            addVillagerRelation(nodes, edges, edgeKeys, snapshot.getSpouseVillagerUuid(), snapshot.getSpouseVillagerName(), currentNodeId, FamilyTreeEdgePayload.EdgeType.SPOUSE, data, current.depth(), queue, queuedVillagers);
            if (edges.size() >= MAX_EDGES || nodes.size() >= MAX_NODES) {
                truncated = true;
                break;
            }
            if (hasChildrenWithFormerSpouse(snapshot, data)) {
                addPlayerRelation(nodes, edges, edgeKeys, snapshot.getFormerSpousePlayerUuid(), snapshot.getFormerSpousePlayerName(), currentNodeId, FamilyTreeEdgePayload.EdgeType.FORMER_SPOUSE, data);
                if (edges.size() >= MAX_EDGES) {
                    truncated = true;
                    break;
                }
            }
            addPlayerRelation(nodes, edges, edgeKeys, snapshot.getParentPlayerUuid(), snapshot.getParentPlayerName(), currentNodeId, FamilyTreeEdgePayload.EdgeType.PARENT_CHILD, data);
            if (edges.size() >= MAX_EDGES) {
                truncated = true;
                break;
            }
            if (current.depth() >= MAX_DEPTH) {
                continue;
            }

            if (snapshot.getParentVillagerUuid() != null && !snapshot.getParentVillagerUuid().equals(snapshot.getVillagerUuid())) {
                FamilyTreeVillagerSnapshot parentSnapshot = data.getVillager(snapshot.getParentVillagerUuid());
                if (parentSnapshot != null) {
                    addVillagerNode(nodes, parentSnapshot, false);
                } else {
                    addVillagerPlaceholder(nodes, snapshot.getParentVillagerUuid(), snapshot.getParentVillagerName());
                }
                addEdge(edges, edgeKeys, villagerNodeId(snapshot.getParentVillagerUuid()), currentNodeId, FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                if (nodes.size() >= MAX_NODES || edges.size() >= MAX_EDGES) {
                    truncated = true;
                    break;
                }
                if (parentSnapshot != null && queuedVillagers.add(villagerNodeId(parentSnapshot.getVillagerUuid()))) {
                    queue.addLast(new QueuedVillager(parentSnapshot.getVillagerUuid(), current.depth() + 1));
                }
                if (parentSnapshot != null) {
                    for (UUID siblingUuid : parentSnapshot.getChildVillagerUuids()) {
                        if (siblingUuid == null || siblingUuid.equals(snapshot.getVillagerUuid())) {
                            continue;
                        }
                        FamilyTreeVillagerSnapshot siblingSnapshot = data.getVillager(siblingUuid);
                        if (siblingSnapshot != null) {
                            addVillagerNode(nodes, siblingSnapshot, false);
                            addEdge(edges, edgeKeys, villagerNodeId(parentSnapshot.getVillagerUuid()), villagerNodeId(siblingUuid), FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                            if (queuedVillagers.add(villagerNodeId(siblingUuid))) {
                                queue.addLast(new QueuedVillager(siblingUuid, current.depth() + 1));
                            }
                        } else {
                            addVillagerPlaceholder(nodes, siblingUuid, "");
                            addEdge(edges, edgeKeys, villagerNodeId(parentSnapshot.getVillagerUuid()), villagerNodeId(siblingUuid), FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                        }
                        if (nodes.size() >= MAX_NODES || edges.size() >= MAX_EDGES) {
                            truncated = true;
                            break;
                        }
                    }
                    if (truncated) {
                        break;
                    }
                }
            }

            if (snapshot.getParentVillager2Uuid() != null && !snapshot.getParentVillager2Uuid().equals(snapshot.getVillagerUuid())) {
                FamilyTreeVillagerSnapshot parentSnapshot = data.getVillager(snapshot.getParentVillager2Uuid());
                if (parentSnapshot != null) {
                    addVillagerNode(nodes, parentSnapshot, false);
                } else {
                    addVillagerPlaceholder(nodes, snapshot.getParentVillager2Uuid(), snapshot.getParentVillager2Name());
                }
                addEdge(edges, edgeKeys, villagerNodeId(snapshot.getParentVillager2Uuid()), currentNodeId, FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                if (nodes.size() >= MAX_NODES || edges.size() >= MAX_EDGES) {
                    truncated = true;
                    break;
                }
                if (parentSnapshot != null && queuedVillagers.add(villagerNodeId(parentSnapshot.getVillagerUuid()))) {
                    queue.addLast(new QueuedVillager(parentSnapshot.getVillagerUuid(), current.depth() + 1));
                }
                if (parentSnapshot != null) {
                    for (UUID siblingUuid : parentSnapshot.getChildVillagerUuids()) {
                        if (siblingUuid == null || siblingUuid.equals(snapshot.getVillagerUuid())) {
                            continue;
                        }
                        FamilyTreeVillagerSnapshot siblingSnapshot = data.getVillager(siblingUuid);
                        if (siblingSnapshot != null) {
                            addVillagerNode(nodes, siblingSnapshot, false);
                            addEdge(edges, edgeKeys, villagerNodeId(parentSnapshot.getVillagerUuid()), villagerNodeId(siblingUuid), FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                            if (queuedVillagers.add(villagerNodeId(siblingUuid))) {
                                queue.addLast(new QueuedVillager(siblingUuid, current.depth() + 1));
                            }
                        } else {
                            addVillagerPlaceholder(nodes, siblingUuid, "");
                            addEdge(edges, edgeKeys, villagerNodeId(parentSnapshot.getVillagerUuid()), villagerNodeId(siblingUuid), FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                        }
                        if (nodes.size() >= MAX_NODES || edges.size() >= MAX_EDGES) {
                            truncated = true;
                            break;
                        }
                    }
                    if (truncated) {
                        break;
                    }
                }
            }

            for (UUID childUuid : snapshot.getChildVillagerUuids()) {
                if (childUuid == null || childUuid.equals(snapshot.getVillagerUuid())) {
                    continue;
                }
                FamilyTreeVillagerSnapshot childSnapshot = data.getVillager(childUuid);
                if (childSnapshot != null) {
                    addVillagerNode(nodes, childSnapshot, false);
                } else {
                    addVillagerPlaceholder(nodes, childUuid, "");
                }
                addEdge(edges, edgeKeys, currentNodeId, villagerNodeId(childUuid), FamilyTreeEdgePayload.EdgeType.PARENT_CHILD);
                if (nodes.size() >= MAX_NODES || edges.size() >= MAX_EDGES) {
                    truncated = true;
                    break;
                }
                if (childSnapshot != null && queuedVillagers.add(villagerNodeId(childUuid))) {
                    queue.addLast(new QueuedVillager(childUuid, current.depth() + 1));
                }
            }
            if (truncated) {
                break;
            }
        }

        return new FamilyTreeBuildResult(rootVillagerUuid, new ArrayList<>(nodes.values()), edges, truncated);
    }

    private static void addVillagerNode(Map<String, FamilyTreeNodePayload> nodes, FamilyTreeVillagerSnapshot snapshot, boolean placeholder) {
        String nodeId = villagerNodeId(snapshot.getVillagerUuid());
        nodes.putIfAbsent(nodeId, new FamilyTreeNodePayload(
                nodeId,
                FamilyTreeNodePayload.NodeType.VILLAGER,
                snapshot.getVillagerUuid(),
                snapshot.getName(),
                snapshot.getGender(),
                snapshot.getAgeStage().name(),
                snapshot.isAlive(),
                placeholder,
                snapshot.isBornFromFamilySystem(),
                createVillagerPortrait(snapshot)
        ));
    }

    private static void addVillagerPlaceholder(Map<String, FamilyTreeNodePayload> nodes, UUID villagerUuid, String displayName) {
        if (villagerUuid == null) {
            return;
        }
        String nodeId = villagerNodeId(villagerUuid);
        nodes.putIfAbsent(nodeId, new FamilyTreeNodePayload(
                nodeId,
                FamilyTreeNodePayload.NodeType.VILLAGER,
                villagerUuid,
                displayName == null ? "" : displayName,
                "",
                "ADULT",
                true,
                true,
                false,
                new FamilyTreePortraitPayload(FamilyTreePortraitPayload.PortraitType.VILLAGER_DEFAULT, 0, "", "", false, new byte[0])
        ));
    }

    private static void addPlayerRelation(Map<String, FamilyTreeNodePayload> nodes, List<FamilyTreeEdgePayload> edges, Set<String> edgeKeys, UUID playerUuid, String playerName, String villagerNodeId, FamilyTreeEdgePayload.EdgeType edgeType, FamilyTreeSavedData data) {
        if (playerUuid == null) {
            return;
        }
        String playerNodeId = playerNodeId(playerUuid);
        FamilyTreePlayerSnapshot playerSnapshot = data.getPlayer(playerUuid);
        String displayName = playerSnapshot != null && !playerSnapshot.getName().isEmpty() ? playerSnapshot.getName() : (playerName == null ? "" : playerName);
        nodes.putIfAbsent(playerNodeId, new FamilyTreeNodePayload(
                playerNodeId,
                FamilyTreeNodePayload.NodeType.PLAYER,
                playerUuid,
                displayName,
                "",
                "",
                true,
                playerSnapshot == null,
                false,
                new FamilyTreePortraitPayload(FamilyTreePortraitPayload.PortraitType.PLAYER_PLACEHOLDER, 0, "", "", false, new byte[0])
        ));
        addEdge(edges, edgeKeys, villagerNodeId, playerNodeId, edgeType);
    }

    private static void addVillagerRelation(Map<String, FamilyTreeNodePayload> nodes, List<FamilyTreeEdgePayload> edges, Set<String> edgeKeys, UUID villagerUuid, String villagerName, String currentNodeId, FamilyTreeEdgePayload.EdgeType edgeType, FamilyTreeSavedData data, int currentDepth, Deque<QueuedVillager> queue, Set<String> queuedVillagers) {
        if (villagerUuid == null) {
            return;
        }
        FamilyTreeVillagerSnapshot snapshot = data.getVillager(villagerUuid);
        if (snapshot != null) {
            addVillagerNode(nodes, snapshot, false);
            if (currentDepth < MAX_DEPTH && queuedVillagers.add(villagerNodeId(villagerUuid))) {
                queue.addLast(new QueuedVillager(villagerUuid, currentDepth + 1));
            }
        } else {
            addVillagerPlaceholder(nodes, villagerUuid, villagerName);
        }
        addEdge(edges, edgeKeys, currentNodeId, villagerNodeId(villagerUuid), edgeType);
    }

    private static boolean hasChildrenWithFormerSpouse(FamilyTreeVillagerSnapshot snapshot, FamilyTreeSavedData data) {
        if (snapshot.getFormerSpousePlayerUuid() == null) {
            return false;
        }
        for (UUID childUuid : snapshot.getChildVillagerUuids()) {
            if (childUuid == null) {
                continue;
            }
            FamilyTreeVillagerSnapshot childSnapshot = data.getVillager(childUuid);
            if (childSnapshot != null
                    && snapshot.getFormerSpousePlayerUuid().equals(childSnapshot.getParentPlayerUuid())
                    && (snapshot.getVillagerUuid().equals(childSnapshot.getParentVillagerUuid())
                    || snapshot.getVillagerUuid().equals(childSnapshot.getParentVillager2Uuid()))) {
                return true;
            }
        }
        return false;
    }

    private static void addEdge(List<FamilyTreeEdgePayload> edges, Set<String> edgeKeys, String fromNodeId, String toNodeId, FamilyTreeEdgePayload.EdgeType edgeType) {
        if (fromNodeId == null || toNodeId == null || fromNodeId.equals(toNodeId)) {
            return;
        }
        String a = fromNodeId.compareTo(toNodeId) <= 0 ? fromNodeId : toNodeId;
        String b = fromNodeId.compareTo(toNodeId) <= 0 ? toNodeId : fromNodeId;
        String edgeKey = edgeType.name() + "|" + a + "|" + b;
        if (edgeKeys.add(edgeKey)) {
            edges.add(new FamilyTreeEdgePayload(fromNodeId, toNodeId, edgeType));
        }
    }

    private static FamilyTreePortraitPayload createVillagerPortrait(FamilyTreeVillagerSnapshot snapshot) {
        FamilyTreePortraitPayload.PortraitType portraitType;
        if (snapshot.hasCustomSkin()) {
            portraitType = FamilyTreePortraitPayload.PortraitType.VILLAGER_CUSTOM_PNG;
        } else if (!snapshot.getSkinResourcePath().isEmpty()) {
            portraitType = FamilyTreePortraitPayload.PortraitType.VILLAGER_RESOURCE;
        } else {
            portraitType = FamilyTreePortraitPayload.PortraitType.VILLAGER_DEFAULT;
        }
        return new FamilyTreePortraitPayload(
                portraitType,
                snapshot.getSkinIndex(),
                snapshot.getSkinResourcePath(),
                snapshot.getSavedSkinInput(),
                !snapshot.isAlive(),
                new byte[0]
        );
    }

    private static String villagerNodeId(UUID uuid) {
        return "villager:" + uuid;
    }

    private static String playerNodeId(UUID uuid) {
        return "player:" + uuid;
    }

    private record QueuedVillager(UUID villagerUuid, int depth) {
    }
}
