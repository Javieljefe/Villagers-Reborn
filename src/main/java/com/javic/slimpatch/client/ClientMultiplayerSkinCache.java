package com.javic.slimpatch.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ClientMultiplayerSkinCache {

    private ClientMultiplayerSkinCache() {
    }

    public static String storeSkin(UUID villagerUuid, byte[] pngData) throws IOException {
        Path skinFile = getSkinFile(villagerUuid);
        Files.createDirectories(skinFile.getParent());
        Files.write(skinFile, pngData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return skinFile.toAbsolutePath().toString().replace('\\', '/');
    }

    public static void clearSkin(UUID villagerUuid) throws IOException {
        Files.deleteIfExists(getSkinFile(villagerUuid));
    }

    private static Path getSkinFile(UUID villagerUuid) {
        String serverId = getServerIdentifier();
        return Paths.get(Minecraft.getInstance().gameDirectory.getAbsolutePath())
                .resolve("config")
                .resolve("slimpatch")
                .resolve("cache")
                .resolve("server_skins")
                .resolve(serverId)
                .resolve(villagerUuid.toString())
                .resolve("skin.png");
    }

    private static String getServerIdentifier() {
        Minecraft mc = Minecraft.getInstance();
        ServerData current = mc.getCurrentServer();
        String value = current != null && current.ip != null && !current.ip.isBlank()
                ? current.ip
                : mc.getSingleplayerServer() != null && mc.getSingleplayerServer().isPublished()
                ? mc.getSingleplayerServer().getMotd()
                : "singleplayer";
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
