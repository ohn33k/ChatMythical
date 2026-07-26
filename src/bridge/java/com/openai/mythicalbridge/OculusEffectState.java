package com.openai.mythicalbridge;

import java.lang.reflect.Method;

/** Draw-time connection from a custom Minecraft RenderType to Oculus' dynamic uniform. */
public final class OculusEffectState {
    private static volatile Method setter;
    private static volatile boolean lookupDone;
    private static volatile boolean failureReported;
    private static volatile boolean firstSetLogged;

    private OculusEffectState() {}

    public static void set(int effectId) {
        int value = effectId >= 1 && effectId <= 7 ? effectId : 0;
        Method method = setter;
        if (!lookupDone) {
            synchronized (OculusEffectState.class) {
                if (!lookupDone) {
                    try {
                        Class<?> bridge = Class.forName("net.irisshaders.iris.uniforms.CobblemonBridge");
                        method = bridge.getMethod("setEffectForEntity", int.class);
                        setter = method;
                        System.out.println("[MythicalEffectsBridge/V4] Connected draw-time RenderType state to Oculus.");
                    } catch (Throwable t) {
                        report("Could not connect to Oculus CobblemonBridge", t);
                    } finally {
                        lookupDone = true;
                    }
                }
            }
        }
        if (method != null) {
            try {
                method.invoke(null, value);
                if (!firstSetLogged && value > 0) {
                    firstSetLogged = true;
                    System.out.println("[MythicalEffectsBridge/V4] First effect activated at actual batch draw time; id=" + value + ".");
                }
            } catch (Throwable t) {
                report("Could not update Oculus effect state", t);
            }
        }
    }

    private static void report(String message, Throwable t) {
        if (!failureReported) {
            failureReported = true;
            System.err.println("[MythicalEffectsBridge/V4] " + message + ": " + t);
            t.printStackTrace(System.err);
        }
    }
}
