package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricUnifiedRegistries {

    public static void init() {
        RegistryFactory.set(new RegistryFactory.Factory() {
            @Override
            public UnifiedRegistries.ItemRegistry createItemRegistry(String modId) {
                return new FabricUnifiedRegistries.ItemRegistry(modId);
            }

            @Override
            public UnifiedRegistries.BlockRegistry createBlockRegistry(String modId) {
                return new FabricUnifiedRegistries.BlockRegistry(modId);
            }
        });
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
            var resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
            return () -> Items.registerItem(resourceKey, function, properties.setId(resourceKey));
        }

        @Override
        public Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> blockSupplier, Item.Properties properties) {
            Block block = blockSupplier.get();
            return () -> (BlockItem) Items.registerBlock(block, properties);
        }
    }

    public record BlockRegistry(String modId) implements UnifiedRegistries.BlockRegistry {

        @Override
        public <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            UnifiedRegistries.ItemRegistry.create(modId).registerBlockItem(name, () -> function.apply(blockProperties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name)))), new Item.Properties());
            return registerWithoutItem(name, function, blockProperties);
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            if (BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) return () -> Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
            else throw new IllegalArgumentException("Block with id " + id + " is already in the block registry.");
        }
    }
}