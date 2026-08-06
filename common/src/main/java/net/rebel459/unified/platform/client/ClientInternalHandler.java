package net.rebel459.unified.platform.client;

public interface ClientInternalHandler {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    ClientHelpersImpl.Networking getNetworking();
    ClientHelpersImpl.EntityRenderers getEntityRenderers();
    ClientHelpersImpl.Tooltips getTooltips();
    ClientHelpersImpl.ParticleProviders getParticleProviders();
    ClientHelpersImpl.ReloadListeners getReloadListeners();
}