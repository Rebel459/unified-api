package net.rebel459.unified.impl.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.api.core.DataRegistry;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.platform.PlatformHandler;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DataRegistries {
    private static final Blocks BLOCKS = new Blocks();
    private static final Items ITEMS = new Items();

    private DataRegistries() {}

    public static UnifiedRegistries.Blocks blocks(String modId) {
        return new StagedBlocks(modId);
    }

    public static UnifiedRegistries.Items items(String modId) {
        return new StagedItems(modId);
    }

    private static final class Blocks extends DataRegistry<Block, BlockPlan, SuppliedBlock> {
        private Blocks() {
            super(Registries.BLOCK, Registries.SOUND_EVENT);
        }

        @Override
        protected SuppliedBlock createPlaceholder(Identifier id, BlockPlan plan,
                Supplier<? extends Block> value) {
            return new SuppliedBlock(() -> BuiltInRegistries.BLOCK, plan.id, value);
        }

        @Override
        protected SuppliedBlock actualRegister(Identifier id, BlockPlan plan) {
            UnifiedRegistries.Blocks platform = PlatformHandler.INSTANCE.createBlocks(id.getNamespace());
            return DataRegistries.actualRegister(platform, plan.id, plan.factory,
                    plan.properties, plan.blockEntity);
        }

        private SuppliedBlock stage(BlockPlan plan) {
            return super.stage(plan.id.block().identifier(), plan);
        }
    }

    private static final class Items extends DataRegistry<Item, ItemPlan, SuppliedItem> {
        private Items() {
            super(Registries.ITEM, Registries.BLOCK);
        }

        @Override
        protected SuppliedItem createPlaceholder(Identifier id, ItemPlan plan,
                Supplier<? extends Item> value) {
            return new SuppliedItem(() -> BuiltInRegistries.ITEM,
                    ResourceKey.create(Registries.ITEM, id), value);
        }

        @Override
        protected SuppliedItem actualRegister(Identifier id, ItemPlan plan) {
            return plan.registration.apply(PlatformHandler.INSTANCE.createItems(id.getNamespace()));
        }

        private SuppliedItem stageItem(Identifier id, ItemPlan plan) {
            return super.stage(id, plan);
        }
    }

    private static final class StagedBlocks implements UnifiedRegistries.Blocks {
        private final String modId;

        private StagedBlocks(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path,
                Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties) {
            SuppliedBlock block = stage(path, path, factory, properties, Optional.empty());
            items(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path,
                Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = stage(path, path, factory, properties, Optional.of(type));
            items(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path,
                Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties) {
            return registerWithoutItem(path, path, factory, properties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path,
                Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return registerWithoutItem(path, path, factory, properties, type);
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String blockPath, String itemPath,
                Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties) {
            return stage(blockPath, itemPath, factory, properties, Optional.empty());
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(
                String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> factory,
                Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return stage(blockPath, itemPath, factory, properties, Optional.of(type));
        }

        private SuppliedBlock stage(String blockPath, String itemPath,
                Function<BlockBehaviour.Properties, ? extends Block> factory,
                Supplier<BlockBehaviour.Properties> properties,
                Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
            Identifier blockId = Identifier.fromNamespaceAndPath(modId, blockPath);
            BlockItemId id = BlockItemId.create(blockId,
                    Identifier.fromNamespaceAndPath(modId, itemPath));
            return BLOCKS.stage(new BlockPlan(id, factory, properties, blockEntity));
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createBlocks(modId).addAlias(from, to);
        }
    }

    private static final class StagedItems implements UnifiedRegistries.Items {
        private final String modId;

        private StagedItems(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> factory,
                Supplier<Item.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            return ITEMS.stageItem(id, new ItemPlan(platform -> platform.register(path, factory, properties)));
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block,
                BiFunction<Block, Item.Properties, Item> factory,
                Supplier<Item.Properties> properties) {
            return registerBlockItem(block.blockItemId(), block, factory, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(BlockItemId id, Supplier<T> block,
                BiFunction<Block, Item.Properties, Item> factory,
                Supplier<Item.Properties> properties) {
            return ITEMS.stageItem(id.item().identifier(), new ItemPlan(platform ->
                    platform.registerBlockItem(id, block, factory, properties)));
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createItems(modId).addAlias(from, to);
        }
    }

    private record BlockPlan(BlockItemId id, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties, Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {}

    private record ItemPlan(Function<UnifiedRegistries.Items, SuppliedItem> registration) {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SuppliedBlock actualRegister(
            UnifiedRegistries.Blocks platform, 
            BlockItemId id,
            Function<BlockBehaviour.Properties, ? extends Block> factory,
            Supplier<BlockBehaviour.Properties> properties,
            Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
        String blockPath = id.block().identifier().getPath();
        String itemPath = id.item().identifier().getPath();
        if (blockEntity.isPresent()) {
            Supplier type = blockEntity.orElseThrow();
            return platform.registerWithoutItem(blockPath, itemPath, factory, properties, type);
        }
        return platform.registerWithoutItem(blockPath, itemPath, factory, properties);
    }
}
  