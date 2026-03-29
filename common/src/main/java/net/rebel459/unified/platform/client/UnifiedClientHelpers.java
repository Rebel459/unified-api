package net.rebel459.unified.platform.client;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.Tooltips TOOLTIPS = ClientInternalHelperImpl.INSTANCE.getTooltips();
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = ClientInternalHelperImpl.INSTANCE.getEntityRenderers();
    public static ClientHelpersImpl.Networking NETWORKING = ClientInternalHelperImpl.INSTANCE.getNetworkPayloads();
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = ClientInternalHelperImpl.INSTANCE.getParticleProviders();
}