package net.rebel459.unified.fabric.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.fabric.client.core.FabricClientHelpers;
import net.rebel459.unified.fabric.client.core.FabricUnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.CommonClientHelpers;
import net.rebel459.unified.impl.client.platform.CommonClientPlatform;

public class FabricClientPlatform implements CommonClientPlatform {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new FabricUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public CommonClientHelpers.Networking getNetworking() {
        return new FabricClientHelpers.Networking();
    }

    @Override
    public CommonClientHelpers.EntityRenderers getEntityRenderers() {
        return new FabricClientHelpers.EntityRenderers();
    }

    @Override
    public CommonClientHelpers.Tooltips getTooltips() {
        return new FabricClientHelpers.Tooltips();
    }

    @Override
    public CommonClientHelpers.ParticleProviders getParticleProviders() {
        return new FabricClientHelpers.ParticleProviders();
    }

    @Override
    public CommonClientHelpers.ResourcePacks getResourcePacks() {
        return new FabricClientHelpers.ResourcePacks();
    }
}