package net.rebel459.unified.api.client.core;

import net.rebel459.unified.impl.client.core.ClientHelpersImpl;
import net.rebel459.unified.impl.client.platform.ClientInternalHandlerImpl;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.Tooltips TOOLTIPS = ClientInternalHandlerImpl.INSTANCE.getTooltips();
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = ClientInternalHandlerImpl.INSTANCE.getEntityRenderers();
    public static ClientHelpersImpl.Networking NETWORKING = ClientInternalHandlerImpl.INSTANCE.getNetworking();
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = ClientInternalHandlerImpl.INSTANCE.getParticleProviders();
    public static ClientHelpersImpl.LegacyBabyArmor LEGACY_BABY_ARMOR = new ClientHelpersImpl.LegacyBabyArmor() {};
    public static ClientHelpersImpl.ResourcePacks RESOURCE_PACKS = ClientInternalHandlerImpl.INSTANCE.getResourcePacks();
}