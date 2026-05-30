package com.javic.slimpatch.familytree;

import net.minecraft.network.FriendlyByteBuf;

public class FamilyTreePortraitPayload {

    public enum PortraitType {
        VILLAGER_DEFAULT,
        VILLAGER_RESOURCE,
        VILLAGER_CUSTOM_PNG,
        PLAYER_PLACEHOLDER
    }

    private final PortraitType portraitType;
    private final int skinIndex;
    private final String skinResourcePath;
    private final String savedSkinInput;
    private final boolean deadGraySuggested;
    private final byte[] customSkinPngData;

    public FamilyTreePortraitPayload(PortraitType portraitType, int skinIndex, String skinResourcePath, String savedSkinInput, boolean deadGraySuggested, byte[] customSkinPngData) {
        this.portraitType = portraitType == null ? PortraitType.PLAYER_PLACEHOLDER : portraitType;
        this.skinIndex = skinIndex;
        this.skinResourcePath = skinResourcePath == null ? "" : skinResourcePath;
        this.savedSkinInput = savedSkinInput == null ? "" : savedSkinInput;
        this.deadGraySuggested = deadGraySuggested;
        this.customSkinPngData = customSkinPngData == null ? new byte[0] : customSkinPngData;
    }

    public PortraitType getPortraitType() {
        return this.portraitType;
    }

    public int getSkinIndex() {
        return this.skinIndex;
    }

    public String getSkinResourcePath() {
        return this.skinResourcePath;
    }

    public String getSavedSkinInput() {
        return this.savedSkinInput;
    }

    public boolean isDeadGraySuggested() {
        return this.deadGraySuggested;
    }

    public byte[] getCustomSkinPngData() {
        return this.customSkinPngData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.portraitType);
        buf.writeVarInt(this.skinIndex);
        buf.writeUtf(this.skinResourcePath);
        buf.writeUtf(this.savedSkinInput);
        buf.writeBoolean(this.deadGraySuggested);
        buf.writeByteArray(this.customSkinPngData);
    }

    public static FamilyTreePortraitPayload decode(FriendlyByteBuf buf) {
        return new FamilyTreePortraitPayload(
                buf.readEnum(PortraitType.class),
                buf.readVarInt(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readByteArray()
        );
    }
}
