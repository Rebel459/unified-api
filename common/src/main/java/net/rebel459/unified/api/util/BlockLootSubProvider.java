package net.rebel459.unified.api.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.rebel459.unified.impl.util.LootProviderContext;

import java.util.Set;

public class BlockLootSubProvider extends net.minecraft.data.loot.BlockLootSubProvider {

    public static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};

    private final net.minecraft.data.loot.BlockLootSubProvider provider;

    public BlockLootSubProvider(HolderLookup.Provider registries, net.minecraft.data.loot.BlockLootSubProvider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), new LootProviderContext(registries));
        this.provider = provider;
    }

    @Override public void generate() {}

    @Override public Holder<LootItemCondition> hasSilkTouch() { return super.hasSilkTouch(); }
    @Override public LootItemCondition.Builder doesNotHaveSilkTouch() { return super.doesNotHaveSilkTouch(); }
    @Override public Holder<LootItemCondition> hasShears() { return super.hasShears(); }
    @Override public LootItemCondition.Builder hasShearsOrSilkTouch() { return super.hasShearsOrSilkTouch(); }
    @Override public LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() { return super.doesNotHaveShearsOrSilkTouch(); }

    @Override public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> builder) {
        return super.applyExplosionDecay(item, builder);
    }

    @Override public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> builder) {
        return super.applyExplosionCondition(item, builder);
    }

    @Override public LootTable.Builder createSingleItemTable(ItemLike item) { return super.createSingleItemTable(item); }

    public static LootTable.Builder createSelfDropDispatchTable(Block block, Holder<LootItemCondition> condition,
            LootPoolEntryContainer.Builder<?> alternative) {
        return BlockLootSubProvider.createSelfDropDispatchTable(block, condition, alternative);
    }

    @Override public LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> alternative) {
        return super.createSilkTouchDispatchTable(block, alternative);
    }

    @Override public LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> alternative) {
        return super.createShearsDispatchTable(block, alternative);
    }

    @Override public LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> alternative) {
        return super.createSilkTouchOrShearsDispatchTable(block, alternative);
    }

    @Override public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item) {
        return super.createSingleItemTableWithSilkTouch(block, item);
    }

    @Override public LootTable.Builder createSingleItemTable(ItemLike item, Holder<ContextIntProvider> count) {
        return super.createSingleItemTable(item, count);
    }

    @Override public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item, Holder<ContextIntProvider> count) {
        return super.createSingleItemTableWithSilkTouch(block, item, count);
    }

    @Override public LootTable.Builder createSilkTouchOnlyTable(ItemLike item) { return super.createSilkTouchOnlyTable(item); }
    @Override public LootTable.Builder createPotFlowerItemTable(ItemLike item) { return super.createPotFlowerItemTable(item); }
    @Override public LootTable.Builder createSlabItemTable(Block block) { return super.createSlabItemTable(block); }

    @Override public <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(
            Block block, Property<T> property, T value) {
        return super.createSinglePropConditionTable(block, property, value);
    }

    @Override public LootTable.Builder createNameableBlockEntityTable(Block block) { return super.createNameableBlockEntityTable(block); }
    @Override public LootTable.Builder createShulkerBoxDrop(Block block) { return super.createShulkerBoxDrop(block); }
    @Override public LootTable.Builder createCopperOreDrops(Block block) { return super.createCopperOreDrops(block); }
    @Override public LootTable.Builder createLapisOreDrops(Block block) { return super.createLapisOreDrops(block); }
    @Override public LootTable.Builder createRedstoneOreDrops(Block block) { return super.createRedstoneOreDrops(block); }
    @Override public LootTable.Builder createBannerDrop(Block block) { return super.createBannerDrop(block); }
    @Override public LootTable.Builder createBeeNestDrop(Block block) { return super.createBeeNestDrop(block); }
    @Override public LootTable.Builder createBeeHiveDrop(Block block) { return super.createBeeHiveDrop(block); }
    @Override public LootTable.Builder createCaveVinesDrop(Block block) { return super.createCaveVinesDrop(block); }
    @Override public LootTable.Builder createCopperGolemStatueBlock(Block block) { return super.createCopperGolemStatueBlock(block); }
    @Override public LootTable.Builder createOreDrop(Block block, Item item) { return super.createOreDrop(block, item); }
    @Override public LootTable.Builder createMushroomBlockDrop(Block block, ItemLike item) { return super.createMushroomBlockDrop(block, item); }
    @Override public LootTable.Builder createGrassDrops(Block block) { return super.createGrassDrops(block); }
    @Override public LootTable.Builder createStemDrops(Block block, Item item) { return super.createStemDrops(block, item); }
    @Override public LootTable.Builder createAttachedStemDrops(Block block, Item item) { return super.createAttachedStemDrops(block, item); }
    @Override public LootTable.Builder createShearsOnlyDrop(ItemLike item) { return super.createShearsOnlyDrop(item); }
    @Override public LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike item) { return super.createShearsOrSilkTouchOnlyDrop(item); }

    @Override public LootTable.Builder createMultifaceBlockDrops(Block block, Holder<LootItemCondition> condition) {
        return super.createMultifaceBlockDrops(block, condition);
    }

    @Override public LootTable.Builder createMultifaceBlockDrops(Block block) { return super.createMultifaceBlockDrops(block); }
    @Override public LootTable.Builder createMossyCarpetBlockDrops(Block block) { return super.createMossyCarpetBlockDrops(block); }
    @Override public LootTable.Builder createLeavesDrops(Block block, Block sapling, float... chances) { return super.createLeavesDrops(block, sapling, chances); }
    @Override public LootTable.Builder createOakLeavesDrops(Block block, Block sapling, float... chances) { return super.createOakLeavesDrops(block, sapling, chances); }
    @Override public LootTable.Builder createMangroveLeavesDrops(Block block) { return super.createMangroveLeavesDrops(block); }

    @Override public LootTable.Builder createCropDrops(Block block, Item mature, Item seeds, LootItemCondition.Builder condition) {
        return super.createCropDrops(block, mature, seeds, condition);
    }

    @Override public LootTable.Builder createDoublePlantShearsDrop(Block block) { return super.createDoublePlantShearsDrop(block); }
    @Override public LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block seedDrop) { return super.createDoublePlantWithSeedDrops(block, seedDrop); }
    @Override public LootTable.Builder createCandleDrops(Block block) { return super.createCandleDrops(block); }
    @Override public LootTable.Builder createSegmentedBlockDrops(Block block) { return super.createSegmentedBlockDrops(block); }
    public static LootTable.Builder createCandleCakeDrops(Block block) { return net.minecraft.data.loot.BlockLootSubProvider.createCandleCakeDrops(block); }
    public static LootTable.Builder noDrop() { return net.minecraft.data.loot.BlockLootSubProvider.noDrop(); }

    @Override public void addNetherVinesDropTable(Block head, Block plant) { provider.addNetherVinesDropTable(head, plant); }
    @Override public LootTable.Builder createDoorTable(Block block) { return super.createDoorTable(block); }
    @Override public void dropPottedContents(Block block) { provider.dropPottedContents(block); }
    @Override public void otherWhenSilkTouch(Block block, Block other) { provider.otherWhenSilkTouch(block, other); }
    @Override public void dropOther(Block block, ItemLike item) { provider.dropOther(block, item); }
    @Override public void dropWhenSilkTouch(Block block) { provider.dropWhenSilkTouch(block); }
    @Override public void dropSelf(Block block) { provider.dropSelf(block); }
}
