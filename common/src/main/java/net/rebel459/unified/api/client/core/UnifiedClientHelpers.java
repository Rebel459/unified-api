package net.rebel459.unified.api.client.core;

import net.rebel459.unified.impl.client.core.CommonClientHelpers;
import net.rebel459.unified.impl.client.platform.ClientPlatformHandler;

public class UnifiedClientHelpers {

    public static CommonClientHelpers.Tooltips TOOLTIPS = ClientPlatformHandler.INSTANCE.getTooltips();
    public static CommonClientHelpers.EntityRenderers ENTITY_RENDERERS = ClientPlatformHandler.INSTANCE.getEntityRenderers();
    public static CommonClientHelpers.Networking NETWORKING = ClientPlatformHandler.INSTANCE.getNetworking();
    public static CommonClientHelpers.ParticleProviders PARTICLE_PROVIDERS = ClientPlatformHandler.INSTANCE.getParticleProviders();
    public static CommonClientHelpers.SimpleBabyArmor SIMPLE_BABY_ARMOR = new CommonClientHelpers.SimpleBabyArmor() {};
    public static CommonClientHelpers.ResourcePacks RESOURCE_PACKS = ClientPlatformHandler.INSTANCE.getResourcePacks();
}