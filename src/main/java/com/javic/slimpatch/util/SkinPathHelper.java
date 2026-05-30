package com.javic.slimpatch.util;

import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.data.WorldSkinData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

public class SkinPathHelper {

    public static ResourceLocation getSkin(String gender, int skinId, Level level) {
        return getSkinForType("custom_villager", gender, skinId, level);
    }

    public static ResourceLocation getSkinForType(String type, String gender, int skinId, Level level) {
        String theme = null;
        boolean useTheme = "custom_villager".equals(type);

        if (useTheme) {
            if (FMLEnvironment.dist.isDedicatedServer()) {
                theme = SlimPatchConfig.SERVER.skinType.get();
            } else if (level instanceof ServerLevel serverLevel) {
                theme = WorldSkinData.get(serverLevel).getTheme();
            } else {
                theme = getClientTheme();
            }

            if (theme == null || theme.isEmpty()) {
                theme = "modern";
            }
        }

        try {
            if (level != null && level.isClientSide) {
                net.minecraft.world.entity.Entity e = getClientMountedEntity();

                if (e != null) {
                    File gameDirectory = getClientGameDirectory();
                    if (gameDirectory != null) {
                        File file = new File(gameDirectory, "config/slimpatch/skins/" + e.getUUID() + "/skin.png");
                        if (file.exists() && file.isFile()) {
                            ResourceLocation loc = loadExternalSkinTexture(e.getUUID(), file);
                            if (loc != null) {
                                return loc;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }

        StringBuilder path = new StringBuilder("slimpatch:textures/entity/")
                .append(type).append("/");

        if (useTheme && "fantasy".equalsIgnoreCase(theme)) {
            path.append("fantasy/");
        }

        path.append(gender.toLowerCase()).append("/skin_");

        if (useTheme || type.equals("human_trader") || type.equals("human_witch")) {
            path.append(skinId);
        } else {
            path.append(String.format("%02d", skinId));
        }

        path.append(".png");

        ResourceLocation loc = ResourceLocation.tryParse(path.toString());
        if (loc == null) {
            loc = ResourceLocation.tryParse("slimpatch:textures/entity/" + type + "/" + gender + "/skin_1.png");
        }

        try {
            if (FMLEnvironment.dist.isClient() && isClientResourceMissing(loc)) {
                int fallback = (int) (Math.random() * 70) + 1;
                String fallbackPath = "slimpatch:textures/entity/custom_villager/modern/" + gender + "/skin_" + fallback + ".png";
                loc = ResourceLocation.tryParse(fallbackPath);
            }
        } catch (Exception e) {
            int fallback = (int) (Math.random() * 70) + 1;
            String fallbackPath = "slimpatch:textures/entity/custom_villager/modern/" + gender + "/skin_" + fallback + ".png";
            loc = ResourceLocation.tryParse(fallbackPath);
        }

        return loc;
    }

    public static ResourceLocation from(String path) {
        return ResourceLocation.fromNamespaceAndPath("slimpatch", path);
    }

    public static ResourceLocation loadExternalSkinTexture(UUID entityId, File external) {
        if (!FMLEnvironment.dist.isClient()) {
            return null;
        }

        try {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("slimpatch_dyn",
                    "skins/" + entityId + "/" + external.getName());

            Object minecraft = getClientMinecraft();
            if (minecraft == null) {
                return null;
            }

            Object textureManager = minecraft.getClass().getMethod("getTextureManager").invoke(minecraft);
            Object texture = textureManager.getClass()
                    .getMethod("getTexture", ResourceLocation.class)
                    .invoke(textureManager, loc);

            if (texture == null) {
                try (InputStream stream = new FileInputStream(external)) {
                    Class<?> nativeImageClass = Class.forName("com.mojang.blaze3d.platform.NativeImage");
                    Object nativeImage = nativeImageClass.getMethod("read", InputStream.class).invoke(null, stream);
                    if (nativeImage == null) {
                        return null;
                    }

                    Class<?> abstractTextureClass = Class.forName("net.minecraft.client.renderer.texture.AbstractTexture");
                    Class<?> dynamicTextureClass = Class.forName("net.minecraft.client.renderer.texture.DynamicTexture");
                    Object dynamicTexture = dynamicTextureClass.getConstructor(nativeImageClass).newInstance(nativeImage);
                    textureManager.getClass()
                            .getMethod("register", ResourceLocation.class, abstractTextureClass)
                            .invoke(textureManager, loc, dynamicTexture);
                }
            }

            return loc;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getClientTheme() {
        try {
            return (String) Class.forName("com.javic.slimpatch.client.ClientSkinTheme")
                    .getMethod("getTheme")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static net.minecraft.world.entity.Entity getClientMountedEntity() {
        try {
            Object minecraft = getClientMinecraft();
            if (minecraft == null) {
                return null;
            }

            Object player = minecraft.getClass().getField("player").get(minecraft);
            if (player == null) {
                return null;
            }

            return (net.minecraft.world.entity.Entity) player.getClass().getMethod("getVehicle").invoke(player);
        } catch (Exception e) {
            return null;
        }
    }

    private static File getClientGameDirectory() {
        try {
            Object minecraft = getClientMinecraft();
            if (minecraft == null) {
                return null;
            }

            return (File) minecraft.getClass().getField("gameDirectory").get(minecraft);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isClientResourceMissing(ResourceLocation loc) {
        try {
            Object minecraft = getClientMinecraft();
            if (minecraft == null) {
                return false;
            }

            Object resourceManager = minecraft.getClass().getMethod("getResourceManager").invoke(minecraft);
            Object resource = resourceManager.getClass().getMethod("getResource", ResourceLocation.class).invoke(resourceManager, loc);
            return Boolean.TRUE.equals(resource.getClass().getMethod("isEmpty").invoke(resource));
        } catch (Exception e) {
            return false;
        }
    }

    private static Object getClientMinecraft() {
        try {
            return Class.forName("net.minecraft.client.Minecraft")
                    .getMethod("getInstance")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
