/*
 * Decompiled with CFR 0.152.
 */
package com.openai.sunlitskins;

import com.openai.sunlitskins.ReflectionUtil;
import com.openai.sunlitskins.SunlitSkinsConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public final class LegendarySpawnManager {
    private static final Path STATE = Path.of("config", "sunlit-compatible-skins-state.properties");
    private static final Path LEGACY_STATE = Path.of("config", "mythicalbackport-state.properties");
    private static final List<Legend> LEGENDS = LegendarySpawnManager.loadLegends();
    private static long onlineSeconds;
    private static long secondsSinceCheck;
    private static long ticks;
    private static long secondsSinceSave;
    private static boolean loaded;
    private static boolean errorLogged;

    private LegendarySpawnManager() {
    }

    public static void tick(Object object) {
        try {
            double d;
            if (!loaded) {
                LegendarySpawnManager.loadState();
                loaded = true;
            }
            if (++ticks < 20L) {
                return;
            }
            ticks = 0L;
            List<?> list = LegendarySpawnManager.players(object);
            if (list.isEmpty()) {
                return;
            }
            ++onlineSeconds;
            ++secondsSinceCheck;
            if (++secondsSinceSave >= 60L) {
                LegendarySpawnManager.saveState();
                secondsSinceSave = 0L;
            }
            SunlitSkinsConfig sunlitSkinsConfig = SunlitSkinsConfig.get();
            if (!sunlitSkinsConfig.legendaryEnabled || LEGENDS.isEmpty()) {
                return;
            }
            long l = (long)sunlitSkinsConfig.minimumOnlineMinutes * 60L;
            long l2 = (long)sunlitSkinsConfig.maximumOnlineMinutes * 60L;
            long l3 = (long)sunlitSkinsConfig.checkIntervalMinutes * 60L;
            if (onlineSeconds < l || secondsSinceCheck < l3) {
                return;
            }
            secondsSinceCheck = 0L;
            if (onlineSeconds >= l2) {
                d = 100.0;
            } else {
                double d2 = (double)(onlineSeconds - l) / (double)Math.max(1L, l2 - l);
                d = sunlitSkinsConfig.initialCheckChancePercent + (sunlitSkinsConfig.maximumCheckChancePercent - sunlitSkinsConfig.initialCheckChancePercent) * d2;
            }
            if (ThreadLocalRandom.current().nextDouble(100.0) > d) {
                return;
            }
            Object obj = list.get(ThreadLocalRandom.current().nextInt(list.size()));
            Legend legend = LegendarySpawnManager.choose(sunlitSkinsConfig);
            if (legend == null) {
                return;
            }
            SpawnResult spawnResult = LegendarySpawnManager.spawnNear(obj, legend, sunlitSkinsConfig);
            if (spawnResult != null) {
                LegendarySpawnManager.announce(list, legend, spawnResult, sunlitSkinsConfig);
                onlineSeconds = 0L;
                secondsSinceCheck = 0L;
                LegendarySpawnManager.saveState();
            }
        }
        catch (Throwable throwable) {
            LegendarySpawnManager.report(throwable);
        }
    }

    private static Legend choose(SunlitSkinsConfig sunlitSkinsConfig) {
        double d = 0.0;
        for (Legend legend : LEGENDS) {
            d += sunlitSkinsConfig.weightFor(legend.species, legend.weight);
        }
        if (d <= 0.0) {
            return null;
        }
        double d2 = ThreadLocalRandom.current().nextDouble(d);
        for (Legend legend : LEGENDS) {
            if (!((d2 -= sunlitSkinsConfig.weightFor(legend.species, legend.weight)) <= 0.0)) continue;
            return legend;
        }
        return LEGENDS.get(LEGENDS.size() - 1);
    }

    private static SpawnResult spawnNear(Object object, Legend legend, SunlitSkinsConfig sunlitSkinsConfig) throws Exception {
        Object object2 = ReflectionUtil.method(object.getClass(), "level", new String[]{"m_9236_", "level"}, 0).invoke(object, new Object[0]);
        double d = ((Number)ReflectionUtil.method(object.getClass(), "getX", new String[]{"m_20185_", "getX"}, 0).invoke(object, new Object[0])).doubleValue();
        double d2 = ((Number)ReflectionUtil.method(object.getClass(), "getY", new String[]{"m_20186_", "getY"}, 0).invoke(object, new Object[0])).doubleValue();
        double d3 = ((Number)ReflectionUtil.method(object.getClass(), "getZ", new String[]{"m_20189_", "getZ"}, 0).invoke(object, new Object[0])).doubleValue();
        for (int i = 0; i < 12; ++i) {
            double d4 = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
            double d5 = 24.0 + ThreadLocalRandom.current().nextDouble(32.0);
            int n = (int)Math.floor(d + Math.cos(d4) * d5);
            int n2 = (int)Math.floor(d3 + Math.sin(d4) * d5);
            int n3 = LegendarySpawnManager.surfaceY(object2, n, n2, (int)Math.floor(d2));
            int n4 = ThreadLocalRandom.current().nextInt(sunlitSkinsConfig.levelMin, sunlitSkinsConfig.levelMax + 1);
            Object object3 = LegendarySpawnManager.parseProperties(legend.species + " level=" + n4);
            Object object4 = LegendarySpawnManager.createEntity(object3, object2);
            if (object4 == null) continue;
            ReflectionUtil.method(object4.getClass(), "setPos", new String[]{"m_6034_", "setPos"}, 3).invoke(object4, (double)n + 0.5, (double)n3 + 1.0, (double)n2 + 0.5);
            Object object5 = ReflectionUtil.method(object2.getClass(), "addFreshEntity", new String[]{"m_7967_", "addFreshEntity"}, 1).invoke(object2, object4);
            if (object5 instanceof Boolean && !((Boolean)object5).booleanValue()) continue;
            return new SpawnResult(n, n3 + 1, n2, LegendarySpawnManager.playerName(object));
        }
        return null;
    }

    private static int surfaceY(Object object, int n, int n2, int n3) throws Exception {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.level.levelgen.Heightmap$Types");
            Object obj = Enum.valueOf(clazz, "MOTION_BLOCKING_NO_LEAVES");
            Object object2 = ReflectionUtil.method(object.getClass(), "height", new String[]{"m_6924_", "getHeight"}, 3).invoke(object, obj, n, n2);
            return ((Number)object2).intValue();
        }
        catch (Throwable throwable) {
            return n3;
        }
    }

    private static Object parseProperties(String string) throws Exception {
        Class<?> clazz = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonProperties");
        Object object = clazz.getField("Companion").get(null);
        return object.getClass().getMethod("parse", String.class).invoke(object, string);
    }

    private static Object createEntity(Object object, Object object2) throws Exception {
        for (Method method : object.getClass().getMethods()) {
            if (!method.getName().equals("createEntity") || method.getParameterCount() != 1) continue;
            return method.invoke(object, object2);
        }
        throw new NoSuchMethodException("PokemonProperties.createEntity");
    }

    private static List<?> players(Object object) throws Exception {
        Object object2 = ReflectionUtil.method(object.getClass(), "playerList", new String[]{"m_6846_", "getPlayerList"}, 0).invoke(object, new Object[0]);
        Object object3 = ReflectionUtil.method(object2.getClass(), "players", new String[]{"m_11314_", "getPlayers"}, 0).invoke(object2, new Object[0]);
        return object3 instanceof List ? (List)object3 : List.of();
    }

    private static String playerName(Object object) {
        try {
            Object object2 = ReflectionUtil.method(object.getClass(), "name", new String[]{"m_7755_", "getName"}, 0).invoke(object, new Object[0]);
            return String.valueOf(ReflectionUtil.method(object2.getClass(), "string", new String[]{"getString", "m_7532_"}, 0).invoke(object2, new Object[0]));
        }
        catch (Throwable throwable) {
            return "a player";
        }
    }

    private static void announce(List<?> list, Legend legend, SpawnResult spawnResult, SunlitSkinsConfig sunlitSkinsConfig) {
        if (!sunlitSkinsConfig.announceSpawns) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("A legendary ").append(legend.display).append(" has appeared");
        if (sunlitSkinsConfig.includeNearbyPlayerName) {
            stringBuilder.append(" near ").append(spawnResult.player);
        }
        stringBuilder.append('!');
        if (sunlitSkinsConfig.includeCoordinates) {
            stringBuilder.append(" (").append(spawnResult.x).append(", ").append(spawnResult.y).append(", ").append(spawnResult.z).append(')');
        }
        try {
            Class<?> clazz = Class.forName("net.minecraft.network.chat.Component");
            Object object = clazz.getMethod("m_237113_", String.class).invoke(null, stringBuilder.toString());
            for (Object obj : list) {
                ReflectionUtil.method(obj.getClass(), "sendSystemMessage", new String[]{"m_213846_", "sendSystemMessage"}, 1).invoke(obj, object);
            }
        }
        catch (Throwable throwable) {
            System.out.println("[SunlitCompatibleSkins] " + String.valueOf(stringBuilder));
        }
    }

    private static void loadState() {
        try {
            Path path;
            Path path2 = path = Files.exists(STATE, new LinkOption[0]) ? STATE : LEGACY_STATE;
            if (!Files.exists(path, new LinkOption[0])) {
                return;
            }
            Properties properties = new Properties();
            try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
                properties.load(inputStream);
            }
            onlineSeconds = Long.parseLong(properties.getProperty("accumulated_online_seconds", "0"));
            secondsSinceCheck = Long.parseLong(properties.getProperty("seconds_since_check", "0"));
            if (path.equals(LEGACY_STATE)) {
                System.out.println("[SunlitCompatibleSkins] Imported the previous Mythical legendary timer state.");
                LegendarySpawnManager.saveState();
            }
        }
        catch (Throwable throwable) {
            System.err.println("[SunlitCompatibleSkins] Could not load legendary state: " + String.valueOf(throwable));
        }
    }

    private static void saveState() {
        try {
            Files.createDirectories(STATE.getParent(), new FileAttribute[0]);
            Properties properties = new Properties();
            properties.setProperty("accumulated_online_seconds", Long.toString(onlineSeconds));
            properties.setProperty("seconds_since_check", Long.toString(secondsSinceCheck));
            try (OutputStream outputStream = Files.newOutputStream(STATE, new OpenOption[0]);){
                properties.store(outputStream, "Sunlit Compatible Skins legendary timer; counts player-online time only");
            }
        }
        catch (Throwable throwable) {
            System.err.println("[SunlitCompatibleSkins] Could not save legendary state: " + String.valueOf(throwable));
        }
    }

    private static List<Legend> loadLegends() {
        ArrayList<Legend> arrayList;
        block14: {
            arrayList = new ArrayList<Legend>();
            try (InputStream inputStream = LegendarySpawnManager.class.getClassLoader().getResourceAsStream("META-INF/sunlitcompatible/legendary-species.tsv");){
                if (inputStream == null) break block14;
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                    String string;
                    while ((string = bufferedReader.readLine()) != null) {
                        if (string.isBlank() || string.startsWith("#")) continue;
                        String[] stringArray = string.split("\\t");
                        arrayList.add(new Legend(stringArray[0], stringArray.length > 1 ? stringArray[1] : stringArray[0], stringArray.length > 2 ? Double.parseDouble(stringArray[2]) : 1.0));
                    }
                }
            }
            catch (Throwable throwable) {
                LegendarySpawnManager.report(throwable);
            }
        }
        return Collections.unmodifiableList(arrayList);
    }

    private static void report(Throwable throwable) {
        if (!errorLogged) {
            errorLogged = true;
            System.err.println("[SunlitCompatibleSkins] Legendary spawn system failed: " + String.valueOf(throwable));
            throwable.printStackTrace(System.err);
        }
    }

    private record Legend(String species, String display, double weight) {
    }

    private record SpawnResult(int x, int y, int z, String player) {
    }
}

