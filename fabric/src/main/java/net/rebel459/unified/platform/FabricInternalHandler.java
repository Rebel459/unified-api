package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.rebel459.unified.util.helper.impl.BlockConversionsImpl;

public class FabricInternalHandler implements InternalHandler {

    @Override
    public <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry) {
        return new FabricUnifiedRegistries.DeferredRegistry<>(modId, registry);
    }

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new FabricUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new FabricUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new FabricUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new FabricUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new FabricUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new FabricUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new FabricHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new FabricHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworking() {
        return new FabricHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new FabricUnifiedPlatform();
    }

    @Override
    public HelpersImpl.BiomeModifications getBiomeModifications() {
        return new FabricHelpersImpl.BiomeModifications();
    }

    @Override
    public HelpersImpl.ReloadListeners getReloadListeners() {
        return new FabricHelpersImpl.ReloadListeners();
    }

    @Override
    public InternalHandler.Impl impl() {
        return new Impl();
    }

    public static class Impl implements InternalHandler.Impl {

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return OxidizableBlocksRegistry::registerNextStage;
        }
    }
}