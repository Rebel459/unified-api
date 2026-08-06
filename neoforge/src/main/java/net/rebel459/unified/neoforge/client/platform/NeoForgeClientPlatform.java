package net.rebel459.unified.neoforge.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.CommonClientHelpers;
import net.rebel459.unified.impl.client.platform.CommonClientPlatform;
import net.rebel459.unified.neoforge.client.core.NeoForgeClientHelpers;
import net.rebel459.unified.neoforge.client.core.NeoForgeUnifiedClientRegistries;

public class NeoForgeClientPlatform implements CommonClientPlatform {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new NeoForgeUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public CommonClientHelpers.Networking getNetworking() {
        return new NeoForgeClientHelpers.Networking();
    }

    @Override
    public CommonClientHelpers.EntityRenderers getEntityRenderers() {
        return new NeoForgeClientHelpers.EntityRenderers();
    }

    @Override
    public CommonClientHelpers.Tooltips getTooltips() {
        return new NeoForgeClientHelpers.Tooltips();
    }

    @Override
    public CommonClientHelpers.ParticleProviders getParticleProviders() {
        return new NeoForgeClientHelpers.ParticleProviders();
    }

    @Override
    public CommonClientHelpers.ResourcePacks getResourcePacks() {
        return new NeoForgeClientHelpers.ResourcePacks();
    }

    @Override
    public CommonClientHelpers.ReloadListeners getReloadListeners() {
        return new NeoForgeClientHelpers.ReloadListeners();
    }
}