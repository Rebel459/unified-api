package net.rebel459.unified.platform;

import net.minecraft.core.Registry;

public class NeoForgeInternalHandler implements InternalHandler {

    @Override
    public UnifiedRegistries.DeferredRegistry createDeferredRegistry(String modId, Registry<?> registry) {
        return new NeoForgeUnifiedRegistries.DeferredRegistry(modId, registry);
    }

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new NeoForgeUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new NeoForgeUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new NeoForgeUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new NeoForgeUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new NeoForgeUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new NeoForgeHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new NeoForgeHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworkPayloads() {
        return new NeoForgeHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new NeoForgeUnifiedPlatform();
    }

    @Override
    public HelpersImpl.BiomeModifications getBiomeModifications() {
        return new NeoForgeHelpersImpl.BiomeModifications();
    }
}