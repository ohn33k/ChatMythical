package com.openai.sunlitskins;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class ReflectionUtil {
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();
    private static final Map<String, Field> FIELDS = new ConcurrentHashMap<>();
    private ReflectionUtil() {}

    static Method method(Class<?> type, String cacheKey, String[] names, int params) throws ReflectiveOperationException {
        String key = type.getName() + "#" + cacheKey;
        Method cached = METHODS.get(key); if (cached != null) return cached;
        for (String name : names) {
            for (Method m : allMethods(type)) {
                if (m.getName().equals(name) && m.getParameterCount() == params) {
                    m.setAccessible(true); METHODS.put(key, m); return m;
                }
            }
        }
        throw new NoSuchMethodException(key + " names=" + Arrays.toString(names));
    }

    static Field field(Class<?> type, String cacheKey, String... names) throws ReflectiveOperationException {
        String key = type.getName() + "#" + cacheKey;
        Field cached = FIELDS.get(key); if (cached != null) return cached;
        for (Class<?> c=type;c!=null;c=c.getSuperclass()) {
            for(String n:names) try {
                Field f=c.getDeclaredField(n); f.setAccessible(true); FIELDS.put(key,f); return f;
            } catch(NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(key);
    }

    static List<Method> allMethods(Class<?> type) {
        ArrayList<Method> out=new ArrayList<>();
        for(Class<?> c=type;c!=null;c=c.getSuperclass()) Collections.addAll(out,c.getDeclaredMethods());
        Collections.addAll(out,type.getMethods());
        return out;
    }
}
