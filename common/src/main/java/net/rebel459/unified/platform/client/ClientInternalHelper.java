package net.rebel459.unified.platform.client;

public interface ClientInternalHelper {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    ClientHelpersImpl.Networking getNetworkPayloads();
    ClientHelpersImpl.EntityRenderers getEntityRenderers();
    ClientHelpersImpl.Tooltips getTooltips();
    ClientHelpersImpl.ParticleProviders getParticleProviders();
}