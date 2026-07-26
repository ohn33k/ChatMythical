/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  snownee.jade.api.EntityAccessor
 *  snownee.jade.api.IEntityComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.config.IPluginConfig
 */
package com.openai.sunlitskins.jade;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum SkinVariantProvider implements IEntityComponentProvider
{
    INSTANCE;

    private static final ResourceLocation UID;
    private static final String PERSISTENT_KEY = "sunlitcompatible_appearance";

    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        try {
            Object object;
            Object object2;
            Entity entity = entityAccessor.getEntity();
            Object object3 = SkinVariantProvider.invokeNoArgs(entity, "getPokemon");
            if (object3 == null) {
                return;
            }
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
            Object object4 = SkinVariantProvider.invokeNoArgs(object3, "getAspects");
            if (object4 instanceof Collection) {
                object2 = (Collection)object4;
                var10_10 = object2.iterator();
                while (var10_10.hasNext()) {
                    var11_11 = var10_10.next();
                    SkinVariantProvider.addLabel(linkedHashSet, String.valueOf(var11_11));
                }
            } else if (object4 instanceof Iterable) {
                object = (Iterable)object4;
                var10_10 = object.iterator();
                while (var10_10.hasNext()) {
                    var11_11 = var10_10.next();
                    SkinVariantProvider.addLabel(linkedHashSet, String.valueOf(var11_11));
                }
            }
            if (linkedHashSet.isEmpty() && (object = SkinVariantProvider.invokeOneArg(object2 = SkinVariantProvider.invokeNoArgs(object3, "getPersistentData"), "getString", PERSISTENT_KEY)) != null) {
                for (Iterator<Object> iterator : String.valueOf(object).split(",")) {
                    SkinVariantProvider.addLabel(linkedHashSet, iterator);
                }
            }
            if (!linkedHashSet.isEmpty()) {
                iTooltip.add(Component.literal((String)String.join((CharSequence)" / ", linkedHashSet)), UID);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public ResourceLocation getUid() {
        return UID;
    }

    private static void addLabel(Set<String> set, String string) {
        String string2 = SkinVariantProvider.normalize(string);
        String string3 = SkinVariantProvider.displayName(string2);
        if (string3 != null && !string3.isBlank()) {
            set.add(string3);
        }
    }

    private static String normalize(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
        if (string2.equals("radar_spawned")) {
            return "mythical_holographic";
        }
        if (string2.equals("mythical_fireworks")) {
            return "mythical_firework";
        }
        return string2;
    }

    private static String displayName(String string) {
        if (!string.startsWith("mythical_")) {
            return null;
        }
        String string2 = string.substring("mythical_".length());
        if (string2.isBlank() || string2.equals("normal") || string2.equals("default")) {
            return null;
        }
        if (string2.equals("glitch")) {
            return "Glitched";
        }
        if (string2.equals("fireworks")) {
            return "Firework";
        }
        if (string2.equals("smp")) {
            return "SMP";
        }
        if (string2.equals("smp_v2")) {
            return "SMP V2";
        }
        if (string2.equals("smp_v3")) {
            return "SMP V3";
        }
        if (string2.startsWith("amongus_")) {
            return "Among Us " + SkinVariantProvider.titleWords(string2.substring("amongus_".length()));
        }
        return SkinVariantProvider.titleWords(string2);
    }

    private static String titleWords(String string) {
        String[] stringArray = string.split("_+");
        ArrayList<Object> arrayList = new ArrayList<Object>(stringArray.length);
        for (String string2 : stringArray) {
            if (string2.isBlank()) continue;
            if (string2.equals("smp")) {
                arrayList.add("SMP");
                continue;
            }
            if (string2.matches("v\\d+")) {
                arrayList.add(string2.toUpperCase(Locale.ROOT));
                continue;
            }
            arrayList.add(Character.toUpperCase(string2.charAt(0)) + string2.substring(1));
        }
        return String.join((CharSequence)" ", arrayList);
    }

    private static Object invokeNoArgs(Object object, String string) throws Exception {
        if (object == null) {
            return null;
        }
        Method method = object.getClass().getMethod(string, new Class[0]);
        return method.invoke(object, new Object[0]);
    }

    private static Object invokeOneArg(Object object, String string, Object object2) throws Exception {
        if (object == null) {
            return null;
        }
        for (Method method : object.getClass().getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 1) continue;
            return method.invoke(object, object2);
        }
        return null;
    }

    static {
        UID = new ResourceLocation("sunlitcompatible", "skin_variant");
    }
}

