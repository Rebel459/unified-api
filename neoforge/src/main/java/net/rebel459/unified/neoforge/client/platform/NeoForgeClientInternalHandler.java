package net.rebel459.unified.neoforge.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.ClientHelpersImpl;
import net.rebel459.unified.impl.client.platform.ClientInternalHandler;
import net.rebel459.unified.neoforge.client.core.NeoForgeClientHelpersImpl;
import net.rebel459.unified.neoforge.client.core.NeoForgeUnifiedClientRegistries;

public class NeoForgeClientInternalHandler implements ClientInternalHandler {

    @Override
    public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
        return new NeoForgeUnifiedClientRegistries.KeyMappings(modId);
    }

    @Override
    public ClientHelpersImpl.Networking getNetworking() {
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

    @Override
    public ClientHelpersImpl.ResourcePacks getResourcePacks() {
        return new NeoForgeClientHelpersImpl.ResourcePacks();
    }
}