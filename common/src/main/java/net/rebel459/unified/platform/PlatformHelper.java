package net.rebel459.unified.platform;

public interface PlatformHelper {

    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.ParticleTypes createParticleTypes(String modId);
    UnifiedRegistries.MobEffects createMobEffects(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    HelpersImpl.CreativeEntries getCreativeEntries();
    HelpersImpl.LootTables getLootTables();
    HelpersImpl.Packs getPacks();
    HelpersImpl.FurnaceFuels getFurnaceFuels();
    HelpersImpl.StrippableBlocks getStrippableBlocks();
    HelpersImpl.Networking getNetworkPayloads();
    HelpersImpl.Platform getPlatform();
}