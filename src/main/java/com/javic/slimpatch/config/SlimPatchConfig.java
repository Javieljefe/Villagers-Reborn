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

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Global SlimPatch configuration (dedicated servers only).")
                    .push("server");
            skinType = builder.comment("Global skin type for villagers: 'modern' or 'fantasy'")
                    .define("skinType", "modern");
            builder.pop();
        }
    }
}