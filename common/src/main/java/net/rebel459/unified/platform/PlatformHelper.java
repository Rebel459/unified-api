package net.rebel459.unified.platform;

public interface PlatformHelper {

    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.ItemComponents createItemComponents(String modId);
    UnifiedRegistries.Particles createParticles(String modId);
    UnifiedRegistries.MobEffects createMobEffects(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);

    UnifiedHelpers.CreativeEntries getCreativeEntries();
    UnifiedHelpers.LootTables getLootTables();
    UnifiedHelpers.Packs getPacks();
    UnifiedHelpers.FurnaceFuels getFurnaceFuels();
    UnifiedHelpers.StrippableBlocks getStrippableBlocks();
    UnifiedHelpers.NetworkPayloads getNetworkPayloads();
    UnifiedHelpers.Platform getPlatform();
}