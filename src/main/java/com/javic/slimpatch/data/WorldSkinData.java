package com.javic.slimpatch.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldSkinData extends SavedData {

    private String skinTheme = "";
    private boolean guiShown = false;

    public WorldSkinData() {}

    public static WorldSkinData load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldSkinData data = new WorldSkinData();
        if (tag.contains("SkinTheme")) data.skinTheme = tag.getString("SkinTheme");
        if (tag.contains("GuiShown")) data.guiShown = tag.getBoolean("GuiShown");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("SkinTheme", skinTheme);
        tag.putBoolean("GuiShown", guiShown);
        return tag;
    }

    public static WorldSkinData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldSkinData::new, WorldSkinData::load),
                "slimpatch_skin_data"
        );
    }

    public void setTheme(String theme) {
        if (theme == null || theme.isEmpty()) return;
        this.skinTheme = theme;
        this.setDirty();
    }

    public String getTheme() {
        return skinTheme;
    }

    public boolean isInitialized() {
        return skinTheme != null && !skinTheme.isEmpty();
    }

    public boolean isGuiShown() {
        return guiShown;
    }

    public void setGuiShown(boolean shown) {
        this.guiShown = shown;
        this.setDirty();
    }
}