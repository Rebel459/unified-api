package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.mixin.block.FuelValuesBuilderAccessor;
import net.rebel459.unified.util.PackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricUnifiedRegistries {

    public static void init() {
        UnifiedFactory.setRegistries(new UnifiedFactory.Registries() {
            @Override
            public UnifiedRegistries.ItemRegistry createItemRegistry(String modId) {
                return new FabricUnifiedRegistries.ItemRegistry(modId);
            }

            @Override
            public UnifiedRegistries.BlockRegistry createBlockRegistry(String modId) {
                return new FabricUnifiedRegistries.BlockRegistry(modId);
            }

            @Override
            public UnifiedRegistries.CreativeRegistry createCreativeRegistry(String modId) {
                return new FabricUnifiedRegistries.CreativeRegistry(modId);
            }

            @Override
            public UnifiedRegistries.ComponentRegistry createComponentRegistry(String modId) {
                return new FabricUnifiedRegistries.ComponentRegistry(modId);
            }
        });
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
            return () -> Items.registerItem(resourceKey, function, properties.get().setId(resourceKey));
        }

        @Override
        public Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> blockSupplier, Supplier<Item.Properties> properties) {
            Block block = blockSupplier.get();
            return () -> (BlockItem) Items.registerBlock(block, properties.get());
        }
    }

    public record BlockRegistry(String modId) implements UnifiedRegistries.BlockRegistry {

        @Override
        public <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            UnifiedRegistries.ItemRegistry.create(modId).registerBlockItem(name, () -> function.apply(blockProperties.setId(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name)))), Item.Properties::new);
            return registerWithoutItem(name, function, blockProperties);
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            if (BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) return () -> Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.setId(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id))));
            else throw new IllegalArgumentException("Block with id " + id + " is already in the block registry.");
        }
    }

    public record CreativeRegistry(String modId) implements UnifiedRegistries.CreativeRegistry {

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

    public record ComponentRegistry(String modId) implements UnifiedRegistries.ComponentRegistry {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return () -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(modId, string), unaryOperator.apply(DataComponentType.builder()).build());
        }
    }
}