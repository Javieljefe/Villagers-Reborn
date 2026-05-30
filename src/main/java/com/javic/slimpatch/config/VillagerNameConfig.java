package com.javic.slimpatch.config;

import com.mojang.logging.LogUtils;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VillagerNameConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("villagersreborn");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("names.toml");
    private static final List<String> DEFAULT_MALE_NAMES = List.of(
            "Adam", "Adrian", "Alexander", "Alfred", "Andrew",
            "Anthony", "Arthur", "Benjamin", "Blake", "Brandon",
            "Brian", "Caleb", "Cameron", "Carl", "Charles",
            "Christian", "Christopher", "Colin", "Connor", "Daniel",
            "David", "Dean", "Dominic", "Dylan", "Edward",
            "Elias", "Elijah", "Elliot", "Ethan", "Evan",
            "Felix", "Finn", "Francis", "Gabriel", "Gavin",
            "George", "Grant", "Gregory", "Harold", "Harrison",
            "Harry", "Henry", "Hugh", "Ian", "Isaac",
            "Jack", "Jacob", "James", "Jason", "Jeremy",
            "Joel", "John", "Jonathan", "Joseph", "Joshua",
            "Julian", "Keith", "Kenneth", "Kevin", "Kyle",
            "Liam", "Logan", "Louis", "Lucas", "Luke",
            "Malcolm", "Marcus", "Mark", "Martin", "Matthew",
            "Michael", "Nathan", "Nicholas", "Noah", "Oliver",
            "Oscar", "Owen", "Patrick", "Paul", "Peter",
            "Philip", "Raymond", "Richard", "Robert", "Ryan",
            "Samuel", "Scott", "Sean", "Simon", "Stephen",
            "Theodore", "Thomas", "Timothy", "Tristan", "Victor",
            "Vincent", "Walter", "William", "Zachary", "Ezekiel"
    );
    private static final List<String> DEFAULT_FEMALE_NAMES = List.of(
            "Abigail", "Ada", "Adeline", "Adele", "Alice",
            "Amelia", "Amy", "Anna", "Annabelle", "Audrey",
            "Autumn", "Ava", "Beatrice", "Bella", "Bethany",
            "Bianca", "Bonnie", "Brianna", "Camilla", "Caroline",
            "Catherine", "Charlotte", "Chloe", "Clara", "Claudia",
            "Daisy", "Diana", "Eleanor", "Elena", "Eliza",
            "Elizabeth", "Ella", "Ellie", "Emily", "Emma",
            "Erin", "Evelyn", "Faith", "Fiona", "Florence",
            "Calia", "Freya", "Gabriella", "Georgia", "Grace",
            "Hailey", "Hannah", "Hazel", "Heather", "Holly",
            "Irene", "Iris", "Isabel", "Isabella", "Jade",
            "Jane", "Jenna", "Jessica", "Joanna", "Josephine",
            "Julia", "June", "Katherine", "Katie", "Kayla",
            "Laura", "Lauren", "Leah", "Lily", "Lucy",
            "Lydia", "Mabel", "Madeline", "Margaret", "Maria",
            "Martha", "Mary", "Matilda", "Megan", "Mia",
            "Molly", "Naomi", "Natalie", "Nora", "Olivia",
            "Paige", "Rachel", "Rebecca", "Rose", "Ruby",
            "Samantha", "Sarah", "Sophia", "Stella", "Summer",
            "Susan", "Vanessa", "Vera", "Victoria", "Violet"
    );
    private static volatile List<String> maleNames = DEFAULT_MALE_NAMES;
    private static volatile List<String> femaleNames = DEFAULT_FEMALE_NAMES;
    private static volatile long lastLoadedMillis = Long.MIN_VALUE;

    private VillagerNameConfig() {
    }

    public static void ensureLoaded() {
        loadIfNeeded();
    }

    public static String getRandomMaleName(RandomSource random) {
        List<String> names = getMaleNames();
        return names.get(random.nextInt(names.size()));
    }

    public static String getRandomFemaleName(RandomSource random) {
        List<String> names = getFemaleNames();
        return names.get(random.nextInt(names.size()));
    }

    private static List<String> getMaleNames() {
        loadIfNeeded();
        return maleNames;
    }

    private static List<String> getFemaleNames() {
        loadIfNeeded();
        return femaleNames;
    }

    private static synchronized void loadIfNeeded() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (Files.notExists(CONFIG_FILE)) {
                Files.writeString(CONFIG_FILE, buildDefaultConfig(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }

            long modifiedMillis = Files.getLastModifiedTime(CONFIG_FILE).toMillis();
            if (modifiedMillis == lastLoadedMillis) {
                return;
            }

            String content = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);
            maleNames = parseNames(content, "maleNames", DEFAULT_MALE_NAMES);
            femaleNames = parseNames(content, "femaleNames", DEFAULT_FEMALE_NAMES);
            lastLoadedMillis = modifiedMillis;
        } catch (Exception e) {
            maleNames = DEFAULT_MALE_NAMES;
            femaleNames = DEFAULT_FEMALE_NAMES;
            lastLoadedMillis = Long.MIN_VALUE;
            LOGGER.warn("Failed to load villager names from {}. Using built-in defaults.", CONFIG_FILE, e);
        }
    }

    private static List<String> parseNames(String content, String key, List<String> fallback) {
        Pattern pattern = Pattern.compile("(?ms)^\\s*" + Pattern.quote(key) + "\\s*=\\s*\\[(.*?)]");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            LOGGER.warn("Missing {} in {}. Using built-in defaults.", key, CONFIG_FILE);
            return fallback;
        }

        List<String> parsed = new ArrayList<>();
        Matcher valueMatcher = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(matcher.group(1));
        while (valueMatcher.find()) {
            String value = unescape(valueMatcher.group(1)).trim();
            if (!value.isEmpty()) {
                parsed.add(value);
            }
        }

        if (parsed.isEmpty()) {
            LOGGER.warn("{} in {} is empty or invalid. Using built-in defaults.", key, CONFIG_FILE);
            return fallback;
        }

        return List.copyOf(parsed);
    }

    private static String buildDefaultConfig() {
        return "maleNames = [" + formatNames(DEFAULT_MALE_NAMES) + "]\n"
                + "femaleNames = [" + formatNames(DEFAULT_FEMALE_NAMES) + "]\n";
    }

    private static String formatNames(List<String> names) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append('"').append(escape(names.get(i))).append('"');
        }
        return builder.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaping) {
                builder.append(c);
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                builder.append(c);
            }
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }
}
