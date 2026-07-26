package com.openai.mythicalbridge;

import net.minecraft.client.renderer.RenderStateShard;

/** Render-state shard whose setup/clear runs when Minecraft draws the batch, not when it queues vertices. */
public final class EffectTexturingState extends RenderStateShard.TexturingStateShard {
    public EffectTexturingState(int effectId) {
        super("mythical_effect_" + effectId,
            () -> OculusEffectState.set(effectId),
            () -> OculusEffectState.set(0));
    }
}
