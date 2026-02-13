package net.rebel459.unified.platform.client;

import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.platform.UnifiedRegistries;

public interface ClientPlatformHelper {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    UnifiedClientHelpers.NetworkPayloads getNetworkPayloads();
    UnifiedClientHelpers.BlockLayers getBlockLayers();
    UnifiedClientHelpers.EntityRenderers getEntityRenderers();
    UnifiedClientHelpers.Tooltips getTooltips();
    UnifiedClientHelpers.ParticleProviders getParticleProviders();
}