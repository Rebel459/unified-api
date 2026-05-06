package net.rebel459.unified.platform;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.BlockLike;
import net.rebel459.unified.util.SuppliedBlock;
import net.rebel459.unified.util.SuppliedItem;
import net.rebel459.unified.util.registry.SuppliedBlockImpl;
import net.rebel459.unified.util.registry.SuppliedItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record DeferredRegistry<Y>(String modId, Registry registry) implements UnifiedRegistries.DeferredRegistry<Y> {

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Y> Supplier<T> register(String path, Supplier<T> value) {
            var thing = (T) Registry.register(registry, Identifier.fromNamespaceAndPath(modId, path), value.get());
            var suppliedThing = Suppliers.memoize(() -> thing);
            suppliedThing.get();
            return suppliedThing;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value) {
            return Registry.registerForHolder(registry, Identifier.fromNamespaceAndPath(modId, path), value.get());
        }

        @Override
        public <T extends Y> Holder<T> registerHolder(String path, Supplier<T> value) {
            return registerForHolder(path, value);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            registry.addAlias(convertedFrom, convertedTo);
        }
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey));
            return new SuppliedItemImpl(BuiltInRegistries.ITEM.getOrThrow(resourceKey));
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            var item = net.minecraft.world.item.Items.registerBlock(blockSupplier.get(), properties.get());
            ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).get();
            return new SuppliedItemImpl(BuiltInRegistries.ITEM.getOrThrow(key));
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
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);

            var block = Registry.register(BuiltInRegistries.BLOCK, blockKey, function.apply(blockProperties.get().setId(blockKey)));
            var blockItem = UnifiedRegistries.Items.create(modId).registerBlockItem(path, () -> block, Item.Properties::new);

            return new SuppliedBlockImpl(BuiltInRegistries.BLOCK.getOrThrow(blockKey), blockItem);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, BlockEntityType<Y> type) {
            SuppliedBlock block = register(path, function, blockProperties);
            type.addValidBlock(block.get());
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, path));
            Registry.register(BuiltInRegistries.BLOCK, key, function.apply(properties.get().setId(key)));
            return new SuppliedBlockImpl(BuiltInRegistries.BLOCK.getOrThrow(key));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, BlockEntityType<Y> type) {
            SuppliedBlock block = registerWithoutItem(path, function, properties);
            type.addValidBlock(block.get());
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties) {
            var item = new Items(modId).register(path, itemFunction, itemProperties);
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, path));
            Registry.register(BuiltInRegistries.BLOCK, key, blockFunction.apply(blockProperties.get().setId(key)));
            return new SuppliedBlockImpl(BuiltInRegistries.BLOCK.getOrThrow(key), item);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            var block = register(path, blockFunction, blockProperties, itemFunction, itemProperties);
            type.addValidBlock(block.get());
            return block;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.BLOCK.addAlias(convertedFrom, convertedTo);
        }
    }

    public record CreativeTabs(String modId) implements UnifiedRegistries.CreativeTabs {

        @Override
        public ResourceKey<CreativeModeTab> register(String path, Supplier<? extends ItemLike> icon) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            CreativeModeTab tab = FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(icon.get()))
                    .title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
                    .displayItems((params, output) -> {})
                    .build();
            ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), id);
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);
            return tabKey;
        }
    }

    public record DataComponentTypes(String modId) implements UnifiedRegistries.DataComponentTypes {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            var component = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(modId, path), unaryOperator.apply(DataComponentType.builder()).build()));
            component.get();
            return component;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplier<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            ResourceKey<EntityType<?>> resourceKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path));
            var entity = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey)));
            entity.get();
            return entity;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.ENTITY_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record BlockEntityTypes(String modId) implements UnifiedRegistries.BlockEntityTypes {

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder) {
            return register(path, builder, Set.of());
        }

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder, BlockLike... blocks) {
            Set<Block> set = new HashSet<>();
            for (BlockLike blockLike : blocks) {
                set.add(blockLike.asBlock());
            }
            return register(path, builder, set);
        }

        private @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder, Set<Block> set) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            Util.fetchChoiceType(References.BLOCK_ENTITY, id.toString());
            var blockEntity = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, new BlockEntityType<>(builder, set)));
            blockEntity.get();
            return blockEntity;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.BLOCK_ENTITY_TYPE.addAlias(convertedFrom, convertedTo);
        }
    }

    public record SoundEvents(String modId) implements UnifiedRegistries.SoundEvents {

        @Override
        public Supplier<SoundEvent> register(String path) {
            return register(path, -1F);
        }
        @Override
        public Supplier<SoundEvent> register(String path, float fixedRange) {
            Identifier identifier = Identifier.fromNamespaceAndPath(modId, path);
            SoundEvent rangeType = SoundEvent.createVariableRangeEvent(identifier);
            if (fixedRange >= 0F) rangeType = SoundEvent.createFixedRangeEvent(identifier, fixedRange);
            SoundEvent finalRangeType = rangeType;
            var soundEvent = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, finalRangeType));
            soundEvent.get();
            return soundEvent;
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

        @Override
        public Holder<SoundEvent> registerHolder(String path) {
            return registerForHolder(path);
        }
        @Override
        public Holder<SoundEvent> registerHolder(String path, float fixedRange) {
            return registerForHolder(path, fixedRange);
        }
    }
}
