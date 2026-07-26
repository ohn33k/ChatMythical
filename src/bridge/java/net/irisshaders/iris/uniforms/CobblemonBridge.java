package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.layer.GbufferPrograms;

/** Shared draw-time effect state read by Oculus' dynamic uniform supplier. */
public final class CobblemonBridge {
    private static volatile int effectId;
    private static volatile boolean firstChangeLogged;

    private CobblemonBridge() {}

    public static void setEffectForEntity(int id) {
        int next = id >= 1 && id <= 7 ? id : 0;
        if (effectId == next) return;
        effectId = next;
        GbufferPrograms.runFallbackEntityListener();
        if (!firstChangeLogged && next > 0) {
            firstChangeLogged = true;
            System.out.println("[MythicalEffectsBridge/V4] Oculus dynamic uniform changed; first active value=" + next + ".");
        }
    }

    public static int getEffectForEntity() {
        return effectId;
    }
}
