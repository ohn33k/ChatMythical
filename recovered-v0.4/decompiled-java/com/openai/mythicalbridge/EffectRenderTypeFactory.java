/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TexturingStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.resources.ResourceLocation
 */
package com.openai.mythicalbridge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.openai.mythicalbridge.EffectTexturingState;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class EffectRenderTypeFactory {
    private static final ConcurrentHashMap<String, RenderType> CACHE = new ConcurrentHashMap();

    private EffectRenderTypeFactory() {
    }

    public static RenderType cutout(ResourceLocation resourceLocation, int n) {
        String string2 = resourceLocation.toString() + "\u0000" + n;
        return CACHE.computeIfAbsent(string2, string -> EffectRenderTypeFactory.createCutout(resourceLocation, n));
    }

    private static RenderType createCutout(ResourceLocation resourceLocation, int n) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.m_110628_().m_173292_(RenderStateShard.f_173113_).m_173290_((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(resourceLocation, false, false)).m_110685_(RenderStateShard.f_110134_).m_110671_(RenderStateShard.f_110152_).m_110677_(RenderStateShard.f_110154_).m_110683_((RenderStateShard.TexturingStateShard)new EffectTexturingState(n)).m_110691_(true);
        RenderType.CompositeRenderType compositeRenderType = RenderType.m_173215_((String)("mythical_effect_entity_cutout_" + n), (VertexFormat)DefaultVertexFormat.f_85812_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)compositeState);
        System.out.println("[MythicalEffectsBridge/V4] Created effect RenderType id=" + n + " texture=" + String.valueOf(resourceLocation) + ".");
        return compositeRenderType;
    }
}

