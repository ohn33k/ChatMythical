/*
 * Decompiled with CFR 0.152.
 */
package com.openai.mythicalbridge;

import java.lang.reflect.Method;

public final class OculusEffectState {
    private static volatile Method setter;
    private static volatile boolean lookupDone;
    private static volatile boolean failureReported;
    private static volatile boolean firstSetLogged;

    private OculusEffectState() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static void set(int n) {
        int n2 = n >= 1 && n <= 7 ? n : 0;
        Method method = setter;
        if (!lookupDone) {
            Class<OculusEffectState> clazz = OculusEffectState.class;
            // MONITORENTER : com.openai.mythicalbridge.OculusEffectState.class
            if (!lookupDone) {
                try {
                    Class<?> clazz2 = Class.forName("net.irisshaders.iris.uniforms.CobblemonBridge");
                    setter = method = clazz2.getMethod("setEffectForEntity", Integer.TYPE);
                    System.out.println("[MythicalEffectsBridge/V4] Connected draw-time RenderType state to Oculus.");
                }
                catch (Throwable throwable) {
                    OculusEffectState.report("Could not connect to Oculus CobblemonBridge", throwable);
                }
                finally {
                    lookupDone = true;
                }
            }
            // MONITOREXIT : clazz
        }
        if (method == null) return;
        try {
            method.invoke(null, n2);
            if (firstSetLogged) return;
            if (n2 <= 0) return;
            firstSetLogged = true;
            System.out.println("[MythicalEffectsBridge/V4] First effect activated at actual batch draw time; id=" + n2 + ".");
            return;
        }
        catch (Throwable throwable) {
            OculusEffectState.report("Could not update Oculus effect state", throwable);
        }
    }

    private static void report(String string, Throwable throwable) {
        if (!failureReported) {
            failureReported = true;
            System.err.println("[MythicalEffectsBridge/V4] " + string + ": " + String.valueOf(throwable));
            throwable.printStackTrace(System.err);
        }
    }
}

