package com.javic.slimpatch.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.util.List;

public final class BirthScreenData {

    public static final int MAX_CHILD_NAME_LENGTH = 32;
    private static final int MAX_CURATED_SKINS = 70;
    private static final List<VillagerPersonality> AVAILABLE_PERSONALITIES = List.of(
            VillagerPersonality.FRIENDLY,
            VillagerPersonality.MEAN,
            VillagerPersonality.SHY,
            VillagerPersonality.BRAVE,
            VillagerPersonality.GRUMPY,
            VillagerPersonality.GREEDY,
            VillagerPersonality.WISE
    );
    private static final List<Integer> MODERN_MALE_SKINS = List.of(1, 2, 3, 4, 5, 6, 7, 8);
    private static final List<Integer> FANTASY_MALE_SKINS = List.of(1, 2, 3, 4, 5, 6, 7, 8);
    private static final List<Integer> MODERN_FEMALE_SKINS = List.of(1, 2, 3, 4, 5, 6, 7, 8);
    private static final List<Integer> FANTASY_FEMALE_SKINS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    private BirthScreenData() {
    }

    public static String normalizeGender(String gender) {
        return "female".equalsIgnoreCase(gender) ? "female" : "male";
    }

    public static String normalizeSkinType(String skinType) {
        return "fantasy".equalsIgnoreCase(skinType) ? "fantasy" : "modern";
    }

    public static List<Integer> getValidSkinIds(String gender, String skinType) {
        String normalizedGender = normalizeGender(gender);
        String normalizedSkinType = normalizeSkinType(skinType);
        java.util.List<Integer> childSkinIds = new java.util.ArrayList<>();
        for (int i = 1; i <= MAX_CURATED_SKINS; i++) {
            if (getCuratedSkinResourcePath(normalizedGender, normalizedSkinType, i) != null) {
                childSkinIds.add(i);
            }
        }
        if (!childSkinIds.isEmpty()) {
            return java.util.List.copyOf(childSkinIds);
        }
        if ("female".equals(normalizedGender)) {
            return "fantasy".equals(normalizedSkinType) ? FANTASY_FEMALE_SKINS : MODERN_FEMALE_SKINS;
        }
        return "fantasy".equals(normalizedSkinType) ? FANTASY_MALE_SKINS : MODERN_MALE_SKINS;
    }

    public static String getCuratedSkinResourcePath(String gender, String skinType, int skinId) {
        String normalizedGender = normalizeGender(gender);
        String normalizedSkinType = normalizeSkinType(skinType);
        String path = "assets/slimpatch/textures/entity/custom_villager/";
        String resourcePath = "slimpatch:textures/entity/custom_villager/";
        if ("fantasy".equals(normalizedSkinType)) {
            path += "fantasy/child/" + normalizedGender + "/skin_" + skinId + ".png";
            resourcePath += "fantasy/child/" + normalizedGender + "/skin_" + skinId + ".png";
        } else {
            path += "child/" + normalizedGender + "/skin_" + skinId + ".png";
            resourcePath += "child/" + normalizedGender + "/skin_" + skinId + ".png";
        }
        ClassLoader classLoader = BirthScreenData.class.getClassLoader();
        return classLoader.getResource(path) != null ? resourcePath : null;
    }

    public static boolean isValidSkin(String gender, String skinType, int skinId) {
        return getValidSkinIds(gender, skinType).contains(skinId);
    }

    public static int getDefaultSkinId(String gender, String skinType) {
        return getValidSkinIds(gender, skinType).getFirst();
    }

    public static int getRandomSkinId(String gender, String skinType, RandomSource random) {
        List<Integer> skinIds = getValidSkinIds(gender, skinType);
        return skinIds.get(random.nextInt(skinIds.size()));
    }

    public static List<VillagerPersonality> getAvailablePersonalities() {
        return AVAILABLE_PERSONALITIES;
    }

    public static boolean isValidPersonality(VillagerPersonality personality) {
        return personality != null && AVAILABLE_PERSONALITIES.contains(personality);
    }

    public static VillagerPersonality sanitizeBirthPersonality(VillagerPersonality personality) {
        return isValidPersonality(personality) ? personality : VillagerPersonality.FRIENDLY;
    }

    public static VillagerPersonality getRandomPersonality(RandomSource random) {
        return AVAILABLE_PERSONALITIES.get(random.nextInt(AVAILABLE_PERSONALITIES.size()));
    }

    public static VillagerPersonality cyclePersonality(VillagerPersonality current, int direction) {
        int currentIndex = AVAILABLE_PERSONALITIES.indexOf(sanitizeBirthPersonality(current));
        int nextIndex = Math.floorMod(currentIndex + direction, AVAILABLE_PERSONALITIES.size());
        return AVAILABLE_PERSONALITIES.get(nextIndex);
    }

    public static Component getPersonalityName(VillagerPersonality personality) {
        VillagerPersonality resolved = sanitizeBirthPersonality(personality);
        return Component.translatable("slimpatch.personality." + resolved.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static String sanitizeChildName(String childName) {
        if (childName == null) {
            return "";
        }
        String trimmed = childName.trim();
        if (trimmed.length() > MAX_CHILD_NAME_LENGTH) {
            return trimmed.substring(0, MAX_CHILD_NAME_LENGTH).trim();
        }
        return trimmed;
    }

    public static boolean isValidChildName(String childName) {
        String sanitized = sanitizeChildName(childName);
        return !sanitized.isEmpty() && sanitized.length() <= MAX_CHILD_NAME_LENGTH;
    }
}
