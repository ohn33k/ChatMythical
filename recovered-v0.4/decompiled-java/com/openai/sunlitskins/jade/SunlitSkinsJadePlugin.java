/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  snownee.jade.api.IEntityComponentProvider
 *  snownee.jade.api.IWailaClientRegistration
 *  snownee.jade.api.IWailaPlugin
 *  snownee.jade.api.WailaPlugin
 */
package com.openai.sunlitskins.jade;

import com.openai.sunlitskins.jade.SkinVariantProvider;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class SunlitSkinsJadePlugin
implements IWailaPlugin {
    private static final String POKEMON_ENTITY = "com.cobblemon.mod.common.entity.pokemon.PokemonEntity";

    public void registerClient(IWailaClientRegistration iWailaClientRegistration) {
        try {
            Class<?> clazz = Class.forName(POKEMON_ENTITY);
            if (Entity.class.isAssignableFrom(clazz)) {
                iWailaClientRegistration.registerEntityComponent((IEntityComponentProvider)SkinVariantProvider.INSTANCE, clazz);
            }
        }
        catch (Throwable throwable) {
            System.err.println("[SunlitCompatibleSkins] Jade skin labels were not registered: " + String.valueOf(throwable));
        }
    }
}

