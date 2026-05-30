package com.javic.slimpatch.familytree;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class FamilyTreeNodePayload {

    public enum NodeType {
        VILLAGER,
        PLAYER
    }

    private final String nodeId;
    private final NodeType nodeType;
    private final UUID uuid;
    private final String displayName;
    private final String gender;
    private final String ageStage;
    private final boolean alive;
    private final boolean placeholder;
    private final boolean bornFromFamilySystem;
    private final FamilyTreePortraitPayload portrait;

    public FamilyTreeNodePayload(String nodeId, NodeType nodeType, UUID uuid, String displayName, String gender, String ageStage, boolean alive, boolean placeholder, boolean bornFromFamilySystem, FamilyTreePortraitPayload portrait) {
        this.nodeId = nodeId == null ? "" : nodeId;
        this.nodeType = nodeType == null ? NodeType.VILLAGER : nodeType;
        this.uuid = uuid;
        this.displayName = displayName == null ? "" : displayName;
        this.gender = gender == null ? "" : gender;
        this.ageStage = ageStage == null ? "" : ageStage;
        this.alive = alive;
        this.placeholder = placeholder;
        this.bornFromFamilySystem = bornFromFamilySystem;
        this.portrait = portrait == null ? new FamilyTreePortraitPayload(FamilyTreePortraitPayload.PortraitType.PLAYER_PLACEHOLDER, 0, "", "", false, new byte[0]) : portrait;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getGender() {
        return this.gender;
    }

    public String getAgeStage() {
        return this.ageStage;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public boolean isPlaceholder() {
        return this.placeholder;
    }

    public boolean isBornFromFamilySystem() {
        return this.bornFromFamilySystem;
    }

    public FamilyTreePortraitPayload getPortrait() {
        return this.portrait;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.nodeId);
        buf.writeEnum(this.nodeType);
        buf.writeBoolean(this.uuid != null);
        if (this.uuid != null) {
            buf.writeUUID(this.uuid);
        }
        buf.writeUtf(this.displayName);
        buf.writeUtf(this.gender);
        buf.writeUtf(this.ageStage);
        buf.writeBoolean(this.alive);
        buf.writeBoolean(this.placeholder);
        buf.writeBoolean(this.bornFromFamilySystem);
        this.portrait.encode(buf);
    }

    public static FamilyTreeNodePayload decode(FriendlyByteBuf buf) {
        return new FamilyTreeNodePayload(
                buf.readUtf(),
                buf.readEnum(NodeType.class),
                buf.readBoolean() ? buf.readUUID() : null,
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                FamilyTreePortraitPayload.decode(buf)
        );
    }
}
