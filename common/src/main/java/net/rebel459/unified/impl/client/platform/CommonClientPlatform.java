package net.rebel459.unified.impl.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.CommonClientHelpers;

public interface CommonClientPlatform {

    UnifiedClientRegistries.KeyMappings createKeyMappings(String modId);

    CommonClientHelpers.Networking getNetworking();
    CommonClientHelpers.EntityRenderers getEntityRenderers();
    CommonClientHelpers.Tooltips getTooltips();
    CommonClientHelpers.ParticleProviders getParticleProviders();
    CommonClientHelpers.ResourcePacks getResourcePacks();
    CommonClientHelpers.ReloadListeners getReloadListeners();
}