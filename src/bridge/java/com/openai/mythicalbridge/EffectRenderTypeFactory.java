package com.openai.mythicalbridge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Produces one cached entity-cutout RenderType per texture/effect pair. */
public final class EffectRenderTypeFactory {
    private static final ConcurrentHashMap<String, RenderType> CACHE = new ConcurrentHashMap<>();

    private EffectRenderTypeFactory() {}

    public static RenderType cutout(ResourceLocation texture, int effectId) {
        String key = texture.toString() + "\u0000" + effectId;
        return CACHE.computeIfAbsent(key, ignored -> createCutout(texture, effectId));
    }

    private static RenderType createCutout(ResourceLocation texture, int effectId) {
        RenderType.CompositeState state = RenderType.CompositeState.m_110628_()
            .m_173292_(RenderStateShard.f_173113_)
            .m_173290_(new RenderStateShard.TextureStateShard(texture, false, false))
            .m_110685_(RenderStateShard.f_110134_)
            .m_110671_(RenderStateShard.f_110152_)
            .m_110677_(RenderStateShard.f_110154_)
            .m_110683_(new EffectTexturingState(effectId))
            .m_110691_(true);

        RenderType result = RenderType.m_173215_(
            "mythical_effect_entity_cutout_" + effectId,
            DefaultVertexFormat.f_85812_,
            VertexFormat.Mode.QUADS,
            256,
            true,
            false,
            state
        );
        System.out.println("[MythicalEffectsBridge/V4] Created effect RenderType id=" + effectId
            + " texture=" + texture + ".");
        return result;
    }
}
