package com.javic.slimpatch.dialogue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import com.google.gson.reflect.TypeToken;
import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.javic.slimpatch.entity.VillagerPersonality;
import com.javic.slimpatch.entity.VillagerRelationshipData;
import com.javic.slimpatch.entity.VillagerRelationshipStage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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

    public static void endDialogue(Villager villager, net.minecraft.world.entity.player.Player player) {
        if (villager == null || player == null) {
            return;
        }

        UUID playerId = ACTIVE_DIALOGUES.get(villager.getUUID());
        if (player.getUUID().equals(playerId)) {
            ACTIVE_DIALOGUES.remove(villager.getUUID());
        }
    }

    public static void endDialoguesForPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }

        ACTIVE_DIALOGUES.entrySet().removeIf(entry -> playerId.equals(entry.getValue()));
    }

    public static boolean isInDialogue(Villager villager) {
        return villager != null && ACTIVE_DIALOGUES.containsKey(villager.getUUID());
    }

    public static UUID getDialoguePlayer(Villager villager) {
        return villager != null ? ACTIVE_DIALOGUES.get(villager.getUUID()) : null;
    }

    public static Map<UUID, UUID> getActiveDialogues() {
        return new HashMap<>(ACTIVE_DIALOGUES);
    }

    private static final Map<String, Map<String, Object>> DIALOGUES = new HashMap<>();
    private static final Map<String, Map<String, Object>> DATING_DIALOGUES = new HashMap<>();
    private static final Map<String, Map<String, Object>> MARRIED_DIALOGUES = new HashMap<>();
    private static final Map<String, Map<String, Object>> TODDLER_DIALOGUES = new HashMap<>();
    private static final Map<String, Map<String, Object>> CHILD_DIALOGUES = new HashMap<>();
    private static final Map<String, Map<String, Object>> TEEN_DIALOGUES = new HashMap<>();

    static {
        loadDialogues();
    }

    @SuppressWarnings("unchecked")
    private static void loadDialogues() {
        DIALOGUES.clear();
        DATING_DIALOGUES.clear();
        MARRIED_DIALOGUES.clear();
        TODDLER_DIALOGUES.clear();
        CHILD_DIALOGUES.clear();
        TEEN_DIALOGUES.clear();

        DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/dialogues.json"));
        DATING_DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/dating_dialogues.json"));
        MARRIED_DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/married_dialogues.json"));
        TODDLER_DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/toddler_dialogues.json"));
        CHILD_DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/child_dialogues.json"));
        TEEN_DIALOGUES.putAll(loadDialogueFile("assets/slimpatch/dialogues/teen_dialogues.json"));
    }

    private static Map<String, Map<String, Object>> loadDialogueFile(String path) {
        try {
            var resource = DialogueManager.class.getClassLoader().getResourceAsStream(path);
            if (resource == null) {
                return Collections.emptyMap();
            }

            InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> data = new LinkedHashMap<>();
            JsonStreamParser parser = new JsonStreamParser(reader);
            Gson gson = new Gson();
            while (parser.hasNext()) {
                JsonElement element = parser.next();
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                Map<String, Map<String, Object>> parsed = gson.fromJson((JsonObject) element, type);
                mergeDialogueMaps(data, parsed);
            }
            reader.close();

            if (!data.isEmpty()) {
                return data;
            }
        } catch (Exception e) {
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static void mergeDialogueMaps(Map<String, Map<String, Object>> target, Map<String, Map<String, Object>> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Map<String, Object>> entry : source.entrySet()) {
            Map<String, Object> targetSection = target.computeIfAbsent(entry.getKey(), ignored -> new LinkedHashMap<>());
            Map<String, Object> sourceSection = entry.getValue();
            if (sourceSection == null || sourceSection.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Object> personalityEntry : sourceSection.entrySet()) {
                Object sourceValue = personalityEntry.getValue();
                Object targetValue = targetSection.get(personalityEntry.getKey());
                if (targetValue instanceof Map<?, ?> targetMap && sourceValue instanceof Map<?, ?> sourceMap) {
                    ((Map<String, Object>) targetMap).putAll((Map<String, Object>) sourceMap);
                } else {
                    targetSection.put(personalityEntry.getKey(), sourceValue);
                }
            }
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
            return "...";
        }

        Map<String, Map<String, Object>> stageDialogues = getStageDialogues(villager);
        String personalityKey = personality.name().toLowerCase(Locale.ROOT);

        String stageLine = getRandomLineFromMap(stageDialogues, category, personalityKey, null, success);
        if (!stageLine.equals("...")) {
            return stageLine;
        }
        return getRandomLineFromMap(DIALOGUES, category, personalityKey, null, success);
    }

    public static String getRandomLine(String category, Villager villager) {
        return getRandomLine(category, villager, true);
    }

    public static String getRandomLine(String category, Villager villager, Player player, boolean success) {
        if (villager == null || category == null) return "...";

        VillagerPersonality personality = null;
        if (villager instanceof MaleVillagerEntity male) {
            personality = male.getPersonality();
        } else if (villager instanceof FemaleVillagerEntity female) {
            personality = female.getPersonality();
        }

        if (personality == null) {
            return "...";
        }

        Map<String, Map<String, Object>> stageDialogues = getStageDialogues(villager, player);
        String personalityKey = personality.name().toLowerCase(Locale.ROOT);
        String playerChildPersonalityKey = getPlayerChildPersonalityKey(villager, player, category, personalityKey);

        String stageLine = getRandomLineFromMap(stageDialogues, category, playerChildPersonalityKey, personalityKey, success);
        if (!stageLine.equals("...")) {
            return stageLine;
        }
        return getRandomLineFromMap(DIALOGUES, category, personalityKey, null, success);
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

    public static String getQuestIntro(String questId) {
        if (DIALOGUES == null || DIALOGUES.isEmpty()) return "...";

        String matchedKey = null;
        for (String key : DIALOGUES.keySet()) {
            if (key.equalsIgnoreCase("QuestIntro")) {
                matchedKey = key;
                break;
            }
        }
        if (matchedKey == null) return "...";

        Map<String, Object> questIntro = DIALOGUES.get(matchedKey);
        if (questIntro == null) return "...";

        Object questEntry = null;
        for (String key : questIntro.keySet()) {
            if (key.equalsIgnoreCase(questId)) {
                questEntry = questIntro.get(key);
                break;
            }
        }
        if (questEntry == null) return "...";

        List<String> lines = extractLinesRecursive(questEntry);
        if (lines == null || lines.isEmpty()) return "...";

        int index = RAND.nextInt(lines.size());
        Object line = lines.get(index);
        if (line instanceof String) {
            return (String) line;
        } else {
            return "...";
        }
    }

    public static String getGenericQuestLine(String category) {
        if (DIALOGUES == null || DIALOGUES.isEmpty()) return "...";

        String matchedKey = null;
        for (String key : DIALOGUES.keySet()) {
            if (key.equalsIgnoreCase(category)) {
                matchedKey = key;
                break;
            }
        }
        if (matchedKey == null) return "...";

        Map<String, Object> section = DIALOGUES.get(matchedKey);
        if (section == null) return "...";

        List<String> lines = extractLinesRecursive(section);
        if (lines == null || lines.isEmpty()) return "...";

        int index = RAND.nextInt(lines.size());
        Object line = lines.get(index);
        if (line instanceof String) {
            return (String) line;
        } else {
            return "...";
        }
    }

    private static List<String> extractLinesRecursive(Object node) {
        if (node instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) node;
            if (map.containsKey("lines")) {
                Object linesObj = map.get("lines");
                if (linesObj instanceof List<?>) {
                    List<?> list = (List<?>) linesObj;
                    List<String> result = new ArrayList<>();
                    for (Object obj : list) {
                        if (obj instanceof String) {
                            result.add((String) obj);
                        }
                    }
                    return result;
                }
            }
            for (Object value : map.values()) {
                List<String> found = extractLinesRecursive(value);
                if (found != null && !found.isEmpty()) return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getRandomLineFromMap(Map<String, Map<String, Object>> dialogues, String category, String personalityKey, String fallbackPersonalityKey, boolean success) {
        if (dialogues == null || dialogues.isEmpty()) {
            return "...";
        }

        Map<String, Object> byPersonality = getIgnoreCase(dialogues, category);
        if (byPersonality == null) {
            return "...";
        }

        Object block = getDialogueBlock(byPersonality, personalityKey, fallbackPersonalityKey);
        if (block == null) {
            return "...";
        }

        if (block instanceof List<?>) {
            List<String> lines = (List<String>) block;
            return lines.isEmpty() ? "..." : lines.get(RAND.nextInt(lines.size()));
        }
        if (block instanceof Map<?, ?> map) {
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

    private static Map<String, Map<String, Object>> getStageDialogues(Villager villager) {
        return getYoungStageDialogues(villager);
    }

    private static Map<String, Map<String, Object>> getStageDialogues(Villager villager, Player player) {
        Map<String, Map<String, Object>> youngStageDialogues = getYoungStageDialogues(villager);
        if (!youngStageDialogues.isEmpty()) {
            return youngStageDialogues;
        }
        if (villager instanceof FamilyVillager familyVillager) {
            if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.MARRIED) {
                if (player == null) {
                    return MARRIED_DIALOGUES;
                }
                if (!VillagerRelationshipData.usesPerPlayerRelationships(villager)) {
                    return MARRIED_DIALOGUES;
                }
                if (player.getUUID().equals(familyVillager.getSpousePlayerUuid())) {
                    return MARRIED_DIALOGUES;
                }
                return Collections.emptyMap();
            }

            if (player != null) {
                if (VillagerRelationshipData.usesPerPlayerRelationships(villager)) {
                    float goldenRelationship = villager.level().isClientSide()
                            ? VillagerRelationshipData.getDisplayedGoldenRelationship(villager, 0.0F)
                            : VillagerRelationshipData.getGoldenRelationshipForPlayer(villager, player.getUUID(), 0.0F);
                    if (goldenRelationship > 0.0F) {
                        return DATING_DIALOGUES;
                    }
                    return Collections.emptyMap();
                }
                float goldenRelationship = villager.level().isClientSide()
                        ? VillagerRelationshipData.getDisplayedGoldenRelationship(villager, familyVillager.getGoldenRelationship())
                        : VillagerRelationshipData.getGoldenRelationshipForPlayer(villager, player.getUUID(), familyVillager.getGoldenRelationship());
                if (goldenRelationship > 0.0F || familyVillager.getRelationshipStage() == VillagerRelationshipStage.DATING) {
                    return DATING_DIALOGUES;
                }
                return Collections.emptyMap();
            }

            if (familyVillager.getRelationshipStage() == VillagerRelationshipStage.DATING) {
                return DATING_DIALOGUES;
            }
        }
        return Collections.emptyMap();
    }

    private static Map<String, Map<String, Object>> getYoungStageDialogues(Villager villager) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return Collections.emptyMap();
        }
        return switch (familyVillager.getAgeStage()) {
            case TODDLER -> TODDLER_DIALOGUES.isEmpty() ? Collections.emptyMap() : TODDLER_DIALOGUES;
            case CHILD -> CHILD_DIALOGUES.isEmpty() ? Collections.emptyMap() : CHILD_DIALOGUES;
            case TEEN -> TEEN_DIALOGUES.isEmpty() ? Collections.emptyMap() : TEEN_DIALOGUES;
            case ADULT -> Collections.emptyMap();
        };
    }

    private static String getPlayerChildPersonalityKey(Villager villager, Player player, String category, String personalityKey) {
        if (!"Intro".equalsIgnoreCase(category) || personalityKey == null || personalityKey.isEmpty()) {
            return personalityKey;
        }
        if (!isYoungVillager(villager) || !isPlayerParent(villager, player)) {
            return personalityKey;
        }
        return personalityKey + "_player_child";
    }

    private static boolean isYoungVillager(Villager villager) {
        if (!(villager instanceof FamilyVillager familyVillager)) {
            return false;
        }
        VillagerAgeStage ageStage = familyVillager.getAgeStage();
        return ageStage == VillagerAgeStage.TODDLER || ageStage == VillagerAgeStage.CHILD || ageStage == VillagerAgeStage.TEEN;
    }

    private static boolean isPlayerParent(Villager villager, Player player) {
        if (!(villager instanceof FamilyVillager familyVillager) || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        UUID parentPlayerUuid = familyVillager.getParentPlayerUuid();
        return parentPlayerUuid != null && parentPlayerUuid.equals(serverPlayer.getUUID());
    }

    private static Object getDialogueBlock(Map<String, Object> byPersonality, String personalityKey, String fallbackPersonalityKey) {
        Object block = getIgnoreCase(byPersonality, personalityKey);
        if (block != null) {
            return block;
        }
        if (fallbackPersonalityKey != null && !fallbackPersonalityKey.equalsIgnoreCase(personalityKey)) {
            block = getIgnoreCase(byPersonality, fallbackPersonalityKey);
            if (block != null) {
                return block;
            }
        }
        return getIgnoreCase(byPersonality, "friendly");
    }

    private static <T> T getIgnoreCase(Map<String, T> map, String key) {
        if (map == null || map.isEmpty() || key == null) {
            return null;
        }
        T exact = map.get(key);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
