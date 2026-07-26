/*
 * Decompiled with CFR 0.152.
 */
package com.openai.sunlitskins;

import com.openai.sunlitskins.ReflectionUtil;
import com.openai.sunlitskins.SunlitSkinsConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class AppearanceManager {
    public static final String TAG = "sunlitcompatible_appearance";
    private static final String LEGACY_TAG = "mythicalbackport_appearance";
    private static final Map<String, List<Variant>> VARIANTS = AppearanceManager.loadVariants();
    private static final Set<String> LEGENDARIES = AppearanceManager.loadLegendaryNames();
    private static final Set<String> RECOGNIZED = AppearanceManager.recognized();
    private static volatile boolean errorLogged;

    private AppearanceManager() {
    }

    public static void onAssigned(Object object, Object object2) {
        if (object == null || object2 == null) {
            return;
        }
        try {
            double d;
            Object object3;
            String string;
            if (AppearanceManager.isClient(object)) {
                AppearanceManager.restorePersistentAspect(object2);
                return;
            }
            String string2 = AppearanceManager.readPersistent(object2);
            if (!string2.isBlank()) {
                AppearanceManager.addAspects(object2, string2);
                return;
            }
            Set<String> set = AppearanceManager.aspects(object2);
            ArrayList<String> arrayList = new ArrayList<String>();
            for (String string3 : set) {
                string = AppearanceManager.normalize(string3);
                if (!RECOGNIZED.contains(string)) continue;
                arrayList.add(string);
            }
            if (!arrayList.isEmpty()) {
                object3 = String.join((CharSequence)",", arrayList);
                AppearanceManager.writePersistent(object2, (String)object3);
                AppearanceManager.addAspects(object2, (String)object3);
                return;
            }
            object3 = SunlitSkinsConfig.get();
            if (!((SunlitSkinsConfig)object3).assignToWildPokemon) {
                return;
            }
            boolean bl = (Boolean)ReflectionUtil.method(object2.getClass(), "isWild", new String[]{"isWild"}, 0).invoke(object2, new Object[0]);
            if (!bl) {
                return;
            }
            string = AppearanceManager.speciesName(object2);
            List list = VARIANTS.getOrDefault(string, List.of());
            if (list.isEmpty()) {
                return;
            }
            ArrayList<Variant> arrayList2 = new ArrayList<Variant>();
            for (Variant variant : list) {
                if (variant.shader && !((SunlitSkinsConfig)object3).includeShaderEffects || !variant.shader && !((SunlitSkinsConfig)object3).includeTextureVariants || !((SunlitSkinsConfig)object3).familyEnabled(variant.aspect)) continue;
                arrayList2.add(variant);
            }
            if (arrayList2.isEmpty()) {
                return;
            }
            double d2 = d = LEGENDARIES.contains(string) ? ((SunlitSkinsConfig)object3).legendaryNormalChancePercent : ((SunlitSkinsConfig)object3).normalChancePercent;
            if (ThreadLocalRandom.current().nextDouble(100.0) < d) {
                return;
            }
            Variant variant = (Variant)arrayList2.get(ThreadLocalRandom.current().nextInt(arrayList2.size()));
            AppearanceManager.writePersistent(object2, variant.aspect);
            AppearanceManager.addAspects(object2, variant.aspect);
        }
        catch (Throwable throwable) {
            AppearanceManager.report("appearance assignment", throwable);
        }
    }

    public static void restorePersistentAspect(Object object) {
        if (object == null) {
            return;
        }
        try {
            String string = AppearanceManager.readPersistent(object);
            if (!string.isBlank()) {
                AppearanceManager.addAspects(object, string);
            }
        }
        catch (Throwable throwable) {
            AppearanceManager.report("appearance restoration", throwable);
        }
    }

    private static boolean isClient(Object object) throws Exception {
        Object object2 = ReflectionUtil.method(object.getClass(), "level", new String[]{"m_9236_", "level"}, 0).invoke(object, new Object[0]);
        try {
            return ReflectionUtil.field(object2.getClass(), "isClientSide", "f_46443_", "isClientSide").getBoolean(object2);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static String speciesName(Object object) throws Exception {
        Object object2 = ReflectionUtil.method(object.getClass(), "getSpecies", new String[]{"getSpecies"}, 0).invoke(object, new Object[0]);
        Object object3 = ReflectionUtil.method(object2.getClass(), "getResourceIdentifier", new String[]{"getResourceIdentifier"}, 0).invoke(object2, new Object[0]);
        String string = String.valueOf(object3).toLowerCase(Locale.ROOT);
        int n = string.indexOf(58);
        return n >= 0 ? string.substring(n + 1) : string;
    }

    private static Set<String> aspects(Object object) throws Exception {
        Object object2 = ReflectionUtil.method(object.getClass(), "getAspects", new String[]{"getAspects"}, 0).invoke(object, new Object[0]);
        return object2 instanceof Set ? (Set)object2 : Set.of();
    }

    private static void addAspects(Object object, String string) throws Exception {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>(AppearanceManager.aspects(object));
        for (String string2 : string.split(",")) {
            if (string2.isBlank()) continue;
            linkedHashSet.add(AppearanceManager.normalize(string2));
        }
        ReflectionUtil.method(object.getClass(), "setAspects", new String[]{"setAspects"}, 1).invoke(object, linkedHashSet);
    }

    private static Object persistentTag(Object object) throws Exception {
        return ReflectionUtil.method(object.getClass(), "getPersistentData", new String[]{"getPersistentData"}, 0).invoke(object, new Object[0]);
    }

    private static String readPersistent(Object object) throws Exception {
        Object object2 = AppearanceManager.persistentTag(object);
        if (object2 == null) {
            return "";
        }
        String string = AppearanceManager.readTagString(object2, TAG);
        if (!string.isBlank()) {
            return string;
        }
        return AppearanceManager.readTagString(object2, LEGACY_TAG);
    }

    private static String readTagString(Object object, String string) {
        for (String string2 : new String[]{"getString", "m_128461_"}) {
            try {
                return String.valueOf(ReflectionUtil.method(object.getClass(), "tagGetString", new String[]{string2}, 1).invoke(object, string));
            }
            catch (Throwable throwable) {
            }
        }
        return "";
    }

    private static void writePersistent(Object object, String string) throws Exception {
        Object object2 = AppearanceManager.persistentTag(object);
        if (object2 == null) {
            return;
        }
        for (String string2 : new String[]{"putString", "m_128359_"}) {
            try {
                ReflectionUtil.method(object2.getClass(), "tagPutString", new String[]{string2}, 2).invoke(object2, TAG, string);
                return;
            }
            catch (Throwable throwable) {
            }
        }
        throw new NoSuchMethodException("CompoundTag.putString");
    }

    private static String normalize(String string) {
        String string2 = string.toLowerCase(Locale.ROOT).trim();
        if (string2.equals("radar_spawned")) {
            return "mythical_holographic";
        }
        if (string2.equals("mythical_fireworks")) {
            return "mythical_firework";
        }
        return string2;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Map<String, List<Variant>> loadVariants() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try (InputStream inputStream = AppearanceManager.class.getClassLoader().getResourceAsStream("META-INF/sunlitcompatible/appearance-variants.tsv");){
            if (inputStream == null) {
                Map<String, List<Variant>> map = Map.of();
                return map;
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    String[] stringArray;
                    if (string.isBlank() || string.startsWith("#") || (stringArray = string.split("\\t", 2)).length < 2) continue;
                    ArrayList<Variant> arrayList = new ArrayList<Variant>();
                    for (String string2 : stringArray[1].split(",")) {
                        String[] stringArray2 = string2.split(":", 2);
                        if (stringArray2.length != 2) continue;
                        arrayList.add(new Variant(AppearanceManager.normalize(stringArray2[0]), stringArray2[1].equals("shader")));
                    }
                    linkedHashMap.put(stringArray[0], Collections.unmodifiableList(arrayList));
                }
                return Collections.unmodifiableMap(linkedHashMap);
            }
        }
        catch (Throwable throwable) {
            AppearanceManager.report("variant table load", throwable);
        }
        return Collections.unmodifiableMap(linkedHashMap);
    }

    private static Set<String> loadLegendaryNames() {
        LinkedHashSet<String> linkedHashSet;
        block14: {
            linkedHashSet = new LinkedHashSet<String>();
            try (InputStream inputStream = AppearanceManager.class.getClassLoader().getResourceAsStream("META-INF/sunlitcompatible/legendary-species.tsv");){
                if (inputStream == null) break block14;
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                    String string;
                    while ((string = bufferedReader.readLine()) != null) {
                        if (string.isBlank() || string.startsWith("#")) continue;
                        linkedHashSet.add(string.split("\\t")[0]);
                    }
                }
            }
            catch (Throwable throwable) {
                AppearanceManager.report("legendary table load", throwable);
            }
        }
        return Collections.unmodifiableSet(linkedHashSet);
    }

    private static Set<String> recognized() {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (List<Variant> list : VARIANTS.values()) {
            for (Variant variant : list) {
                linkedHashSet.add(variant.aspect);
            }
        }
        return Collections.unmodifiableSet(linkedHashSet);
    }

    private static void report(String string, Throwable throwable) {
        if (!errorLogged) {
            errorLogged = true;
            System.err.println("[SunlitCompatibleSkins] Failed during " + string + ": " + String.valueOf(throwable));
            throwable.printStackTrace(System.err);
        }
    }

    private record Variant(String aspect, boolean shader) {
    }
}

