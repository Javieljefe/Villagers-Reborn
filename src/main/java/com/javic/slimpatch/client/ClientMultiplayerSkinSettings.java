package com.javic.slimpatch.client;

public final class ClientMultiplayerSkinSettings {

    private static boolean allowMultiplayerCustomSkins;
    private static int maxCustomSkinSizeKb = 256;

    private ClientMultiplayerSkinSettings() {
    }

    public static void set(boolean allow, int maxSizeKb) {
        allowMultiplayerCustomSkins = allow;
        maxCustomSkinSizeKb = maxSizeKb;
    }

    public static boolean isAllowed() {
        return allowMultiplayerCustomSkins;
    }

    public static int getMaxCustomSkinSizeKb() {
        return maxCustomSkinSizeKb;
    }
}
