package com.javic.slimpatch.client;

import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.util.MultiplayerSkinStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.Util;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ClientCustomSkinHelper {

    private ClientCustomSkinHelper() {
    }

    public static boolean allowLocalCustomSkins() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() != null && !mc.getSingleplayerServer().isPublished();
    }

    public static boolean canUploadMultiplayerCustomSkin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSingleplayerServer() != null && mc.getSingleplayerServer().isPublished()) {
            return SlimPatchConfig.SERVER.allowMultiplayerCustomSkins.get();
        }
        return ClientMultiplayerSkinSettings.isAllowed();
    }

    public static byte[] readSkinBytes(String skinInput, UUID ownerUuid) {
        return readSkinBytes(skinInput);
    }

    public static byte[] readSkinBytes(String skinInput) {
        try {
            String trimmedSkinInput = skinInput == null ? "" : skinInput.trim();
            if (trimmedSkinInput.isEmpty()) {
                return null;
            }

            int maxBytes = ClientMultiplayerSkinSettings.getMaxCustomSkinSizeKb() * 1024;
            if (trimmedSkinInput.startsWith("http://") || trimmedSkinInput.startsWith("https://")) {
                try (InputStream inputStream = URI.create(trimmedSkinInput).toURL().openStream()) {
                    byte[] data = inputStream.readNBytes(maxBytes + 1);
                    if (data.length > maxBytes) {
                        return null;
                    }
                    return data;
                }
            }

            Path directSkinFile = Path.of(trimmedSkinInput);
            if (Files.exists(directSkinFile) && Files.isRegularFile(directSkinFile)) {
                if (Files.size(directSkinFile) > maxBytes) {
                    return null;
                }
                return Files.readAllBytes(directSkinFile);
            }

            Path skinFile = new File(Minecraft.getInstance().gameDirectory, "config/slimpatch/skins/" + Minecraft.getInstance().player.getUUID() + "/" + trimmedSkinInput).toPath();
            if (!Files.exists(skinFile) || !Files.isRegularFile(skinFile)) {
                return null;
            }
            if (Files.size(skinFile) > maxBytes) {
                return null;
            }
            return Files.readAllBytes(skinFile);
        } catch (Exception e) {
            return null;
        }
    }

    public static String storePreviewSkin(UUID previewUuid, String savedSkinInput, byte[] pngData) {
        try {
            if (pngData == null || pngData.length == 0) {
                return "";
            }
            String fileName = MultiplayerSkinStorage.sanitizeSavedInput(savedSkinInput);
            if (!fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
                fileName = fileName + ".png";
            }
            Path skinFolder = FMLPaths.CONFIGDIR.get().resolve("slimpatch").resolve("birth_preview");
            Files.createDirectories(skinFolder);
            File skinFile = skinFolder.resolve(System.currentTimeMillis() + "_" + fileName).toFile();
            Files.write(skinFile.toPath(), pngData);
            return skinFile.getAbsolutePath().replace("\\", "/");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getPendingBirthSkinKey(int spouseEntityId) {
        Minecraft minecraft = Minecraft.getInstance();
        UUID playerUuid = minecraft.player != null ? minecraft.player.getUUID() : new UUID(0L, 0L);
        return "birth_" + playerUuid + "_" + spouseEntityId;
    }

    public static File getPendingBirthSkinFolder(String pendingKey) {
        return FMLPaths.CONFIGDIR.get().resolve("slimpatch").resolve("skins").resolve(pendingKey).toFile();
    }

    public static void openPendingBirthSkinFolder(String pendingKey) {
        try {
            File folder = getPendingBirthSkinFolder(pendingKey);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            Util.getPlatform().openFile(folder);
        } catch (Exception e) {
        }
    }

    public static String resolvePendingBirthSkinPath(String pendingKey, String skinInput) {
        try {
            String trimmedSkinInput = skinInput == null ? "" : skinInput.trim();
            if (trimmedSkinInput.isEmpty()) {
                return "";
            }
            File folder = getPendingBirthSkinFolder(pendingKey);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (trimmedSkinInput.startsWith("http://") || trimmedSkinInput.startsWith("https://")) {
                File skinFile = new File(folder, pendingKey + ".png");
                try (InputStream inputStream = URI.create(trimmedSkinInput).toURL().openStream()) {
                    Files.copy(inputStream, skinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return skinFile.getAbsolutePath().replace("\\", "/");
            }
            Path directSkinFile = Path.of(trimmedSkinInput);
            if (Files.exists(directSkinFile) && Files.isRegularFile(directSkinFile)) {
                return directSkinFile.toAbsolutePath().toString().replace("\\", "/");
            }
            File skinFile = new File(folder, trimmedSkinInput);
            if (skinFile.exists() && skinFile.isFile()) {
                return skinFile.getAbsolutePath().replace("\\", "/");
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static byte[] readPendingBirthSkinBytes(String pendingKey, String skinInput) {
        try {
            String resolvedPath = resolvePendingBirthSkinPath(pendingKey, skinInput);
            if (resolvedPath.isEmpty()) {
                return null;
            }
            Path path = Path.of(resolvedPath);
            int maxBytes = ClientMultiplayerSkinSettings.getMaxCustomSkinSizeKb() * 1024;
            if (!Files.exists(path) || !Files.isRegularFile(path) || Files.size(path) > maxBytes) {
                return null;
            }
            return Files.readAllBytes(path);
        } catch (Exception e) {
            return null;
        }
    }

    public static String storeLocalSkinInput(UUID ownerUuid, String skinInput) {
        try {
            String trimmedSkinInput = skinInput == null ? "" : skinInput.trim();
            if (trimmedSkinInput.isEmpty()) {
                return "";
            }
            Path directSkinFile = Path.of(trimmedSkinInput);
            if (Files.exists(directSkinFile) && Files.isRegularFile(directSkinFile)) {
                return directSkinFile.toAbsolutePath().toString().replace("\\", "/");
            }
            File skinFolder = new File(Minecraft.getInstance().gameDirectory, "config/slimpatch/skins/" + ownerUuid);
            if (!skinFolder.exists()) {
                skinFolder.mkdirs();
            }
            File skinFile;
            if (trimmedSkinInput.startsWith("http://") || trimmedSkinInput.startsWith("https://")) {
                skinFile = new File(skinFolder, ownerUuid + ".png");
                try (InputStream inputStream = URI.create(trimmedSkinInput).toURL().openStream()) {
                    Files.copy(inputStream, skinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                skinFile = new File(skinFolder, trimmedSkinInput);
            }
            if (!skinFile.exists() || !skinFile.isFile()) {
                return "";
            }
            return skinFile.getAbsolutePath().replace("\\", "/");
        } catch (Exception e) {
            return "";
        }
    }

    public static void clearPreviewSkin(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.getPersistentData().remove("SavedSkinInput");
        entity.getPersistentData().remove("CustomSkinPath");
    }
}
