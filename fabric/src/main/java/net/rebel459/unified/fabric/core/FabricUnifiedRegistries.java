package net.rebel459.unified.fabric.core;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.rebel459.unified.impl.util.BlockItemId;
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
import net.rebel459.unified.api.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record DeferredRegistry<Y>(String modId, Registry<Y> registry) implements UnifiedRegistries.DeferredRegistry<Y> {

        @Override
        public <T extends Y> Supplied<T> register(String path, Supplier<T> value) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<Y> key = ResourceKey.create(registry.key(), id);
            Supplied<T> existing = StagedRegistry.getClaimed(key);
            if (existing != null) return existing;
            Holder.Reference<T> holder = Registry.registerForHolder(registry, id, value.get());
            return new Supplied<>(key, holder::value, () -> holder);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            registry.addAlias(convertedFrom, convertedTo);
        }
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            SuppliedItem existing = StagedRegistry.getClaimed(resourceKey);
            if (existing != null) return existing;
            var item = net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey));
            return new SuppliedItem(resourceKey, () -> item, () -> BuiltInRegistries.ITEM.getOrThrow(resourceKey));
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return registerBlockItem(block.blockItemId(), block, function, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(BlockItemId id, Supplier<T> block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            SuppliedItem existing = StagedRegistry.getClaimed(id.item());
            if (existing != null) return existing;
            Block value = block.get();
            Item item = net.minecraft.world.item.Items.registerItem(
                    id.item(),
                    settings -> function.apply(value, settings),
                    properties.get().useBlockDescriptionPrefix().requiredFeatures(value.requiredFeatures()));
            return new SuppliedItem(id.item(), () -> item, () -> BuiltInRegistries.ITEM.getOrThrow(id.item()));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.ITEM.addAlias(convertedFrom, convertedTo);
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties) {
            Identifier blockId = Identifier.fromNamespaceAndPath(modId, path);
            BlockItemId blockItemId = BlockItemId.create(blockId, blockId);
            SuppliedBlock existing = StagedRegistry.getClaimed(blockItemId.block());
            if (existing != null) return existing;
            Block block = Registry.register(BuiltInRegistries.BLOCK, blockItemId.block(), function.apply(blockProperties.get().setId(blockItemId.block())));
            net.minecraft.world.item.Items.registerBlock(block, BlockItem::new, new Item.Properties());
            return new SuppliedBlock(blockItemId, () -> block, () -> BuiltInRegistries.BLOCK.getOrThrow(blockItemId.block()));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock existing = StagedRegistry.getClaimed(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, path)));
            if (existing != null) return existing;
            SuppliedBlock block = register(path, function, blockProperties);
            type.get().addValidBlock(block.get());
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            return registerWithoutItem(path, path, function, properties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return registerWithoutItem(path, path, function, properties, type);
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, blockPath));
            SuppliedBlock existing = StagedRegistry.getClaimed(key);
            if (existing != null) return existing;
            Block block = Registry.register(BuiltInRegistries.BLOCK, key, function.apply(properties.get().setId(key)));
            return new SuppliedBlock(BlockItemId.create(key.identifier(), Identifier.fromNamespaceAndPath(modId, itemPath)), () -> block, () -> BuiltInRegistries.BLOCK.getOrThrow(key));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String blockPath, String itemPath, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = registerWithoutItem(blockPath, itemPath, function, properties);
            type.get().addValidBlock(block.get());
            return block;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.BLOCK.addAlias(convertedFrom, convertedTo);
        }
    }

    public record DataComponentTypes(String modId) implements UnifiedRegistries.DataComponentTypes {

        @Override
        public <T> Supplied<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(modId, path));
            Supplied<DataComponentType<T>> existing = StagedRegistry.getClaimed(key);
            if (existing != null) return existing;
            DataComponentType<T> component = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, unaryOperator.apply(DataComponentType.builder()).build());
            return new Supplied<>(key, () -> component, () -> BuiltInRegistries.DATA_COMPONENT_TYPE.getOrThrow(key));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplied<EntityType<T>> register(String path, @NotNull Supplier<EntityType.Builder<T>> builder) {
            ResourceKey<EntityType<?>> resourceKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path));
            Supplied<EntityType<T>> existing = StagedRegistry.getClaimed(resourceKey);
            if (existing != null) return existing;
            EntityType<T> entity = Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.get().build(resourceKey));
            return new Supplied<>(resourceKey, () -> entity, () -> BuiltInRegistries.ENTITY_TYPE.getOrThrow(resourceKey));
        }

        @Override
        public <T extends LivingEntity> Supplied<EntityType<T>> register(String path, Supplier<EntityType.Builder<T>> builder, Supplier<AttributeSupplier> attributes) {
            Supplied<EntityType<T>> entity = register(path, builder);
            FabricDefaultAttributeRegistry.register(entity.get(), attributes.get());
            return entity;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.ENTITY_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record SoundEvents(String modId) implements UnifiedRegistries.SoundEvents {

        @Override
        public Supplied<SoundEvent> register(String path) {
            return register(path, -1F);
        }
        @Override
        public Supplied<SoundEvent> register(String path, float fixedRange) {
            ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath(modId, path));
            Supplied<SoundEvent> existing = StagedRegistry.getClaimed(key);
            if (existing != null) return existing;
            SoundEvent rangeType = SoundEvent.createVariableRangeEvent(key.identifier());
            if (fixedRange >= 0F) rangeType = SoundEvent.createFixedRangeEvent(key.identifier(), fixedRange);
            SoundEvent finalRangeType = rangeType;
            SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, key, finalRangeType);
            return new Supplied<>(key, () -> sound, () -> BuiltInRegistries.SOUND_EVENT.getOrThrow(key));
        }
    }

}
