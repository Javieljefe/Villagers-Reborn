package com.javic.slimpatch.client;

import net.minecraft.client.Minecraft;

public class ClientSkinTheme {

    private static String theme = "";
    private static boolean hasSentToServer = false;

    public static void setTheme(String newTheme) {
        if (newTheme == null || newTheme.isEmpty()) return;
        theme = newTheme;
        hasSentToServer = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {}
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
}