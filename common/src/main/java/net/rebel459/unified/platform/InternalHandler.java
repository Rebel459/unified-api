package net.rebel459.unified.platform;

import net.minecraft.core.Registry;

public interface InternalHandler {

    UnifiedRegistries.DeferredRegistry createDeferredRegistry(String modId, Registry<?> registry);
    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    HelpersImpl.CreativeEntries getCreativeEntries();
    HelpersImpl.Packs getPacks();
    HelpersImpl.Networking getNetworkPayloads();
    HelpersImpl.Platform getPlatform();
    HelpersImpl.BiomeModifications getBiomeModifications();
}