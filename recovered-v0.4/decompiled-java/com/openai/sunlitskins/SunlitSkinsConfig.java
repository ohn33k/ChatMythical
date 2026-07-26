/*
 * Decompiled with CFR 0.152.
 */
package com.openai.sunlitskins;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class SunlitSkinsConfig {
    private static final Path PATH = Path.of("config", "sunlit-compatible-skins.toml");
    private static volatile SunlitSkinsConfig CURRENT = SunlitSkinsConfig.defaults();
    private static volatile long lastChecked;
    private static volatile long lastModified;
    boolean assignToWildPokemon = true;
    double normalChancePercent = 50.0;
    double legendaryNormalChancePercent = 50.0;
    boolean includeTextureVariants = true;
    boolean includeShaderEffects = true;
    Set<String> enabledFamilies = Set.of("*");
    boolean legendaryEnabled = true;
    int minimumOnlineMinutes = 60;
    int maximumOnlineMinutes = 240;
    int checkIntervalMinutes = 5;
    double initialCheckChancePercent = 2.0;
    double maximumCheckChancePercent = 25.0;
    boolean announceSpawns = true;
    boolean includeNearbyPlayerName = true;
    boolean includeCoordinates = false;
    int levelMin = 50;
    int levelMax = 70;
    final Map<String, Double> speciesWeights = new ConcurrentHashMap<String, Double>();

    SunlitSkinsConfig() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static SunlitSkinsConfig get() {
        long l = System.currentTimeMillis();
        if (l - lastChecked <= 30000L) return CURRENT;
        Class<SunlitSkinsConfig> clazz = SunlitSkinsConfig.class;
        synchronized (SunlitSkinsConfig.class) {
            if (l - lastChecked <= 30000L) return CURRENT;
            lastChecked = l;
            SunlitSkinsConfig.reloadIfChanged();
            // ** MonitorExit[var2_1] (shouldn't be in output)
            return CURRENT;
        }
    }

    private static SunlitSkinsConfig defaults() {
        return new SunlitSkinsConfig();
    }

    private static void reloadIfChanged() {
        try {
            SunlitSkinsConfig sunlitSkinsConfig;
            long l;
            if (!Files.exists(PATH, new LinkOption[0])) {
                Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
                Files.writeString(PATH, (CharSequence)SunlitSkinsConfig.template(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
            if ((l = Files.getLastModifiedTime(PATH, new LinkOption[0]).toMillis()) == lastModified) {
                return;
            }
            CURRENT = sunlitSkinsConfig = SunlitSkinsConfig.parse(Files.readAllLines(PATH, StandardCharsets.UTF_8));
            lastModified = l;
            System.out.println("[SunlitCompatibleSkins] Loaded " + String.valueOf(PATH) + ". Wild appearances=" + sunlitSkinsConfig.assignToWildPokemon + ", normal chance=" + sunlitSkinsConfig.normalChancePercent + "%, legendary timer=" + sunlitSkinsConfig.legendaryEnabled + ".");
        }
        catch (Throwable throwable) {
            System.err.println("[SunlitCompatibleSkins] Could not load config; retaining previous values: " + String.valueOf(throwable));
        }
    }

    private static SunlitSkinsConfig parse(List<String> list) {
        SunlitSkinsConfig sunlitSkinsConfig = SunlitSkinsConfig.defaults();
        String string = "";
        for (String string2 : list) {
            String string3 = string2;
            int n = string3.indexOf(35);
            if (n >= 0) {
                string3 = string3.substring(0, n);
            }
            if ((string3 = string3.trim()).isEmpty()) continue;
            if (string3.startsWith("[") && string3.endsWith("]")) {
                string = string3.substring(1, string3.length() - 1).trim().toLowerCase(Locale.ROOT);
                continue;
            }
            int n2 = string3.indexOf(61);
            if (n2 < 1) continue;
            String string4 = SunlitSkinsConfig.unquote(string3.substring(0, n2).trim()).toLowerCase(Locale.ROOT);
            String string5 = SunlitSkinsConfig.unquote(string3.substring(n2 + 1).trim());
            try {
                switch (string) {
                    case "appearance_spawning": {
                        SunlitSkinsConfig.parseAppearance(sunlitSkinsConfig, string4, string5);
                        break;
                    }
                    case "legendary_spawning": {
                        SunlitSkinsConfig.parseLegendary(sunlitSkinsConfig, string4, string5);
                        break;
                    }
                    case "legendary_species_weights": {
                        sunlitSkinsConfig.speciesWeights.put(string4.replace("cobblemon:", ""), Math.max(0.0, Double.parseDouble(string5)));
                        break;
                    }
                }
            }
            catch (RuntimeException runtimeException) {
                System.err.println("[SunlitCompatibleSkins] Ignoring invalid config value: " + string2);
            }
        }
        if (sunlitSkinsConfig.maximumOnlineMinutes < sunlitSkinsConfig.minimumOnlineMinutes) {
            sunlitSkinsConfig.maximumOnlineMinutes = sunlitSkinsConfig.minimumOnlineMinutes;
        }
        if (sunlitSkinsConfig.levelMax < sunlitSkinsConfig.levelMin) {
            sunlitSkinsConfig.levelMax = sunlitSkinsConfig.levelMin;
        }
        return sunlitSkinsConfig;
    }

    private static void parseAppearance(SunlitSkinsConfig sunlitSkinsConfig, String string, String string2) {
        switch (string) {
            case "enabled": {
                sunlitSkinsConfig.assignToWildPokemon = Boolean.parseBoolean(string2);
                break;
            }
            case "normal_chance_percent": {
                sunlitSkinsConfig.normalChancePercent = SunlitSkinsConfig.clampDouble(string2, 0.0, 100.0);
                break;
            }
            case "legendary_normal_chance_percent": {
                sunlitSkinsConfig.legendaryNormalChancePercent = SunlitSkinsConfig.clampDouble(string2, 0.0, 100.0);
                break;
            }
            case "include_texture_variants": {
                sunlitSkinsConfig.includeTextureVariants = Boolean.parseBoolean(string2);
                break;
            }
            case "include_shader_effects": {
                sunlitSkinsConfig.includeShaderEffects = Boolean.parseBoolean(string2);
                break;
            }
            case "enabled_variant_families": {
                LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
                for (String string3 : string2.split(",")) {
                    if (string3.isBlank()) continue;
                    linkedHashSet.add(string3.trim().toLowerCase(Locale.ROOT));
                }
                sunlitSkinsConfig.enabledFamilies = linkedHashSet.isEmpty() ? Set.of("*") : Collections.unmodifiableSet(linkedHashSet);
                break;
            }
        }
    }

    private static void parseLegendary(SunlitSkinsConfig sunlitSkinsConfig, String string, String string2) {
        switch (string) {
            case "enabled": {
                sunlitSkinsConfig.legendaryEnabled = Boolean.parseBoolean(string2);
                break;
            }
            case "minimum_online_minutes": {
                sunlitSkinsConfig.minimumOnlineMinutes = SunlitSkinsConfig.clampInt(string2, 1, 10080);
                break;
            }
            case "maximum_online_minutes": {
                sunlitSkinsConfig.maximumOnlineMinutes = SunlitSkinsConfig.clampInt(string2, 1, 10080);
                break;
            }
            case "check_interval_minutes": {
                sunlitSkinsConfig.checkIntervalMinutes = SunlitSkinsConfig.clampInt(string2, 1, 240);
                break;
            }
            case "initial_check_chance_percent": {
                sunlitSkinsConfig.initialCheckChancePercent = SunlitSkinsConfig.clampDouble(string2, 0.0, 100.0);
                break;
            }
            case "maximum_check_chance_percent": {
                sunlitSkinsConfig.maximumCheckChancePercent = SunlitSkinsConfig.clampDouble(string2, 0.0, 100.0);
                break;
            }
            case "announce_spawns": {
                sunlitSkinsConfig.announceSpawns = Boolean.parseBoolean(string2);
                break;
            }
            case "include_nearby_player_name": {
                sunlitSkinsConfig.includeNearbyPlayerName = Boolean.parseBoolean(string2);
                break;
            }
            case "include_coordinates": {
                sunlitSkinsConfig.includeCoordinates = Boolean.parseBoolean(string2);
                break;
            }
            case "level_min": {
                sunlitSkinsConfig.levelMin = SunlitSkinsConfig.clampInt(string2, 1, 100);
                break;
            }
            case "level_max": {
                sunlitSkinsConfig.levelMax = SunlitSkinsConfig.clampInt(string2, 1, 100);
                break;
            }
        }
    }

    double weightFor(String string, double d) {
        return this.speciesWeights.getOrDefault(string, d);
    }

    boolean familyEnabled(String string) {
        if (this.enabledFamilies.contains("*")) {
            return true;
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        return this.enabledFamilies.contains(string2) || this.enabledFamilies.contains(string2.replace("mythical_", ""));
    }

    private static String unquote(String string) {
        if ((string = string.trim()).length() >= 2 && (string.startsWith("\"") && string.endsWith("\"") || string.startsWith("'") && string.endsWith("'"))) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private static int clampInt(String string, int n, int n2) {
        return Math.max(n, Math.min(n2, Integer.parseInt(string)));
    }

    private static double clampDouble(String string, double d, double d2) {
        return Math.max(d, Math.min(d2, Double.parseDouble(string)));
    }

    static String template() {
        return "# Sunlit Compatible Skins server configuration.\n# Changes reload automatically within about 30 seconds.\n\n[appearance_spawning]\n# Assign a valid appearance to naturally created wild Pokemon.\nenabled = true\n# The remaining percentage is divided evenly among valid enabled appearances.\nnormal_chance_percent = 50.0\nlegendary_normal_chance_percent = 50.0\ninclude_texture_variants = true\ninclude_shader_effects = true\n# Comma-separated names such as blazing,twilight,galaxy, or * for everything.\nenabled_variant_families = \"*\"\n\n[legendary_spawning]\nenabled = true\n# The timer advances only while at least one player is online.\nminimum_online_minutes = 60\nmaximum_online_minutes = 240\ncheck_interval_minutes = 5\ninitial_check_chance_percent = 2.0\nmaximum_check_chance_percent = 25.0\nannounce_spawns = true\ninclude_nearby_player_name = true\ninclude_coordinates = false\nlevel_min = 50\nlevel_max = 70\n\n[legendary_species_weights]\n# Optional relative weights. Zero disables a species.\n# Native supported scheduler species: articuno, ironleaves, mew, mewtwo,\n# moltres, rayquaza, walkingwake, xerneas, and zapdos.\n# mew = 0.25\n# moltres = 1.0\n";
    }

    static {
        lastModified = Long.MIN_VALUE;
    }
}

