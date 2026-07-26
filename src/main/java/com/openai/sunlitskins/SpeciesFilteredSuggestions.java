package com.openai.sunlitskins;

import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Filters only the custom mythical_* completion keys by the species already typed. */
public final class SpeciesFilteredSuggestions {
    private static final Map<String, Set<String>> ALLOWED = loadAllowed();
    private static volatile boolean errorLogged;

    private SpeciesFilteredSuggestions() {}


    /** Hide custom appearance flags until a species has been entered. */
    public static List<String> initialKeys(PropertiesCompletionProvider provider) {
        ArrayList<String> result = new ArrayList<>();
        for (String key : provider.keys()) {
            if (!normalizedKey(key).startsWith("mythical_")) result.add(key);
        }
        return result;
    }

    public static CompletableFuture<Suggestions> suggestKeys(
        PropertiesCompletionProvider provider,
        String partialKey,
        Collection<String> excludedKeys,
        SuggestionsBuilder builder
    ) {
        String species = findSpecies(builder.getInput(), builder.getStart());
        Set<String> allowed = species == null ? null : ALLOWED.get(species);
        if (allowed == null) {
            return provider.suggestKeys(partialKey, excludedKeys, builder);
        }

        // Use a temporary builder so global mythical suggestions can be discarded
        // without touching all of Cobblemon's ordinary property completions.
        SuggestionsBuilder temporary = new SuggestionsBuilder(builder.getInput(), builder.getStart());
        return provider.suggestKeys(partialKey, excludedKeys, temporary).thenApply(suggestions -> {
            for (Suggestion suggestion : suggestions.getList()) {
                String text = suggestion.getText();
                String key = normalizedKey(text);
                if (!key.startsWith("mythical_") || allowed.contains(key)) {
                    builder.suggest(text);
                }
            }
            return builder.build();
        });
    }

    private static String normalizedKey(String suggestion) {
        String value = suggestion == null ? "" : suggestion.trim().toLowerCase(Locale.ROOT);
        int space = value.lastIndexOf(' ');
        if (space >= 0) value = value.substring(space + 1);
        int eq = value.indexOf('=');
        if (eq >= 0) value = value.substring(0, eq);
        return value;
    }

    private static String findSpecies(String input, int completionStart) {
        if (input == null || input.isBlank()) return null;
        String[] tokens = input.trim().split("\\s+");
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i].trim().toLowerCase(Locale.ROOT);
            if (token.isEmpty() || token.indexOf('=') >= 0) continue;
            int colon = token.indexOf(':');
            if (colon >= 0) token = token.substring(colon + 1);
            if (ALLOWED.containsKey(token)) return token;
        }
        return null;
    }

    private static Map<String, Set<String>> loadAllowed() {
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap<>();
        try (InputStream in = SpeciesFilteredSuggestions.class.getClassLoader()
            .getResourceAsStream("META-INF/sunlitcompatible/appearance-variants.tsv")) {
            if (in == null) return Collections.emptyMap();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\t", 2);
                    if (parts.length != 2) continue;
                    LinkedHashSet<String> variants = new LinkedHashSet<>();
                    for (String token : parts[1].split(",")) {
                        String[] fields = token.split(":", 2);
                        if (fields.length > 0 && fields[0].startsWith("mythical_")) {
                            variants.add(fields[0].toLowerCase(Locale.ROOT));
                        }
                    }
                    result.put(parts[0].toLowerCase(Locale.ROOT), Collections.unmodifiableSet(variants));
                }
            }
        } catch (Throwable error) {
            report(error);
        }
        return Collections.unmodifiableMap(result);
    }

    private static void report(Throwable error) {
        if (!errorLogged) {
            errorLogged = true;
            System.err.println("[SunlitCompatibleSkins] Could not load species-filtered autocomplete: " + error);
            error.printStackTrace(System.err);
        }
    }
}
