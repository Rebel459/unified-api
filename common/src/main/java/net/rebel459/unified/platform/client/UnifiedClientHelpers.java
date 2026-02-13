package net.rebel459.unified.platform.client;

import net.rebel459.unified.platform.Factory;

import java.util.ServiceLoader;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.NetworkPayloads NETWORK_PAYLOADS = Factory.getClientHelpers().createNetworkPayloads();;
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = Factory.getClientHelpers().createEntityRenderers();;
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = Factory.getClientHelpers().createParticleProviders();;
    public static ClientHelpersImpl.BlockLayers BLOCK_LAYERS = Factory.getClientHelpers().createBlockLayers();;
    public static ClientHelpersImpl.Tooltips TOOLTIPS = Factory.getClientHelpers().createTooltips();;

}