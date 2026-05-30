package com.javic.slimpatch.client.gui.familytree;

import com.javic.slimpatch.familytree.FamilyTreeNodePayload;
import com.javic.slimpatch.familytree.FamilyTreePortraitPayload;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public final class FamilyTreePortraitResolver {

    private FamilyTreePortraitResolver() {
    }

    public static ResourceLocation resolve(Minecraft minecraft, FamilyTreeNodePayload node, Map<String, ResourceLocation> cache) {
        if (minecraft == null || node == null || cache == null) {
            return null;
        }
        String cacheKey = getCacheKey(node);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        ResourceLocation location = null;
        if (node.getNodeType() == FamilyTreeNodePayload.NodeType.PLAYER) {
            location = resolvePlayerSkin(minecraft, node);
            cache.put(cacheKey, location);
            return location;
        }
        String nodeId = node.getNodeId();
        FamilyTreePortraitPayload portrait = node.getPortrait();
        if (portrait != null) {
            if (portrait.getPortraitType() == FamilyTreePortraitPayload.PortraitType.VILLAGER_CUSTOM_PNG && portrait.getCustomSkinPngData().length > 0) {
                location = loadDynamicTexture(minecraft, cacheKey, portrait.getCustomSkinPngData());
            }
            if (location == null && !portrait.getSkinResourcePath().isEmpty()) {
                location = ResourceLocation.tryParse(portrait.getSkinResourcePath());
                if (location == null) {
                    File external = new File(portrait.getSkinResourcePath());
                    if (external.exists() && external.isFile()) {
                        location = loadExternalTexture(minecraft, cacheKey, external);
                    }
                }
            }
        }
        cache.put(cacheKey, location);
        return location;
    }

    private static String getCacheKey(FamilyTreeNodePayload node) {
        FamilyTreePortraitPayload portrait = node.getPortrait();
        if (portrait == null) {
            return node.getNodeId();
        }
        int pngHash = java.util.Arrays.hashCode(portrait.getCustomSkinPngData());
        String skinResourcePath = portrait.getSkinResourcePath();
        if (!skinResourcePath.isEmpty() && ResourceLocation.tryParse(skinResourcePath) == null) {
            File external = new File(skinResourcePath);
            if (external.exists() && external.isFile()) {
                skinResourcePath = skinResourcePath + "|" + external.lastModified() + "|" + external.length();
            }
        }
        return node.getNodeId()
                + "|" + portrait.getPortraitType().name()
                + "|" + portrait.getSkinIndex()
                + "|" + skinResourcePath
                + "|" + portrait.getSavedSkinInput()
                + "|" + pngHash;
    }

    private static ResourceLocation resolvePlayerSkin(Minecraft minecraft, FamilyTreeNodePayload node) {
        if (minecraft.getConnection() != null && node.getUuid() != null) {
            var playerInfo = minecraft.getConnection().getPlayerInfo(node.getUuid());
            if (playerInfo != null) {
                return playerInfo.getSkin().texture();
            }
        }
        if (minecraft.player != null && node.getUuid() != null && node.getUuid().equals(minecraft.player.getUUID())) {
            return minecraft.player.getSkin().texture();
        }
        return null;
    }	

    private static ResourceLocation loadDynamicTexture(Minecraft minecraft, String nodeId, byte[] pngData) {
        try {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath("slimpatch", "family_tree/" + nodeId.replace(':', '_'));
            minecraft.getTextureManager().register(location, new DynamicTexture(NativeImage.read(new ByteArrayInputStream(pngData))));
            return location;
        } catch (Exception e) {
            return null;
        }
    }

    private static ResourceLocation loadExternalTexture(Minecraft minecraft, String cacheKey, File external) {
        try {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath("slimpatch", "family_tree/" + Integer.toHexString(cacheKey.hashCode()));
            try (FileInputStream inputStream = new FileInputStream(external)) {
                minecraft.getTextureManager().register(location, new DynamicTexture(NativeImage.read(inputStream)));
            }
            return location;
        } catch (Exception e) {
            return null;
        }
    }
}
