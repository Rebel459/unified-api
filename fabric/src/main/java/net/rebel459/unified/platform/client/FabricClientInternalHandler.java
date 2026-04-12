package net.rebel459.unified.platform.client;

public class FabricClientInternalHandler implements ClientInternalHandler {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new FabricUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public ClientHelpersImpl.Networking getNetworking() {
        return new FabricClientHelpersImpl.Networking();
    }

    @Override
    public ClientHelpersImpl.EntityRenderers getEntityRenderers() {
        return new FabricClientHelpersImpl.EntityRenderers();
    }

    @Override
    public ClientHelpersImpl.Tooltips getTooltips() {
        return new FabricClientHelpersImpl.Tooltips();
    }

    @Override
    public ClientHelpersImpl.ParticleProviders getParticleProviders() {
        return new FabricClientHelpersImpl.ParticleProviders();
    }
}