/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package com.openai.sunlitskins;

import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class SpeciesFilteredSuggestions {
    private static final Map<String, Set<String>> ALLOWED = SpeciesFilteredSuggestions.loadAllowed();
    private static volatile boolean errorLogged;

    private SpeciesFilteredSuggestions() {
    }

    public static List<String> initialKeys(PropertiesCompletionProvider propertiesCompletionProvider) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string : propertiesCompletionProvider.keys()) {
            if (SpeciesFilteredSuggestions.normalizedKey(string).startsWith("mythical_")) continue;
            arrayList.add(string);
        }
        return arrayList;
    }

    public static CompletableFuture<Suggestions> suggestKeys(PropertiesCompletionProvider propertiesCompletionProvider, String string, Collection<String> collection, SuggestionsBuilder suggestionsBuilder) {
        Set<String> set;
        String string2 = SpeciesFilteredSuggestions.findSpecies(suggestionsBuilder.getInput(), suggestionsBuilder.getStart());
        Set<String> set2 = set = string2 == null ? null : ALLOWED.get(string2);
        if (set == null) {
            return propertiesCompletionProvider.suggestKeys(string, collection, suggestionsBuilder);
        }
        SuggestionsBuilder suggestionsBuilder2 = new SuggestionsBuilder(suggestionsBuilder.getInput(), suggestionsBuilder.getStart());
        return propertiesCompletionProvider.suggestKeys(string, collection, suggestionsBuilder2).thenApply(suggestions -> {
            for (Suggestion suggestion : suggestions.getList()) {
                String string = suggestion.getText();
                String string2 = SpeciesFilteredSuggestions.normalizedKey(string);
                if (string2.startsWith("mythical_") && !set.contains(string2)) continue;
                suggestionsBuilder.suggest(string);
            }
            return suggestionsBuilder.build();
        });
    }

    private static String normalizedKey(String string) {
        int n;
        String string2 = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
        int n2 = string2.lastIndexOf(32);
        if (n2 >= 0) {
            string2 = string2.substring(n2 + 1);
        }
        if ((n = string2.indexOf(61)) >= 0) {
            string2 = string2.substring(0, n);
        }
        return string2;
    }

    private static String findSpecies(String string, int n) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String[] stringArray = string.trim().split("\\s+");
        for (int i = stringArray.length - 1; i >= 0; --i) {
            String string2 = stringArray[i].trim().toLowerCase(Locale.ROOT);
            if (string2.isEmpty() || string2.indexOf(61) >= 0) continue;
            int n2 = string2.indexOf(58);
            if (n2 >= 0) {
                string2 = string2.substring(n2 + 1);
            }
            if (!ALLOWED.containsKey(string2)) continue;
            return string2;
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Map<String, Set<String>> loadAllowed() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try (InputStream inputStream = SpeciesFilteredSuggestions.class.getClassLoader().getResourceAsStream("META-INF/sunlitcompatible/appearance-variants.tsv");){
            if (inputStream == null) {
                Map<String, Set<String>> map = Collections.emptyMap();
                return map;
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    String[] stringArray;
                    if (string.isBlank() || string.startsWith("#") || (stringArray = string.split("\\t", 2)).length != 2) continue;
                    LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
                    for (String string2 : stringArray[1].split(",")) {
                        String[] stringArray2 = string2.split(":", 2);
                        if (stringArray2.length <= 0 || !stringArray2[0].startsWith("mythical_")) continue;
                        linkedHashSet.add(stringArray2[0].toLowerCase(Locale.ROOT));
                    }
                    linkedHashMap.put(stringArray[0].toLowerCase(Locale.ROOT), Collections.unmodifiableSet(linkedHashSet));
                }
                return Collections.unmodifiableMap(linkedHashMap);
            }
        }
        catch (Throwable throwable) {
            SpeciesFilteredSuggestions.report(throwable);
        }
        return Collections.unmodifiableMap(linkedHashMap);
    }

    private static void report(Throwable throwable) {
        if (!errorLogged) {
            errorLogged = true;
            System.err.println("[SunlitCompatibleSkins] Could not load species-filtered autocomplete: " + String.valueOf(throwable));
            throwable.printStackTrace(System.err);
        }
    }
}

