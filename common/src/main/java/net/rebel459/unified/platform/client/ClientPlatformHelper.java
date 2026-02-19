package net.rebel459.unified.platform.client;

public interface ClientPlatformHelper {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    ClientHelpersImpl.Networking getNetworkPayloads();
    ClientHelpersImpl.EntityRenderers getEntityRenderers();
    ClientHelpersImpl.Tooltips getTooltips();
    ClientHelpersImpl.ParticleProviders getParticleProviders();
}