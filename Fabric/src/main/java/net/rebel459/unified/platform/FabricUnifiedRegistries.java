package net.rebel459.unified.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

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
        public Item register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
            var resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
            return Items.registerItem(resourceKey, function, properties.setId(resourceKey));
        }

        @Override
        public Item registerBlockItem(Block block, Item.Properties properties) {
            return Items.registerBlock(block, properties.setId(ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier())));
        }
    }

    public record BlockRegistry(String modId) implements UnifiedRegistries.BlockRegistry {

        @Override
        public <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            UnifiedRegistries.ItemRegistry.create(modId).registerBlockItem(function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name)))), new Item.Properties());
            return registerWithoutItem(name, function, properties);
        }

        @Override
        public <T extends Block> T registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            if (BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) return Registry.register(BuiltInRegistries.BLOCK, id, function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
            else throw new IllegalArgumentException("Block with id " + id + " is already in the block registry.");
        }
    }
}