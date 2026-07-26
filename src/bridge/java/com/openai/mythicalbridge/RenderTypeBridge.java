package com.openai.mythicalbridge;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Mirrors Mythical's real approach: select a custom RenderType from the Pokémon aspect,
 * then let that RenderType set/clear the uniform at actual draw time.
 */
public final class RenderTypeBridge {
    private static final ThreadLocal<Object> CURRENT_ENTITY = new ThreadLocal<>();
    private static final ConcurrentHashMap<Class<?>, Method> ASPECT_METHODS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Method> INVOKE_METHODS = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static volatile boolean failureReported;

    private RenderTypeBridge() {}

    public static void setCurrentEntity(Object entity) {
        if (entity == null) CURRENT_ENTITY.remove();
        else CURRENT_ENTITY.set(entity);
    }

    public static void clearCurrentEntity() {
        CURRENT_ENTITY.remove();
    }

    public static RenderType choose(Object originalFunction, ResourceLocation texture) {
        Object entity = CURRENT_ENTITY.get();
        int effectId = effectFor(entity);
        if (effectId > 0) {
            String marker = effectId + ":" + (entity == null ? "null" : entity.getClass().getName());
            if (REPORTED.add(marker)) {
                System.out.println("[MythicalEffectsBridge/V4] Selected effect id " + effectId
                    + " from synchronized Pokémon aspects; using a draw-time custom RenderType.");
            }
            return EffectRenderTypeFactory.cutout(texture, effectId);
        }
        return invokeOriginal(originalFunction, texture);
    }

    private static RenderType invokeOriginal(Object function, ResourceLocation texture) {
        if (function == null) return null;
        try {
            Method invoke = INVOKE_METHODS.computeIfAbsent(function.getClass(), type -> {
                try {
                    Method m = type.getMethod("invoke", Object.class);
                    m.setAccessible(true);
                    return m;
                } catch (ReflectiveOperationException e) {
                    throw new MethodLookupFailure(e);
                }
            });
            return (RenderType) invoke.invoke(function, texture);
        } catch (Throwable t) {
            Throwable cause = t instanceof MethodLookupFailure && t.getCause() != null ? t.getCause() : t;
            report("Could not invoke Cobblemon's original RenderType selector", cause);
            return null;
        }
    }

    private static int effectFor(Object entity) {
        if (entity == null) return 0;
        try {
            Method getAspects = ASPECT_METHODS.computeIfAbsent(entity.getClass(), type -> {
                try {
                    Method m = type.getMethod("getAspects");
                    m.setAccessible(true);
                    return m;
                } catch (ReflectiveOperationException e) {
                    throw new MethodLookupFailure(e);
                }
            });
            Object raw = getAspects.invoke(entity);
            if (!(raw instanceof Iterable<?> aspects)) return 0;

            for (Object value : aspects) {
                if (value == null) continue;
                String aspect = value.toString().toLowerCase(Locale.ROOT);
                switch (aspect) {
                    case "mythical_radiant", "radiant": return 1;
                    case "mythical_magma", "magma": return 2;
                    case "mythical_glitch", "glitch": return 3;
                    case "mythical_galaxy", "galaxy": return 4;
                    case "mythical_matrix", "matrix": return 5;
                    case "mythical_fireworks", "mythical_firework", "fireworks", "firework": return 6;
                    case "mythical_holographic", "holographic": return 7;
                    default: { }
                }
            }
        } catch (Throwable t) {
            Throwable cause = t instanceof MethodLookupFailure && t.getCause() != null ? t.getCause() : t;
            report("Could not inspect Pokémon aspects", cause);
        }
        return 0;
    }

    private static void report(String message, Throwable t) {
        if (!failureReported) {
            failureReported = true;
            System.err.println("[MythicalEffectsBridge/V4] " + message + ": " + t);
            t.printStackTrace(System.err);
        }
    }

    private static final class MethodLookupFailure extends RuntimeException {
        MethodLookupFailure(Throwable cause) { super(cause); }
    }
}
