package net.rebel459.unified.fabric.client.platform;

import net.rebel459.unified.api.client.core.UnifiedClientRegistries;
import net.rebel459.unified.fabric.client.core.FabricClientHelpersImpl;
import net.rebel459.unified.fabric.client.core.FabricUnifiedClientRegistries;
import net.rebel459.unified.impl.client.core.ClientHelpersImpl;
import net.rebel459.unified.impl.client.platform.ClientInternalHandler;

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

    @Override
    public ClientHelpersImpl.ResourcePacks getResourcePacks() {
        return new FabricClientHelpersImpl.ResourcePacks();
    }
}