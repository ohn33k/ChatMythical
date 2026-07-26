package com.openai.mythicaloculus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import net.irisshaders.iris.uniforms.CobblemonBridge;

/** Registers cobblemon_effectType with every Oculus dynamic-uniform holder. */
public final class OculusUniformHook {
    private static volatile boolean failureReported;
    private static final AtomicInteger REGISTRATIONS = new AtomicInteger();

    private OculusUniformHook() {}

    public static void register(Object holder) {
        if (holder == null) return;
        try {
            Class<?> holderType = Class.forName("net.irisshaders.iris.gl.uniform.DynamicUniformHolder");
            Class<?> notifierType = Class.forName("net.irisshaders.iris.gl.state.ValueUpdateNotifier");
            Class<?> notifiers = Class.forName("net.irisshaders.iris.gl.state.StateUpdateNotifiers");
            Field field = notifiers.getField("fallbackEntityNotifier");
            Object notifier = field.get(null);
            Method uniform1i = holderType.getMethod("uniform1i", String.class, IntSupplier.class, notifierType);
            IntSupplier supplier = CobblemonBridge::getEffectForEntity;
            uniform1i.invoke(holder, "cobblemon_effectType", supplier, notifier);
            int count = REGISTRATIONS.incrementAndGet();
            if (count <= 3) {
                System.out.println("[MythicalEffectsBridge/V4] Registered cobblemon_effectType on Oculus holder #" + count + ".");
            }
        } catch (Throwable t) {
            if (!failureReported) {
                failureReported = true;
                System.err.println("[MythicalEffectsBridge/V4] Could not register Oculus uniform: " + t);
                t.printStackTrace(System.err);
            }
        }
    }
}
