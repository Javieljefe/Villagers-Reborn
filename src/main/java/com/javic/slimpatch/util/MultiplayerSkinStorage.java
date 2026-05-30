package com.javic.slimpatch.util;

import com.javic.slimpatch.config.SlimPatchConfig;
import net.neoforged.fml.loading.FMLPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class MultiplayerSkinStorage {

    private static final byte[] PNG_SIGNATURE = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private MultiplayerSkinStorage() {
    }

    public static boolean isEnabled() {
        return SlimPatchConfig.SERVER.allowMultiplayerCustomSkins.get();
    }

    public static int getMaxSizeKb() {
        return SlimPatchConfig.SERVER.maxCustomSkinSizeKb.get();
    }

    public static Path getSkinFile(UUID villagerUuid) {
        return FMLPaths.CONFIGDIR.get()
                .resolve("slimpatch")
                .resolve("skins")
                .resolve("server")
                .resolve(villagerUuid.toString())
                .resolve("skin.png");
    }

    public static void saveSkin(UUID villagerUuid, byte[] pngData) throws IOException {
        Path skinFile = getSkinFile(villagerUuid);
        Files.createDirectories(skinFile.getParent());
        Files.write(skinFile, pngData);
    }

    public static byte[] readSkin(UUID villagerUuid) throws IOException {
        Path skinFile = getSkinFile(villagerUuid);
        return Files.exists(skinFile) ? Files.readAllBytes(skinFile) : null;
    }

    public static void deleteSkin(UUID villagerUuid) throws IOException {
        Path skinFile = getSkinFile(villagerUuid);
        Files.deleteIfExists(skinFile);
    }

    public static String getStoredSkinPath(UUID villagerUuid) {
        return getSkinFile(villagerUuid).toAbsolutePath().toString().replace('\\', '/');
    }

    public static String validatePng(byte[] pngData) {
        if (pngData == null || pngData.length == 0) {
            return "Missing skin data.";
        }
        if (pngData.length > getMaxSizeKb() * 1024) {
            return "Custom skin exceeds the configured size limit.";
        }
        if (pngData.length < PNG_SIGNATURE.length) {
            return "Custom skin is not a valid PNG file.";
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (pngData[i] != PNG_SIGNATURE[i]) {
                return "Custom skin is not a valid PNG file.";
            }
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngData));
            if (image == null) {
                return "Custom skin is not a readable PNG image.";
            }
            if (image.getWidth() < 16 || image.getHeight() < 16 || image.getWidth() > 1024 || image.getHeight() > 1024) {
                return "Custom skin dimensions are out of range.";
            }
        } catch (IOException e) {
            return "Custom skin is not a readable PNG image.";
        }

        return null;
    }

    public static String sanitizeSavedInput(String savedSkinInput) {
        if (savedSkinInput == null || savedSkinInput.isBlank()) {
            return "custom_skin.png";
        }
        String value = savedSkinInput.replace('\\', '/');
        int slash = value.lastIndexOf('/');
        if (slash >= 0) {
            value = value.substring(slash + 1);
        }
        if (value.isBlank()) {
            value = "custom_skin.png";
        }
        return value.length() > 200 ? value.substring(0, 200) : value;
    }
}
