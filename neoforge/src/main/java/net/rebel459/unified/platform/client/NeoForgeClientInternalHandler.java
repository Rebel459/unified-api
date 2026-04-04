package net.rebel459.unified.platform.client;

public class NeoForgeClientInternalHandler implements ClientInternalHandler {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new NeoForgeUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public ClientHelpersImpl.Networking getNetworkPayloads() {
        return new NeoForgeClientHelpersImpl.Networking();
    }

    @Override
    public ClientHelpersImpl.EntityRenderers getEntityRenderers() {
        return new NeoForgeClientHelpersImpl.EntityRenderers();
    }

    @Override
    public ClientHelpersImpl.Tooltips getTooltips() {
        return new NeoForgeClientHelpersImpl.Tooltips();
    }

    @Override
    public ClientHelpersImpl.ParticleProviders getParticleProviders() {
        return new NeoForgeClientHelpersImpl.ParticleProviders();
    }
}