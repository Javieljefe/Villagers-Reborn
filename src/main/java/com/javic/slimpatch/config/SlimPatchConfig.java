package com.javic.slimpatch.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SlimPatchConfig {

    public static final ModConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SERVER = new ServerConfig(builder);
        SERVER_SPEC = builder.build();
    }

    public static class ServerConfig {
        public final ModConfigSpec.ConfigValue<String> skinType;
        public final ModConfigSpec.BooleanValue allowMultiplayerCustomSkins;
        public final ModConfigSpec.IntValue maxCustomSkinSizeKb;

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Global Villagers Reborn configuration (dedicated servers only).")
                    .translation("slimpatch.configuration.server")
                    .push("server");
            skinType = builder.comment("Global skin type for villagers: 'modern' or 'fantasy'")
                    .translation("slimpatch.configuration.skinType")
                    .define("skinType", "modern");
            allowMultiplayerCustomSkins = builder.comment("Allow villagers to use uploaded custom PNG skins in multiplayer/LAN. Disabled keeps multiplayer custom skins blocked.")
                    .translation("slimpatch.configuration.allowMultiplayerCustomSkins")
                    .define("allowMultiplayerCustomSkins", false);
            maxCustomSkinSizeKb = builder.comment("Maximum uploaded custom skin size in kilobytes for multiplayer/LAN uploads.")
                    .translation("slimpatch.configuration.maxCustomSkinSizeKb")
                    .defineInRange("maxCustomSkinSizeKb", 256, 16, 2048);
            builder.pop();
        }
    }
}
