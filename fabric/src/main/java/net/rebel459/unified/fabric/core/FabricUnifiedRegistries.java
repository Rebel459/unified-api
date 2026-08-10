package net.rebel459.unified.fabric.core;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.util.BlockLike;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record DeferredRegistry<Y>(String modId, Registry<Y> registry) implements UnifiedRegistries.DeferredRegistry<Y> {

        @Override
        public <T extends Y> Supplied<T> register(String path, Supplier<T> value) {
            ResourceKey<Y> key = ResourceKey.create(registry.key(), Identifier.fromNamespaceAndPath(modId, path));
            var registered = Registry.register(registry, key, value.get());
            Supplier<T> supplied = () -> registered;
            return new Supplied<>(() -> registry, key, supplied);
        }

        @Override
        public <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value) {
            return Registry.registerForHolder(registry, Identifier.fromNamespaceAndPath(modId, path), value.get());
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            registry.addAlias(convertedFrom, convertedTo);
        }
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            var item = net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey));
            Supplier<Item> supplied = () -> item;
            return new SuppliedItem(() -> BuiltInRegistries.ITEM, resourceKey, supplied);
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return registerBlockItem(block.identifier().getPath(), block, function, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var itemId = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            Item item = net.minecraft.world.item.Items.registerBlock(new BlockItemId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, path)), itemId), block.get(), function, properties.get());
            Supplier<Item> supplied = () -> item;
            return new SuppliedItem(() -> BuiltInRegistries.ITEM, itemId, supplied);
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

            Block block = Registry.register(BuiltInRegistries.BLOCK, blockItemId.block(), function.apply(blockProperties.get().setId(blockItemId.block())));
            Item item = net.minecraft.world.item.Items.registerBlock(blockItemId, block, BlockItem::new, new Item.Properties());

            Supplier<Block> suppliedBlock = () -> block;
            Supplier<Item> suppliedItem = () -> item;
            
            return new SuppliedBlock(() -> BuiltInRegistries.BLOCK, blockItemId.block(), suppliedBlock, new SuppliedItem(() -> BuiltInRegistries.ITEM, blockItemId.item(), suppliedItem));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = register(path, function, blockProperties);
            type.get().addValidBlock(block.get());
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
            Block block = Registry.register(BuiltInRegistries.BLOCK, key, function.apply(properties.get().setId(key)));
            Supplier<Block> supplied = () -> block;
            return new SuppliedBlock(() -> BuiltInRegistries.BLOCK, key, supplied, null);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = registerWithoutItem(path, function, properties);
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
            DataComponentType<T> component = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, unaryOperator.apply(DataComponentType.builder()).build());
            Supplier<DataComponentType<T>> supplied = () -> component;
            return new Supplied<>(() -> BuiltInRegistries.DATA_COMPONENT_TYPE, key, supplied);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplied<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            ResourceKey<EntityType<?>> resourceKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path));
            EntityType<T> entity = Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey));
            Supplier<EntityType<T>> supplied = () -> entity;
            return new Supplied<>(() -> BuiltInRegistries.ENTITY_TYPE, resourceKey, supplied);
        }

        @Override
        public <T extends LivingEntity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder, Supplier<AttributeSupplier> attributes) {
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
            SoundEvent rangeType = SoundEvent.createVariableRangeEvent(key.identifier());
            if (fixedRange >= 0F) rangeType = SoundEvent.createFixedRangeEvent(key.identifier(), fixedRange);
            SoundEvent finalRangeType = rangeType;
            SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, key, finalRangeType);
            Supplier<SoundEvent> supplied = () -> sound;
            return new Supplied<>(() -> BuiltInRegistries.SOUND_EVENT, key, supplied);
        }

        @Override
        public Holder<SoundEvent> registerForHolder(String path) {
            return registerForHolder(path, -1F);
        }
        @Override
        public Holder<SoundEvent> registerForHolder(String path, float fixedRange) {
            Identifier identifier = Identifier.fromNamespaceAndPath(modId, path);
            SoundEvent rangeType = SoundEvent.createVariableRangeEvent(identifier);
            if (fixedRange >= 0F) rangeType = SoundEvent.createFixedRangeEvent(identifier, fixedRange);
            SoundEvent finalRangeType = rangeType;
            return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, identifier, finalRangeType);
        }
    }
}
