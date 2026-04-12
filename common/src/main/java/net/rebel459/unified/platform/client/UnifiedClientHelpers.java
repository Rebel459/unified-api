package net.rebel459.unified.platform.client;

import net.rebel459.unified.client.util.helper.LegacyBabyArmor;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.Tooltips TOOLTIPS = ClientInternalHandlerImpl.INSTANCE.getTooltips();
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = ClientInternalHandlerImpl.INSTANCE.getEntityRenderers();
    public static ClientHelpersImpl.Networking NETWORKING = ClientInternalHandlerImpl.INSTANCE.getNetworking();
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = ClientInternalHandlerImpl.INSTANCE.getParticleProviders();
    public static LegacyBabyArmor LEGACY_BABY_ARMOR = new LegacyBabyArmor() {};
}