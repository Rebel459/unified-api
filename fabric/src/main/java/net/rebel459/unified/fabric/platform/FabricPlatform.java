package net.rebel459.unified.fabric.platform;

import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.fabric.core.FabricHelpers;
import net.rebel459.unified.fabric.core.FabricInstance;
import net.rebel459.unified.fabric.core.FabricUnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.platform.CommonPlatform;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;

public class FabricPlatform implements CommonPlatform {

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
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new FabricUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new FabricUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public CommonInstance getInstance() {
        return new FabricInstance();
    }

    @Override
    public CommonHelpers.CreativeEntries getCreativeEntries() {
        return new FabricHelpers.CreativeEntries();
    }

    @Override
    public CommonHelpers.DataPacks getDataPacks() {
        return new FabricHelpers.DataPacks();
    }

    @Override
    public CommonHelpers.Networking getNetworking() {
        return new FabricHelpers.Networking();
    }

    @Override
    public CommonHelpers.BiomeModifications getBiomeModifications() {
        return new FabricHelpers.BiomeModifications();
    }

    @Override
    public CommonHelpers.ReloadListeners getReloadListeners() {
        return new FabricHelpers.ReloadListeners();
    }

    @Override
    public CommonPlatform.Impl impl() {
        return new Impl();
    }

    public static class Impl implements CommonPlatform.Impl {

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return (from, to) -> OxidizableBlocksRegistry.registerNextStage(from.asBlock(), to.asBlock());
        }
    }
}