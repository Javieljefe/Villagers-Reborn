package com.javic.slimpatch.dialogue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerPersonality;
import net.minecraft.world.entity.npc.Villager;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DialogueManager {

    private static final Map<UUID, UUID> ACTIVE_DIALOGUES = new HashMap<>();

    public static void startDialogue(Villager villager, net.minecraft.world.entity.player.Player player) {
        if (villager != null && player != null) {
            ACTIVE_DIALOGUES.put(villager.getUUID(), player.getUUID());
        }
    }

    public static void endDialogue(Villager villager) {
        if (villager != null) {
            ACTIVE_DIALOGUES.remove(villager.getUUID());
        }
    }

    public static boolean isInDialogue(Villager villager) {
        return villager != null && ACTIVE_DIALOGUES.containsKey(villager.getUUID());
    }

    public static UUID getDialoguePlayer(Villager villager) {
        return villager != null ? ACTIVE_DIALOGUES.get(villager.getUUID()) : null;
    }

    private static final Map<String, Map<String, Object>> DIALOGUES = new HashMap<>();

    static {
        loadDialogues();
    }

    @SuppressWarnings("unchecked")
    private static void loadDialogues() {
        try {
            var resource = DialogueManager.class.getClassLoader().getResourceAsStream("assets/slimpatch/dialogues/dialogues.json");
            if (resource == null) {
                SlimPatch.LOGGER.error("[SlimPatch] dialogues.json not found in assets/slimpatch/dialogues/");
                return;
            }

            InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> data = new Gson().fromJson(reader, type);
            reader.close();

            if (data != null) {
                DIALOGUES.clear();
                DIALOGUES.putAll(data);
                SlimPatch.LOGGER.info("[SlimPatch] Loaded {} dialogue categories from JSON.", DIALOGUES.size());
            } else {
                SlimPatch.LOGGER.warn("[SlimPatch] dialogues.json is empty or invalid.");
            }
        } catch (Exception e) {
            SlimPatch.LOGGER.error("[SlimPatch] Failed to load dialogues.json", e);
        }
    }

    private static final Random RAND = new Random();

    private static final Map<VillagerPersonality, Map<String, Integer>> PERSONALITY_MODIFIERS = Map.ofEntries(
            Map.entry(VillagerPersonality.FRIENDLY, Map.of("Friendly", +20, "Mean", -20, "Joke", +10, "Flirt", 0)),
            Map.entry(VillagerPersonality.MEAN, Map.of("Friendly", -20, "Mean", +20, "Joke", -10, "Flirt", -10)),
            Map.entry(VillagerPersonality.SHY, Map.of("Friendly", +15, "Mean", -15, "Joke", +10, "Flirt", -10)),
            Map.entry(VillagerPersonality.BRAVE, Map.of("Friendly", +10, "Mean", +10, "Joke", 0, "Flirt", +10)),
            Map.entry(VillagerPersonality.GRUMPY, Map.of("Friendly", -20, "Mean", +15, "Joke", -15, "Flirt", -10)),
            Map.entry(VillagerPersonality.GREEDY, Map.of("Friendly", 0, "Mean", -10, "Joke", 0, "Flirt", +10)),
            Map.entry(VillagerPersonality.ROMANTIC, Map.of("Friendly", +10, "Mean", -20, "Joke", +10, "Flirt", +25)),
            Map.entry(VillagerPersonality.WISE, Map.of("Friendly", +10, "Mean", -10, "Joke", +10, "Flirt", -5))
    );

    public static boolean calculateSuccess(VillagerPersonality personality, String option) {
        if (personality == null || option == null) return true;

        if (option.equalsIgnoreCase("Mean") &&
                personality != VillagerPersonality.GRUMPY &&
                personality != VillagerPersonality.MEAN) {
            return false;
        }

        int baseChance = 70;
        int modifier = PERSONALITY_MODIFIERS
                .getOrDefault(personality, Collections.emptyMap())
                .getOrDefault(option, 0);
        int finalChance = Math.max(15, Math.min(95, baseChance + modifier));

        int roll = RAND.nextInt(100) + 1;
        return roll <= finalChance;
    }

    @SuppressWarnings("unchecked")
    public static String getRandomLine(String category, Villager villager, boolean success) {
        if (villager == null || category == null) return "...";

        VillagerPersonality personality = null;
        if (villager instanceof MaleVillagerEntity male) {
            personality = male.getPersonality();
        } else if (villager instanceof FemaleVillagerEntity female) {
            personality = female.getPersonality();
        }

        if (personality == null) {
            SlimPatch.LOGGER.warn("[SlimPatch] Villager without personality in category '{}'", category);
            return "...";
        }

        Map<String, Object> byPersonality = DIALOGUES.get(category);
        if (byPersonality == null) {
            SlimPatch.LOGGER.warn("[SlimPatch] No dialogue category '{}'", category);
            return "...";
        }

        Object block = byPersonality.get(personality.name().toLowerCase(Locale.ROOT));
        if (block == null) {
            SlimPatch.LOGGER.warn("[SlimPatch] No lines for category '{}' and personality '{}'", category, personality);
            return "...";
        }

        if (block instanceof List<?>) {
            List<String> lines = (List<String>) block;
            return lines.isEmpty() ? "..." : lines.get(RAND.nextInt(lines.size()));
        } else if (block instanceof Map<?, ?> map) {
            List<String> chosen;

            if (!success && map.containsKey("fail")) {
                Object failObj = map.get("fail");
                if (failObj instanceof List<?>) {
                    chosen = (List<String>) failObj;
                } else if (failObj instanceof String single) {
                    chosen = List.of(single);
                } else {
                    chosen = new ArrayList<>();
                }
            } else {
                Object lineObj = map.get("lines");
                if (lineObj instanceof List<?>) {
                    chosen = (List<String>) lineObj;
                } else if (lineObj instanceof String singleLine) {
                    chosen = List.of(singleLine);
                } else {
                    Object maybeList = map.get("default");
                    if (maybeList instanceof List<?>) {
                        chosen = (List<String>) maybeList;
                    } else if (maybeList instanceof String singleAlt) {
                        chosen = List.of(singleAlt);
                    } else {
                        chosen = new ArrayList<>();
                    }
                }
            }

            if (chosen == null || chosen.isEmpty()) return "...";
            return chosen.get(RAND.nextInt(chosen.size()));
        }

        return "...";
    }

    public static String getRandomLine(String category, Villager villager) {
        return getRandomLine(category, villager, true);
    }

    public static float getRelationshipChange(VillagerPersonality personality, String option, boolean success) {
        if (option == null) return 0.0f;

        if (option.equalsIgnoreCase("Mean") &&
                personality != VillagerPersonality.GRUMPY &&
                personality != VillagerPersonality.MEAN) {
            return -0.5f;
        }

        if (success) {
            if (option.equalsIgnoreCase("Flirt")) return +0.7f;
            return +0.5f;
        } else {
            if (option.equalsIgnoreCase("Flirt")) return -0.4f;
            return -0.3f;
        }
    }
}