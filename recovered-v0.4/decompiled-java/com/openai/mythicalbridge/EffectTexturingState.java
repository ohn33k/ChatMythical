/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderStateShard$TexturingStateShard
 */
package com.openai.mythicalbridge;

import com.openai.mythicalbridge.OculusEffectState;
import net.minecraft.client.renderer.RenderStateShard;

public final class EffectTexturingState
extends RenderStateShard.TexturingStateShard {
    public EffectTexturingState(int n) {
        super("mythical_effect_" + n, () -> OculusEffectState.set(n), () -> OculusEffectState.set(0));
    }
}

