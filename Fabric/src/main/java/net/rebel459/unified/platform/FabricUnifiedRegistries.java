package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.StrippableBlockRegistryImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.mixin.block.FuelValuesBuilderAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

            @Override
            public UnifiedRegistries.FuelRegistry createFuelRegistry() {
                return null;
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

    public static class FuelRegistry implements UnifiedRegistries.FuelRegistry {

        private static final List<FuelRegistryEvents.BuildCallback> CALLBACKS = new ArrayList<>();
        private static final List<FuelRegistryEvents.ExclusionsCallback> EXCLUSIONS_CALLBACKS = new ArrayList<>();

        @Override
        public void add(ItemLike item, int ticks) {
            CALLBACKS.add((builder, context) -> {
                if (ticks >= 0) {
                    builder.add(item, ticks);
                }
            });
            EXCLUSIONS_CALLBACKS.add((builder, context) -> {
                if (ticks < 0) {
                    ((FuelValuesBuilderAccessor) builder).getValues().remove(item.asItem());
                }
            });
        }

        @Override
        public void add(TagKey<Item> tag, int ticks) {
            CALLBACKS.add((builder, context) -> {
                if (ticks >= 0) {
                    builder.add(tag, ticks);
                }
            });
            EXCLUSIONS_CALLBACKS.add((builder, context) -> {
                if (ticks < 0) {
                    ((FuelValuesBuilderAccessor) builder).getValues().remove(tag);
                }
            });
        }

        static {
            FuelRegistryEvents.BUILD.register((builder, context) -> {
                for (var callback : CALLBACKS) {
                    callback.build(builder, context);
                }
            });
            FuelRegistryEvents.EXCLUSIONS.register((builder, context) -> {
                for (var callback : EXCLUSIONS_CALLBACKS) {
                    callback.buildExclusions(builder, context);
                }
            });
        }
    }
}