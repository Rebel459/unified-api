package net.rebel459.unified.impl.core;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.platform.PlatformHandler;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class StagedRegistries {
    private StagedRegistries() {}

    public static final class StagedDeferredRegistry<T> implements UnifiedRegistries.DeferredRegistry<T> {
        private final String modId;
        private final Registry<T> registry;

        public StagedDeferredRegistry(String modId, Registry<T> registry) {
            this.modId = modId;
            this.registry = registry;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <Y extends T> Supplied<Y> register(String path, Supplier<Y> value) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<T> key = ResourceKey.create(registry.key(), id);
            return (Supplied<Y>) StagedRegistry.stage(registry, id,
                    (supplied, holder) -> new Supplied<>(key, supplied, holder),
                    () -> (Supplied<T>) PlatformHandler.INSTANCE.createDeferredRegistry(modId, registry).register(path, value)
            );
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createDeferredRegistry(modId, registry).addAlias(from, to);
        }
    }

    public static final class StagedItems implements UnifiedRegistries.Items {
        private final String modId;

        public StagedItems(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> factory, Supplier<Item.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            return StagedRegistry.stage(BuiltInRegistries.ITEM, id,
                    (value, holder) -> new SuppliedItem(key, value, holder),
                    () -> PlatformHandler.INSTANCE.createItems(modId).register(path, factory, properties));
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block,
                BiFunction<Block, Item.Properties, Item> factory,
                Supplier<Item.Properties> properties) {
            return registerBlockItem(block.blockItemId(), block, factory, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(BlockItemId id, Supplier<T> block, BiFunction<Block, Item.Properties, Item> factory, Supplier<Item.Properties> properties) {
            Identifier itemId = id.item().identifier();
            return StagedRegistry.stage(BuiltInRegistries.ITEM, itemId,
                    (value, holder) -> new SuppliedItem(id.item(), value, holder),
                    () -> PlatformHandler.INSTANCE.createItems(modId).registerBlockItem(id, block, factory, properties));
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createItems(modId).addAlias(from, to);
        }
    }

    public static final class StagedBlocks implements UnifiedRegistries.Blocks {
        private final String modId;

        public StagedBlocks(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
            SuppliedBlock block = registerWithoutItem(path, factory, properties);
            new StagedItems(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = registerWithoutItem(path, factory, properties, type);
            new StagedItems(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
            return registerWithoutItem(path, path, factory, properties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return registerWithoutItem(path, path, factory, properties, type);
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
            return register(blockPath, itemPath, factory, properties, Optional.empty());
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return register(blockPath, itemPath, factory, properties, Optional.of(type));
        }

        private SuppliedBlock register(String blockPath, String itemPath, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties, Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
            Identifier blockId = Identifier.fromNamespaceAndPath(modId, blockPath);
            BlockItemId id = BlockItemId.create(blockId, Identifier.fromNamespaceAndPath(modId, itemPath));
            return StagedRegistry.stage(BuiltInRegistries.BLOCK, blockId,
                    (value, holder) -> new SuppliedBlock(id, value, holder),
                    () -> actualRegister(PlatformHandler.INSTANCE.createBlocks(modId), id, factory, properties, blockEntity));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static SuppliedBlock actualRegister(UnifiedRegistries.Blocks registry, BlockItemId id, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties, Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
            String blockPath = id.block().identifier().getPath();
            String itemPath = id.item().identifier().getPath();
            if (blockEntity.isPresent()) {
                Supplier type = blockEntity.orElseThrow();
                return registry.registerWithoutItem(blockPath, itemPath, factory, properties, type);
            }
            return registry.registerWithoutItem(blockPath, itemPath, factory, properties);
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createBlocks(modId).addAlias(from, to);
        }
    }

    public static final class StagedDataComponentTypes implements UnifiedRegistries.DataComponentTypes {
        private final String modId;

        public StagedDataComponentTypes(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Supplied<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> builder) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, id);
            return (Supplied<DataComponentType<T>>) (Supplied<?>) StagedRegistry.stage(BuiltInRegistries.DATA_COMPONENT_TYPE, id,
                    (value, holder) -> new Supplied<>(key, value, holder),
                    () -> (Supplied<DataComponentType<?>>) (Supplied<?>) PlatformHandler.INSTANCE.createDataComponentTypes(modId).register(path, builder)
            );
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createDataComponentTypes(modId).addAlias(from, to);
        }
    }

    public static final class StagedEntityTypes implements UnifiedRegistries.EntityTypes {
        private final String modId;

        public StagedEntityTypes(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public <T extends Entity> Supplied<EntityType<T>> register(String path,
                EntityType.Builder<T> builder) {
            return register(path, registry -> registry.register(path, builder));
        }

        @Override
        public <T extends LivingEntity> Supplied<EntityType<T>> register(String path,
                EntityType.Builder<T> builder, Supplier<AttributeSupplier> attributes) {
            return register(path, registry -> registry.register(path, builder, attributes));
        }

        @SuppressWarnings("unchecked")
        private <T extends Entity> Supplied<EntityType<T>> register(String path, Function<UnifiedRegistries.EntityTypes, Supplied<EntityType<T>>> registration) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
            return (Supplied<EntityType<T>>) (Supplied<?>) StagedRegistry.stage(
                    BuiltInRegistries.ENTITY_TYPE, id,
                    (value, holder) -> new Supplied<>(key, value, holder),
                    () -> (Supplied<EntityType<?>>) (Supplied<?>) registration.apply(PlatformHandler.INSTANCE.createEntityTypes(modId))
            );
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createEntityTypes(modId).addAlias(from, to);
        }
    }

    public static final class StagedSoundEvents implements UnifiedRegistries.SoundEvents {
        private final String modId;

        public StagedSoundEvents(String modId) {
            this.modId = modId;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public Supplied<SoundEvent> register(String path) {
            return register(path, registry -> registry.register(path));
        }

        @Override
        public Supplied<SoundEvent> register(String path, float fixedRange) {
            return register(path, registry -> registry.register(path, fixedRange));
        }

        private Supplied<SoundEvent> register(String path, Function<UnifiedRegistries.SoundEvents, Supplied<SoundEvent>> registration) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, id);
            return StagedRegistry.stage(BuiltInRegistries.SOUND_EVENT, id,
                    (value, holder) -> new Supplied<>(key, value, holder),
                    () -> registration.apply(PlatformHandler.INSTANCE.createSoundEvents(modId))
            );
        }
    }
}
