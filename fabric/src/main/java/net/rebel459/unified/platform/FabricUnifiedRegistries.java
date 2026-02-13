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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
            return Suppliers.memoize(() -> net.minecraft.world.item.Items.registerItem(resourceKey, function, properties.get().setId(resourceKey)));
        }

        @Override
        public <T extends Block> Supplier<BlockItem> registerBlockItem(String name, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            return Suppliers.memoize(() -> (BlockItem) net.minecraft.world.item.Items.registerBlock(blockSupplier.get(), properties.get()));
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        @Override
        public <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            Identifier blockId = Identifier.fromNamespaceAndPath(modId, name);
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);

            Supplier<T> blockSupplier = Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK, blockId, function.apply(blockProperties.setId(blockKey))));

            UnifiedRegistries.Items.create(modId).registerBlockItem(name, blockSupplier, Item.Properties::new);

            return blockSupplier;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type) {
            Supplier<T> block = register(name, function, blockProperties);
            T blockInstance = block.get();
            ((FabricBlockEntityType) type).addSupportedBlock(blockInstance);
            return block;
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            return Suppliers.memoize(() -> Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id)))));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type) {
            Supplier<T> block = registerWithoutItem(name, function, properties);
            T blockInstance = block.get();
            ((FabricBlockEntityType) type).addSupportedBlock(blockInstance);
            return block;
        }
    }

    public record CreativeTabs(String modId) implements UnifiedRegistries.CreativeTabs {

        @Override
        public ResourceKey<CreativeModeTab> registerTab(String path, Supplier<? extends ItemLike> icon) {
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

    public record ItemComponents(String modId) implements UnifiedRegistries.ItemComponents {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return Suppliers.memoize(() -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(modId, string), unaryOperator.apply(DataComponentType.builder()).build()));
        }
    }

    public record Particles(String modId) implements UnifiedRegistries.Particles {

        @Override
        public <T extends ParticleType> Supplier<T> register(String path, ParticleType type) {
            return Suppliers.memoize(() -> (T) Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(modId, path), type));
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
            return Suppliers.memoize(() -> Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey)));
        }
    }
}