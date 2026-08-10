package net.rebel459.unified.neoforge.platform;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;
import net.rebel459.unified.impl.core.CommonInstance;
import net.rebel459.unified.impl.platform.CommonPlatform;
import net.rebel459.unified.impl.helper.BlockConversionsImpl;
import net.rebel459.unified.neoforge.core.NeoForgeHelpers;
import net.rebel459.unified.neoforge.core.NeoForgeInstance;
import net.rebel459.unified.neoforge.core.NeoForgeUnifiedRegistries;

import java.util.HashMap;
import java.util.function.Supplier;

public class NeoForgePlatform implements CommonPlatform {

    @Override
    public <Y> UnifiedRegistries.DeferredRegistry<Y> createDeferredRegistry(String modId, Registry<Y> registry) {
        return new NeoForgeUnifiedRegistries.DeferredRegistry<>(modId, registry);
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
    public CommonInstance getInstance() {
        return new NeoForgeInstance();
    }

    @Override
    public CommonHelpers.CreativeEntries getCreativeEntries() {
        return new NeoForgeHelpers.CreativeEntries();
    }

    @Override
    public CommonHelpers.DataPacks getDataPacks() {
        return new NeoForgeHelpers.DataPacks();
    }

    @Override
    public CommonHelpers.Networking getNetworking() {
        return new NeoForgeHelpers.Networking();
    }

    @Override
    public CommonHelpers.BiomeModifications getBiomeModifications() {
        return new NeoForgeHelpers.BiomeModifications();
    }

    @Override
    public CommonHelpers.ReloadListeners getReloadListeners() {
        return new NeoForgeHelpers.ReloadListeners();
    }

    @Override
    public CommonPlatform.Impl impl() {
        return new Impl();
    }

    public static class Impl implements CommonPlatform.Impl {

        public static HashMap<Block, Block> OXIDIZABLES = new HashMap<>();

        @Override
        public BlockConversionsImpl.Oxidizables getOxidizables() {
            return (from, to) -> {
                OXIDIZABLES.put(from.asBlock(), to.asBlock());

                for (BlockState state : from.asBlock().getStateDefinition().getPossibleStates()) {
                    state.initCache();
                }
            };
        }

        @Override
        public CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            return new CreativeModeTab.Builder(row, column)
                    .title(displayName)
                    .icon(iconGenerator)
                    .displayItems(displayItemsGenerator)
                    .build();
        }
    }
}
