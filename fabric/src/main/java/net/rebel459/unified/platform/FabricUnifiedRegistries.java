package net.rebel459.unified.platform;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.rebel459.unified.util.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedBlockImpl;
import net.rebel459.unified.util.SuppliedItem;
import net.rebel459.unified.util.registry.SuppliedItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            var item = Holder.direct(net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey)));
            return new SuppliedItemImpl(item);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            var item = Holder.direct(net.minecraft.world.item.Items.registerBlock(blockSupplier.get(), properties.get()));
            return new SuppliedItemImpl(item);
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

            var block = Holder.direct((Block) Registry.register(BuiltInRegistries.BLOCK, blockId, function.apply(blockProperties.get().setId(blockKey))));
            var suppliedBlock = new SuppliedBlockImpl(block);

            UnifiedRegistries.Items.create(modId).registerBlockItem(path, suppliedBlock, Item.Properties::new);

            return suppliedBlock;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, BlockEntityType<Y> type) {
            SuppliedBlock block = register(path, function, blockProperties);
            type.addValidBlock(block.get());
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            var block = Holder.direct((Block) Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.get().setId(ResourceKey.create(Registries.BLOCK, id)))));
            return new SuppliedBlockImpl(block);
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
            new Items(modId).register(path, itemFunction, itemProperties);
            return registerWithoutItem(path, blockFunction, blockProperties);
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
            new Items(modId).register(path, itemFunction, itemProperties);
            return registerWithoutItem(path, blockFunction, blockProperties, type);
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

    public record ParticleTypes(String modId) implements UnifiedRegistries.ParticleTypes {

        @Override
        public <T extends ParticleType> Supplier<T> register(String path, ParticleType type) {
            var particle = Suppliers.memoize(() -> (T) Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(modId, path), type));
            particle.get();
            return particle;
        }
    }

    public record MobEffects(String modId) implements UnifiedRegistries.MobEffects {

        @Override
        public Holder<MobEffect> register(String path, MobEffect effect) {
            return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(modId, path), effect);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BuiltInRegistries.MOB_EFFECT.addAlias(convertedFrom, convertedTo);
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
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder, Block... blocks) {
            return register(path, builder, Set.of(blocks));
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
            Identifier identifier = Identifier.fromNamespaceAndPath(modId, path);
            var soundEvent = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier)));
            soundEvent.get();
            return soundEvent;
        }

        @Override
        public Holder<SoundEvent> registerHolder(String path) {
            Identifier identifier = Identifier.fromNamespaceAndPath(modId, path);
            return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
        }
    }

    public record EnchantmentCodecs(String modId) implements UnifiedRegistries.EnchantmentCodecs {

        @Override
        public void registerProvider(String path, MapCodec<? extends EnchantmentProvider> codec) {
            Registry.register(BuiltInRegistries.ENCHANTMENT_PROVIDER_TYPE, Identifier.fromNamespaceAndPath(modId, path), codec);
        }

        @Override
        public void registerLevelBasedValue(String path, MapCodec<? extends LevelBasedValue> codec) {
            Registry.register(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, Identifier.fromNamespaceAndPath(modId, path), codec);
        }

        @Override
        public void registerEntityEffect(String path, MapCodec<? extends EnchantmentEntityEffect> codec) {
            Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.fromNamespaceAndPath(modId, path), codec);
        }

        @Override
        public void registerValueEffect(String path, MapCodec<? extends EnchantmentValueEffect> codec) {
            Registry.register(BuiltInRegistries.ENCHANTMENT_VALUE_EFFECT_TYPE, Identifier.fromNamespaceAndPath(modId, path), codec);
        }

        @Override
        public void registerLocationBasedEffect(String path, MapCodec<? extends EnchantmentLocationBasedEffect> codec) {
            Registry.register(BuiltInRegistries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, Identifier.fromNamespaceAndPath(modId, path), codec);
        }
    }

    public record MapDecorationTypes(String modId) implements UnifiedRegistries.MapDecorationTypes {

        @Override
        public Holder<MapDecorationType> register(String path, boolean showOnItemFrame, int mapColor, boolean explorationMapElement, boolean trackCount) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            return Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, ResourceKey.create(Registries.MAP_DECORATION_TYPE, id), new MapDecorationType(id, showOnItemFrame, mapColor, explorationMapElement, trackCount));
        }
    }
}