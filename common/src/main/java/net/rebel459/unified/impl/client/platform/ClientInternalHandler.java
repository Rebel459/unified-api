package net.rebel459.unified.impl.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.ClientHelpersImpl;

public interface ClientInternalHandler {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    ClientHelpersImpl.Networking getNetworking();
    ClientHelpersImpl.EntityRenderers getEntityRenderers();
    ClientHelpersImpl.Tooltips getTooltips();
    ClientHelpersImpl.ParticleProviders getParticleProviders();
    ClientHelpersImpl.ResourcePacks getResourcePacks();
}