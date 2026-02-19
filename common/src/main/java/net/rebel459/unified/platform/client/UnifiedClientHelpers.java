package net.rebel459.unified.platform.client;

import net.rebel459.unified.platform.HelpersImpl;
import net.rebel459.unified.platform.PlatformHelperImpl;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.Tooltips TOOLTIPS = ClientPlatformHelperImpl.INSTANCE.getTooltips();
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = ClientPlatformHelperImpl.INSTANCE.getEntityRenderers();
    public static ClientHelpersImpl.Networking NETWORKING = ClientPlatformHelperImpl.INSTANCE.getNetworkPayloads();
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = ClientPlatformHelperImpl.INSTANCE.getParticleProviders();
}