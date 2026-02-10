package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeUnifiedRegistries {

    public static void init() {
        RegistryFactory.set(new RegistryFactory.Factory() {
            @Override
            public UnifiedRegistries.ItemRegistry createItemRegistry(String modId) {
                return new NeoForgeUnifiedRegistries.ItemRegistry(modId);
            }

            @Override
            public UnifiedRegistries.BlockRegistry createBlockRegistry(String modId) {
                return new NeoForgeUnifiedRegistries.BlockRegistry(modId);
            }
        });
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Item register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
            return DeferredRegister.createItems(modId).registerItem(name, function, (Supplier)(() -> properties.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name))))).asItem();
        }

        @Override
        public Item registerBlockItem(Block block, Item.Properties properties) {
            return DeferredRegister.createItems(modId).registerSimpleBlockItem((Holder)block, (Supplier)(() -> properties.setId(ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier())))).asItem();
        }
    }

    public record BlockRegistry(String modId) implements UnifiedRegistries.BlockRegistry {

        @Override
        public <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            UnifiedRegistries.ItemRegistry.create(modId).registerBlockItem(function.apply(properties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name)))), new Item.Properties());
            return registerWithoutItem(name, function, properties);
        }

        @Override
        public <T extends Block> T registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            return DeferredRegister.createBlocks(modId).registerBlock(path, function).get();
        }
    }
}