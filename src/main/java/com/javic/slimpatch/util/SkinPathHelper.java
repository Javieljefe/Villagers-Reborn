package com.javic.slimpatch.util;

import com.javic.slimpatch.client.ClientSkinTheme;
import com.javic.slimpatch.config.SlimPatchConfig;
import com.javic.slimpatch.data.WorldSkinData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;

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
                theme = ClientSkinTheme.getTheme();
            }

            if (theme == null || theme.isEmpty()) {
                theme = "modern";
            }
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

        // 🔸 Verificar existencia del recurso; si no existe, usar una skin de respaldo
        try {
            if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(loc).isEmpty()) {
                int fallback = (int) (Math.random() * 70) + 1;
                String fallbackPath = "slimpatch:textures/entity/custom_villager/modern/" + gender + "/skin_" + fallback + ".png";
                loc = ResourceLocation.tryParse(fallbackPath);
                System.out.println("[SlimPatch] Textura no encontrada: " + path + " → asignada skin de respaldo " + fallbackPath);
            }
        } catch (Exception e) {
            int fallback = (int) (Math.random() * 70) + 1;
            String fallbackPath = "slimpatch:textures/entity/custom_villager/modern/" + gender + "/skin_" + fallback + ".png";
            loc = ResourceLocation.tryParse(fallbackPath);
            System.out.println("[SlimPatch] Excepción al verificar textura: " + path + " → asignada skin de respaldo " + fallbackPath);
        }

        return loc;
    }

    public static ResourceLocation from(String path) {
        return ResourceLocation.fromNamespaceAndPath("slimpatch", path);
    }
}