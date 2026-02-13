package net.rebel459.unified.platform.client;

public class NeoForgeClientPlatformHelper implements ClientPlatformHelper {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new NeoForgeUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public UnifiedClientHelpers.NetworkPayloads getNetworkPayloads() {
        return new NeoForgeUnifiedClientHelpers.NetworkPayloads();
    }

    @Override
    public UnifiedClientHelpers.BlockLayers getBlockLayers() {
        return new NeoForgeUnifiedClientHelpers.BlockLayers();
    }

    @Override
    public UnifiedClientHelpers.EntityRenderers getEntityRenderers() {
        return new NeoForgeUnifiedClientHelpers.EntityRenderers();
    }

    @Override
    public UnifiedClientHelpers.Tooltips getTooltips() {
        return new NeoForgeUnifiedClientHelpers.Tooltips();
    }

    @Override
    public UnifiedClientHelpers.ParticleProviders getParticleProviders() {
        return new NeoForgeUnifiedClientHelpers.ParticleProviders();
    }
}