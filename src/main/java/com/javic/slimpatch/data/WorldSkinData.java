package com.javic.slimpatch.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Almacena el tema global de skins (Modern o Fantasy) a nivel de mundo.
 * Persiste entre sesiones y se sincroniza automáticamente a todos los clientes.
 * 
 * 🔸 Incluye un flag "guiShown" para asegurar que la GUI de selección
 *     de skins solo se muestra una vez en toda la historia del mundo.
 */
public class WorldSkinData extends SavedData {

    private String skinTheme = ""; // vacío → fuerza GUI en mundos nuevos
    private boolean guiShown = false;

    public WorldSkinData() {}

    // ============================================================
    // 🧩 CARGA / GUARDADO NBT
    // ============================================================

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

    // ============================================================
    // 🌍 ACCESO GLOBAL
    // ============================================================

    public static WorldSkinData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldSkinData::new, WorldSkinData::load),
                "slimpatch_skin_data"
        );
    }

    // ============================================================
    // 🎨 GET / SET
    // ============================================================

    public void setTheme(String theme) {
        if (theme == null || theme.isEmpty()) return;
        this.skinTheme = theme;
        this.setDirty(); // marcar para guardar
    }

    public String getTheme() {
        return skinTheme;
    }

    public boolean isInitialized() {
        return skinTheme != null && !skinTheme.isEmpty();
    }

    // ============================================================
    // 🧩 FLAG GUI
    // ============================================================

    /** Devuelve true si la GUI ya se mostró alguna vez en este mundo */
    public boolean isGuiShown() {
        return guiShown;
    }

    /** Marca la GUI como mostrada permanentemente */
    public void setGuiShown(boolean shown) {
        this.guiShown = shown;
        this.setDirty();
    }
}