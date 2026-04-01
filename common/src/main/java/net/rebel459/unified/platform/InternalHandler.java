package net.rebel459.unified.platform;

public interface InternalHandler {

    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.ParticleTypes createParticleTypes(String modId);
    UnifiedRegistries.MobEffects createMobEffects(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);
    UnifiedRegistries.EnchantmentCodecs createEnchantmentCodecs(String modId);
    UnifiedRegistries.MapDecorationTypes createMapDecorationTypes(String modId);

    HelpersImpl.CreativeEntries getCreativeEntries();
    HelpersImpl.Packs getPacks();
    HelpersImpl.Networking getNetworkPayloads();
    HelpersImpl.Platform getPlatform();
    HelpersImpl.BiomeModifications getBiomeModifications();
}