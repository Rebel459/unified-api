package net.rebel459.unified.platform;

public interface PlatformHelper {

    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.ParticleTypes createParticleTypes(String modId);
    UnifiedRegistries.MobEffects createMobEffects(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    UnifiedHelpers.CreativeEntries getCreativeEntries();
    UnifiedHelpers.LootTables getLootTables();
    UnifiedHelpers.Packs getPacks();
    UnifiedHelpers.FurnaceFuels getFurnaceFuels();
    UnifiedHelpers.StrippableBlocks getStrippableBlocks();
    UnifiedHelpers.NetworkPayloads getNetworkPayloads();
    UnifiedHelpers.Platform getPlatform();
}