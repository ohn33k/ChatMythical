/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 */
package com.openai.mythicalbridge;

import com.openai.mythicalbridge.EffectRenderTypeFactory;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class RenderTypeBridge {
    private static final ThreadLocal<Object> CURRENT_ENTITY = new ThreadLocal();
    private static final ConcurrentHashMap<Class<?>, Method> ASPECT_METHODS = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Class<?>, Method> INVOKE_METHODS = new ConcurrentHashMap();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static volatile boolean failureReported;

    private RenderTypeBridge() {
    }

    public static void setCurrentEntity(Object object) {
        if (object == null) {
            CURRENT_ENTITY.remove();
        } else {
            CURRENT_ENTITY.set(object);
        }
    }

    public static void clearCurrentEntity() {
        CURRENT_ENTITY.remove();
    }

    public static RenderType choose(Object object, ResourceLocation resourceLocation) {
        Object object2 = CURRENT_ENTITY.get();
        int n = RenderTypeBridge.effectFor(object2);
        if (n > 0) {
            String string = n + ":" + (object2 == null ? "null" : object2.getClass().getName());
            if (REPORTED.add(string)) {
                System.out.println("[MythicalEffectsBridge/V4] Selected effect id " + n + " from synchronized Pok\u00e9mon aspects; using a draw-time custom RenderType.");
            }
            return EffectRenderTypeFactory.cutout(resourceLocation, n);
        }
        return RenderTypeBridge.invokeOriginal(object, resourceLocation);
    }

    private static RenderType invokeOriginal(Object object, ResourceLocation resourceLocation) {
        if (object == null) {
            return null;
        }
        try {
            Method method = INVOKE_METHODS.computeIfAbsent(object.getClass(), clazz -> {
                try {
                    Method method = clazz.getMethod("invoke", Object.class);
                    method.setAccessible(true);
                    return method;
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    throw new MethodLookupFailure(reflectiveOperationException);
                }
            });
            return (RenderType)method.invoke(object, resourceLocation);
        }
        catch (Throwable throwable) {
            Throwable throwable2 = throwable instanceof MethodLookupFailure && throwable.getCause() != null ? throwable.getCause() : throwable;
            RenderTypeBridge.report("Could not invoke Cobblemon's original RenderType selector", throwable2);
            return null;
        }
    }

    private static int effectFor(Object object) {
        if (object == null) {
            return 0;
        }
        try {
            Method method = ASPECT_METHODS.computeIfAbsent(object.getClass(), clazz -> {
                try {
                    Method method = clazz.getMethod("getAspects", new Class[0]);
                    method.setAccessible(true);
                    return method;
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    throw new MethodLookupFailure(reflectiveOperationException);
                }
            });
            Object object2 = method.invoke(object, new Object[0]);
            if (!(object2 instanceof Iterable)) {
                return 0;
            }
            Iterable iterable = (Iterable)object2;
            for (Object t : iterable) {
                String string;
                if (t == null) continue;
                switch (string = t.toString().toLowerCase(Locale.ROOT)) {
                    case "mythical_radiant": 
                    case "radiant": {
                        return 1;
                    }
                    case "mythical_magma": 
                    case "magma": {
                        return 2;
                    }
                    case "mythical_glitch": 
                    case "glitch": {
                        return 3;
                    }
                    case "mythical_galaxy": 
                    case "galaxy": {
                        return 4;
                    }
                    case "mythical_matrix": 
                    case "matrix": {
                        return 5;
                    }
                    case "mythical_fireworks": 
                    case "mythical_firework": 
                    case "fireworks": 
                    case "firework": {
                        return 6;
                    }
                    case "mythical_holographic": 
                    case "holographic": {
                        return 7;
                    }
                }
            }
        }
        catch (Throwable throwable) {
            Throwable throwable2 = throwable instanceof MethodLookupFailure && throwable.getCause() != null ? throwable.getCause() : throwable;
            RenderTypeBridge.report("Could not inspect Pok\u00e9mon aspects", throwable2);
        }
        return 0;
    }

    private static void report(String string, Throwable throwable) {
        if (!failureReported) {
            failureReported = true;
            System.err.println("[MythicalEffectsBridge/V4] " + string + ": " + String.valueOf(throwable));
            throwable.printStackTrace(System.err);
        }
    }

    private static final class MethodLookupFailure
    extends RuntimeException {
        MethodLookupFailure(Throwable throwable) {
            super(throwable);
        }
    }
}

