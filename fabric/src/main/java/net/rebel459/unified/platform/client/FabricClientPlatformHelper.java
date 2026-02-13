package net.rebel459.unified.platform.client;

public class FabricClientPlatformHelper implements ClientPlatformHelper {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new FabricUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public UnifiedClientHelpers.NetworkPayloads getNetworkPayloads() {
        return new FabricUnifiedClientHelpers.NetworkPayloads();
    }

    @Override
    public UnifiedClientHelpers.BlockLayers getBlockLayers() {
        return new FabricUnifiedClientHelpers.BlockLayers();
    }

    @Override
    public UnifiedClientHelpers.EntityRenderers getEntityRenderers() {
        return new FabricUnifiedClientHelpers.EntityRenderers();
    }

    @Override
    public UnifiedClientHelpers.Tooltips getTooltips() {
        return new FabricUnifiedClientHelpers.Tooltips();
    }

    @Override
    public UnifiedClientHelpers.ParticleProviders getParticleProviders() {
        return new FabricUnifiedClientHelpers.ParticleProviders();
    }
}