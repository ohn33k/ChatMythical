/*
 * Decompiled with CFR 0.152.
 */
package com.openai.sunlitskins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ReflectionUtil {
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<String, Method>();
    private static final Map<String, Field> FIELDS = new ConcurrentHashMap<String, Field>();

    private ReflectionUtil() {
    }

    static Method method(Class<?> clazz, String string, String[] stringArray, int n) throws ReflectiveOperationException {
        String string2 = clazz.getName() + "#" + string;
        Method method = METHODS.get(string2);
        if (method != null) {
            return method;
        }
        for (String string3 : stringArray) {
            for (Method method2 : ReflectionUtil.allMethods(clazz)) {
                if (!method2.getName().equals(string3) || method2.getParameterCount() != n) continue;
                method2.setAccessible(true);
                METHODS.put(string2, method2);
                return method2;
            }
        }
        throw new NoSuchMethodException(string2 + " names=" + Arrays.toString(stringArray));
    }

    static Field field(Class<?> clazz, String string, String ... stringArray) throws ReflectiveOperationException {
        String string2 = clazz.getName() + "#" + string;
        Field field = FIELDS.get(string2);
        if (field != null) {
            return field;
        }
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            for (String string3 : stringArray) {
                try {
                    Field field2 = clazz2.getDeclaredField(string3);
                    field2.setAccessible(true);
                    FIELDS.put(string2, field2);
                    return field2;
                }
                catch (NoSuchFieldException noSuchFieldException) {
                }
            }
        }
        throw new NoSuchFieldException(string2);
    }

    static List<Method> allMethods(Class<?> clazz) {
        ArrayList<Method> arrayList = new ArrayList<Method>();
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            Collections.addAll(arrayList, clazz2.getDeclaredMethods());
        }
        Collections.addAll(arrayList, clazz.getMethods());
        return arrayList;
    }
}

