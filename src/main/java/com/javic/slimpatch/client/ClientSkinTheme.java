package com.javic.slimpatch.client;

import net.minecraft.client.Minecraft;

public class ClientSkinTheme {

    private static String theme = "";
    private static boolean hasSentToServer = false;

    public static void setTheme(String newTheme) {
        String normalized = normalizeTheme(newTheme);
        if (normalized == null) return;
        applyTheme(normalized);
    }

    public static void sendThemeToServerIfNeeded() {}

    public static String getTheme() {
        return theme;
    }

    private static boolean guiShownOnce = false;

    public static boolean wasGuiShownOnce() {
        return guiShownOnce;
    }

    public static void markGuiShownOnce() {
        guiShownOnce = true;
    }

    public static void clear() {
        applyTheme("");
    }

    private static void applyTheme(String newTheme) {
        if (theme.equals(newTheme)) return;
        theme = newTheme;
        hasSentToServer = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.levelRenderer.allChanged();
        }
    }

    private static String normalizeTheme(String newTheme) {
        if (newTheme == null || newTheme.isEmpty()) return null;
        return "fantasy".equalsIgnoreCase(newTheme) ? "fantasy" : "modern";
    }
}
