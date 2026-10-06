package net.rebel459.unified.impl.client.platform;

import net.rebel459.unified.impl.client.core.CommonClientHelpers;

public interface CommonClientPlatform {

    CommonClientHelpers.Networking getNetworking();
    CommonClientHelpers.EntityRenderers getEntityRenderers();
    CommonClientHelpers.Tooltips getTooltips();
    CommonClientHelpers.ParticleProviders getParticleProviders();
    CommonClientHelpers.ResourcePacks getResourcePacks();
    CommonClientHelpers.ReloadListeners getReloadListeners();
    CommonClientHelpers.KeyMappings getKeyMappings();
}