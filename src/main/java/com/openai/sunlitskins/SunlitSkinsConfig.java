package com.openai.sunlitskins;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

final class SunlitSkinsConfig {
    private static final Path PATH = Path.of("config", "sunlit-compatible-skins.toml");
    private static volatile SunlitSkinsConfig CURRENT = defaults();
    private static volatile long lastChecked;
    private static volatile long lastModified = Long.MIN_VALUE;

    boolean assignToWildPokemon = false;
    double normalChancePercent = 75.0;
    boolean includeTextureVariants = true;
    boolean includeShaderEffects = true;
    Set<String> enabledFamilies = Set.of("*");

    static SunlitSkinsConfig get() {
        long now = System.currentTimeMillis();
        if (now-lastChecked > 30_000L) synchronized (SunlitSkinsConfig.class) {
            if (now-lastChecked > 30_000L) { lastChecked=now; reloadIfChanged(); }
        }
        return CURRENT;
    }

    private static SunlitSkinsConfig defaults() { return new SunlitSkinsConfig(); }

    private static void reloadIfChanged() {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH.getParent());
                Files.writeString(PATH, template(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
            long modified=Files.getLastModifiedTime(PATH).toMillis();
            if(modified==lastModified)return;
            CURRENT=parse(Files.readAllLines(PATH,StandardCharsets.UTF_8));
            lastModified=modified;
            System.out.println("[SunlitCompatibleSkins] Loaded "+PATH+". Wild assignment="+CURRENT.assignToWildPokemon+".");
        } catch(Throwable t) {
            System.err.println("[SunlitCompatibleSkins] Could not load config; retaining previous values: "+t);
        }
    }

    private static SunlitSkinsConfig parse(List<String> lines) {
        SunlitSkinsConfig c=defaults(); String section="";
        for(String raw:lines) {
            String line=raw; int hash=line.indexOf('#'); if(hash>=0)line=line.substring(0,hash); line=line.trim();
            if(line.isEmpty())continue;
            if(line.startsWith("[")&&line.endsWith("]")){section=line.substring(1,line.length()-1).trim().toLowerCase(Locale.ROOT);continue;}
            int eq=line.indexOf('='); if(eq<1)continue;
            String key=unquote(line.substring(0,eq).trim()).toLowerCase(Locale.ROOT);
            String value=unquote(line.substring(eq+1).trim());
            if(!section.equals("appearance_spawning"))continue;
            try {
                switch(key) {
                    case "enabled" -> c.assignToWildPokemon=Boolean.parseBoolean(value);
                    case "normal_chance_percent" -> c.normalChancePercent=Math.max(0,Math.min(100,Double.parseDouble(value)));
                    case "include_texture_variants" -> c.includeTextureVariants=Boolean.parseBoolean(value);
                    case "include_shader_effects" -> c.includeShaderEffects=Boolean.parseBoolean(value);
                    case "enabled_variant_families" -> {
                        LinkedHashSet<String>s=new LinkedHashSet<>();
                        for(String v:value.split(","))if(!v.isBlank())s.add(v.trim().toLowerCase(Locale.ROOT));
                        c.enabledFamilies=s.isEmpty()?Set.of("*"):Collections.unmodifiableSet(s);
                    }
                }
            } catch(RuntimeException ignored) { System.err.println("[SunlitCompatibleSkins] Ignoring invalid config value: "+raw); }
        }
        return c;
    }

    boolean familyEnabled(String aspect) {
        if(enabledFamilies.contains("*"))return true;
        String s=aspect.toLowerCase(Locale.ROOT);
        return enabledFamilies.contains(s)||enabledFamilies.contains(s.replace("mythical_",""));
    }

    private static String unquote(String s) {
        s=s.trim(); if(s.length()>=2&&((s.startsWith("\"")&&s.endsWith("\""))||(s.startsWith("'")&&s.endsWith("'"))))return s.substring(1,s.length()-1); return s;
    }

    private static String template() {
        return "# Sunlit Compatible Skins. Changes reload automatically within about 30 seconds.\n"
            + "[appearance_spawning]\n"
            + "# Disabled by default while the compatibility set is being tested. The grid command still works.\n"
            + "enabled = false\n"
            + "normal_chance_percent = 75.0\n"
            + "include_texture_variants = true\n"
            + "include_shader_effects = true\n"
            + "enabled_variant_families = \"*\"\n";
    }
}
