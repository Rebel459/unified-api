package net.rebel459.unified.platform;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public Supplier<Item> register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(modId, path));
            var item = Suppliers.memoize(() -> net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey)));
            item.get();
            return item;
        }

        @Override
        public <T extends Block> Supplier<BlockItem> registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            var item = Suppliers.memoize(() -> (BlockItem) net.minecraft.world.item.Items.registerBlock(blockSupplier.get(), properties.get()));
            item.get();
            return item;
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        @Override
        public <T extends Block> Supplier<T> register(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            Identifier blockId = Identifier.fromNamespaceAndPath(modId, path);
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);

            Supplier<T> blockSupplier = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK, blockId, function.apply(blockProperties.setId(blockKey))));

            UnifiedRegistries.Items.create(modId).registerBlockItem(path, blockSupplier, Item.Properties::new);

            blockSupplier.get();
            return blockSupplier;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> register(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type) {
            Supplier<T> block = register(path, function, blockProperties);
            T blockInstance = block.get();
            ((FabricBlockEntityType) type).addSupportedBlock(blockInstance);
            block.get();
            return block;
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            var block = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id)))));
            block.get();
            return block;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type) {
            Supplier<T> block = registerWithoutItem(path, function, properties);
            T blockInstance = block.get();
            ((FabricBlockEntityType) type).addSupportedBlock(blockInstance);
            block.get();
            return block;
        }
    }

    public record CreativeTabs(String modId) implements UnifiedRegistries.CreativeTabs {

        @Override
        public ResourceKey<CreativeModeTab> register(String path, Supplier<? extends ItemLike> icon) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            CreativeModeTab tab = FabricItemGroup.builder()
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
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplier<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            ResourceKey<EntityType<?>> resourceKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path));
            var entity = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey)));
            entity.get();
            return entity;
        }
    }

    public record BlockEntityTypes(String modId) implements UnifiedRegistries.BlockEntityTypes {

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, @NotNull BlockEntityType.BlockEntitySupplier<T> builder, Block... blocks) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            Util.fetchChoiceType(References.BLOCK_ENTITY, id.toString());
            var blockEntity = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, new BlockEntityType<>(builder, Set.of(blocks))));
            blockEntity.get();
            return blockEntity;
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
}