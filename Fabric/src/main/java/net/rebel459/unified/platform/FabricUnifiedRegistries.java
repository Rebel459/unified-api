package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
                return new FabricUnifiedRegistries.FuelRegistry();
            }

            @Override
            public UnifiedRegistries.CreativeRegistry createCreativeRegistry() {
                return new FabricUnifiedRegistries.CreativeRegistry();
            }

            @Override
            public UnifiedRegistries.ComponentRegistry createComponentRegistry(String modId) {
                return new FabricUnifiedRegistries.ComponentRegistry(modId);
            }

            @Override
            public UnifiedRegistries.PackRegistry createPackRegistry(String modId) {
                return new FabricUnifiedRegistries.PackRegistry(modId);
            }
        });
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var resourceKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
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
            UnifiedRegistries.ItemRegistry.create(modId).registerBlockItem(name, () -> function.apply(blockProperties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name)))), Item.Properties::new);
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

    public static class CreativeRegistry implements UnifiedRegistries.CreativeRegistry {

        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                add(tab, itemLike.asItem().getDefaultInstance());
            }
        }

            @SafeVarargs
        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            var itemList = Arrays.stream(items).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                ItemStack item = itemList.get(x);
                ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                    entries.accept(item);
                });
            }
        }

        @Override
        public final void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                addAfter(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                entries.addAfter(existingItem, addedItems);
            });
        }

        @Override
        public final void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                addBefore(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                entries.addBefore(existingItem, addedItems);
            });
        }

        @Override
        public ResourceKey<CreativeModeTab> registerTab(Identifier id, Supplier<? extends ItemLike> icon) {
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

    public record PackRegistry(String modId) implements UnifiedRegistries.PackRegistry {

        @Override
        public void register(String path, PackInfo info) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            if (FabricLoader.getInstance().getModContainer(id.getNamespace()).isEmpty()) return;
            PackActivationType activationType = PackActivationType.DEFAULT_ENABLED;
            if (info.equals(PackInfo.REQUIRED_DATA) || info.equals(PackInfo.REQUIRED_RESOURCES)) activationType = PackActivationType.ALWAYS_ENABLED;
            ResourceLoader.registerBuiltinPack(
                    id, FabricLoader.getInstance().getModContainer(id.getNamespace()).get(),
                    Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                    activationType
            );
        }
    }
}