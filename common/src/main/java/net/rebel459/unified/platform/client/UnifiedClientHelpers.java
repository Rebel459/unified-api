package net.rebel459.unified.platform.client;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.rebel459.unified.client.util.helper.LegacyBabyArmor;

public class UnifiedClientHelpers {

    public static ClientHelpersImpl.Tooltips TOOLTIPS = ClientInternalHelperImpl.INSTANCE.getTooltips();
    public static ClientHelpersImpl.EntityRenderers ENTITY_RENDERERS = ClientInternalHelperImpl.INSTANCE.getEntityRenderers();
    public static ClientHelpersImpl.Networking NETWORKING = ClientInternalHelperImpl.INSTANCE.getNetworkPayloads();
    public static ClientHelpersImpl.ParticleProviders PARTICLE_PROVIDERS = ClientInternalHelperImpl.INSTANCE.getParticleProviders();
    public static LegacyBabyArmor LEGACY_BABY_ARMOR = new LegacyBabyArmor() {};
}