package com.javic.slimpatch;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static {
        BUILDER.push("appearance");
    }
    public static final ModConfigSpec.BooleanValue CUSTOM_VILLAGER_SKINS = BUILDER
            .translation("slimpatch.config.customVillagerSkins")
            .define("customVillagerSkins", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_ILLAGER_SKINS = BUILDER
            .translation("slimpatch.config.customIllagerSkins")
            .define("customIllagerSkins", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_WITCH_SKIN = BUILDER
            .translation("slimpatch.config.customWitchSkin")
            .define("customWitchSkin", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_PILLAGER_MODEL = BUILDER
            .translation("slimpatch.config.customPillagerModel")
            .define("customPillagerModel", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_VINDICATOR_MODEL = BUILDER
            .translation("slimpatch.config.customVindicatorModel")
            .define("customVindicatorModel", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_EVOKER_MODEL = BUILDER
            .translation("slimpatch.config.customEvokerModel")
            .define("customEvokerModel", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_VILLAGER_SOUNDS = BUILDER
            .translation("slimpatch.config.customVillagerSounds")
            .define("customVillagerSounds", true);
    public static final ModConfigSpec.BooleanValue CUSTOM_ILLAGER_SOUNDS = BUILDER
            .translation("slimpatch.config.customIllagerSounds")
            .define("customIllagerSounds", true);
    public static final ModConfigSpec.BooleanValue VILLAGER_NAME_TAG = BUILDER
            .translation("slimpatch.config.villagerNameTag")
            .define("villagerNameTag", true);
    public static final ModConfigSpec.BooleanValue PRESS_R_TO_TALK_INDICATOR = BUILDER
            .translation("slimpatch.config.pressRToTalkIndicator")
            .define("pressRToTalkIndicator", true);
    static {
        BUILDER.pop();
        BUILDER.push("gameplay");
    }
    public static final ModConfigSpec.DoubleValue DIALOGUE_CAMERA_SENSITIVITY = BUILDER
            .translation("slimpatch.config.dialogueCameraSensitivity")
            .defineInRange("dialogueCameraSensitivity", 1.0D, 0.5D, 1.5D);
    public static final ModConfigSpec.BooleanValue INVERT_DIALOGUE_CAMERA_X = BUILDER
            .translation("slimpatch.config.invertDialogueCameraX")
            .define("invertDialogueCameraX", false);
    public static final ModConfigSpec.BooleanValue INVERT_DIALOGUE_CAMERA_Y = BUILDER
            .translation("slimpatch.config.invertDialogueCameraY")
            .define("invertDialogueCameraY", false);
    public static final ModConfigSpec.BooleanValue DIALOGUE_SCREEN_BLUR = BUILDER
            .translation("slimpatch.config.dialogueScreenBlur")
            .define("dialogueScreenBlur", false);
    public static final ModConfigSpec.BooleanValue FORCE_FIRST_PERSON_IN_DIALOGUE = BUILDER
            .translation("slimpatch.config.forceFirstPersonInDialogue")
            .define("forceFirstPersonInDialogue", false);
    public static final ModConfigSpec.DoubleValue HEALTH_PER_GOLDEN_CARROT = BUILDER
            .translation("slimpatch.config.healthPerGoldenCarrot")
            .defineInRange("healthPerGoldenCarrot", 2.0D, 0.1D, 100.0D);
    public static final ModConfigSpec.DoubleValue MAX_BONUS_HEALTH = BUILDER
            .translation("slimpatch.config.maxBonusHealth")
            .defineInRange("maxBonusHealth", 10.0D, 0.0D, 1000.0D);
    static {
        BUILDER.pop();
        BUILDER.push("family");
    }
    public static final ModConfigSpec.BooleanValue GENDERED_RELATIONSHIP_LABELS = BUILDER
            .translation("slimpatch.config.genderedRelationshipLabels")
            .define("genderedRelationshipLabels", true);
    public static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_PLAYER_SPOUSES = BUILDER
            .translation("slimpatch.config.allowMultiplePlayerSpouses")
            .define("allowMultiplePlayerSpouses", true);
    public static final ModConfigSpec.BooleanValue SPOUSE_COOKING_ENABLED = BUILDER
            .translation("slimpatch.config.spouseCookingEnabled")
            .define("spouseCookingEnabled", true);
    public static final ModConfigSpec.IntValue SPOUSE_COOKING_COOLDOWN_TICKS = BUILDER
            .translation("slimpatch.config.spouseCookingCooldownTicks")
            .defineInRange("spouseCookingCooldownTicks", 24000, 1200, 240000);
    public static final ModConfigSpec.BooleanValue ENABLE_WEDDING_CUTSCENE = BUILDER
            .translation("slimpatch.config.enableWeddingCutscene")
            .define("enableWeddingCutscene", true);
    public static final ModConfigSpec.BooleanValue ENABLE_FAMILY_CUTSCENE = BUILDER
            .translation("slimpatch.config.enableFamilyCutscene")
            .define("enableFamilyCutscene", true);
    public static final ModConfigSpec.IntValue PREGNANCY_DURATION_TICKS = BUILDER
            .translation("slimpatch.config.pregnancyDurationTicks")
            .defineInRange("pregnancyDurationTicks", 24000, 20, 24000000);
    public static final ModConfigSpec.BooleanValue ENABLE_PREGNANCY_BELLY = BUILDER
            .translation("slimpatch.config.enablePregnancyBelly")
            .define("enablePregnancyBelly", false);
    public static final ModConfigSpec.IntValue FAMILY_CHARM_MAX_CHILDREN_PER_PAIR = BUILDER
            .translation("slimpatch.config.familyCharmMaxChildrenPerPair")
            .defineInRange("familyCharmMaxChildrenPerPair", 3, 1, 20);
    public static final ModConfigSpec.BooleanValue ENABLE_AGING = BUILDER
            .translation("slimpatch.config.enableAging")
            .define("enableAging", true);
    public static final ModConfigSpec.IntValue TODDLER_DURATION_TICKS = BUILDER
            .translation("slimpatch.config.toddlerDurationTicks")
            .defineInRange("toddlerDurationTicks", 72000, 20, 24000000);
    public static final ModConfigSpec.IntValue CHILD_DURATION_TICKS = BUILDER
            .translation("slimpatch.config.childDurationTicks")
            .defineInRange("childDurationTicks", 168000, 20, 24000000);
    public static final ModConfigSpec.IntValue TEEN_DURATION_TICKS = BUILDER
            .translation("slimpatch.config.teenDurationTicks")
            .defineInRange("teenDurationTicks", 168000, 20, 24000000);
    static {
        BUILDER.pop();
    }
    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
