package net.rebel459.unified.impl.platform;

import net.minecraft.core.Registry;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;
import org.jetbrains.annotations.ApiStatus;

public interface CommonPlatform {

    <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry);
    UnifiedRegistries.Items createItems(String modId);
    UnifiedRegistries.Blocks createBlocks(String modId);
    UnifiedRegistries.CreativeTabs createCreativeTabs(String modId);
    UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId);
    UnifiedRegistries.EntityTypes createEntityTypes(String modId);
    UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId);
    UnifiedRegistries.SoundEvents createSoundEvents(String modId);

    CommonInstance getInstance();
    CommonHelpers.CreativeEntries getCreativeEntries();
    CommonHelpers.DataPacks getDataPacks();
    CommonHelpers.Networking getNetworking();
    CommonHelpers.BiomeModifications getBiomeModifications();

    @ApiStatus.Internal
    Impl impl();

    @ApiStatus.Internal
    interface Impl {
        BlockConversionsImpl.Oxidizables getOxidizables();
    }
}