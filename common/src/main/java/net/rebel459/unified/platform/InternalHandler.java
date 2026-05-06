package net.rebel459.unified.platform;

import net.minecraft.core.Registry;
import net.rebel459.unified.util.helper.BlockConversionsImpl;
import org.jetbrains.annotations.ApiStatus;

public interface InternalHandler {

    <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry);
    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    HelpersImpl.CreativeEntries getCreativeEntries();
    HelpersImpl.Packs getPacks();
    HelpersImpl.Networking getNetworking();
    HelpersImpl.Platform getPlatform();
    HelpersImpl.BiomeModifications getBiomeModifications();

    @ApiStatus.Internal
    Impl impl();

    @ApiStatus.Internal
    interface Impl {
        BlockConversionsImpl.Oxidizables getOxidizables();
    }
}