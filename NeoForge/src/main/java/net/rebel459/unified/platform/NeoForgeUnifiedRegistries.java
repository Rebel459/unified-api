package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class NeoForgeUnifiedRegistries {

    public static final Map<String, DeferredRegister.Items> ITEMS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.Blocks> BLOCKS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<CreativeModeTab>> CREATIVE_TABS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.DataComponents> DATA_COMPONENTS = new ConcurrentHashMap<>();

    public static void init() {
        RegistryFactory.set(new RegistryFactory.Factory() {
            @Override
            public UnifiedRegistries.ItemRegistry createItemRegistry(String modId) {
                NeoForgeUnifiedRegistries.ITEMS.putIfAbsent(modId, DeferredRegister.createItems(modId));
                return new NeoForgeUnifiedRegistries.ItemRegistry(modId);
            }

            @Override
            public UnifiedRegistries.BlockRegistry createBlockRegistry(String modId) {
                NeoForgeUnifiedRegistries.BLOCKS.putIfAbsent(modId, DeferredRegister.createBlocks(modId));
                return new NeoForgeUnifiedRegistries.BlockRegistry(modId);
            }

            @Override
            public UnifiedRegistries.FuelRegistry createFuelRegistry() {
                return new NeoForgeUnifiedRegistries.FuelRegistry();
            }

            @Override
            public UnifiedRegistries.CreativeRegistry createCreativeRegistry() {
                return new NeoForgeUnifiedRegistries.CreativeRegistry();
            }

            @Override
            public UnifiedRegistries.ComponentRegistry createComponentRegistry(String modId) {
                return new NeoForgeUnifiedRegistries.ComponentRegistry(modId);
            }
        });
    }

    public static void registerBus(String modId, IEventBus modEventBus) {
        DeferredRegister.Items items = ITEMS.computeIfAbsent(modId, string -> DeferredRegister.createItems(modId));
        DeferredRegister.Blocks blocks = BLOCKS.computeIfAbsent(modId, string -> DeferredRegister.createBlocks(modId));
        DeferredRegister<CreativeModeTab> creativeTabs = CREATIVE_TABS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId));
        DeferredRegister.DataComponents components = DATA_COMPONENTS.computeIfAbsent(modId, string -> DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modId));

        items.register(modEventBus);
        blocks.register(modEventBus);
        creativeTabs.register(modEventBus);
        components.register(modEventBus);
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return ITEMS.get(modId).registerItem(name, function, properties);
        }

        @Override
        public Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> block, Supplier<Item.Properties> properties) {
            return ITEMS.get(modId).registerSimpleBlockItem(name, block, properties);
        }
    }

    public record BlockRegistry(String modId) implements UnifiedRegistries.BlockRegistry {

        @Override
        public <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            Supplier<T> blockHolder = registerWithoutItem(name, function, blockProperties);
            ITEMS.get(modId).registerSimpleBlockItem(name, blockHolder);
            return blockHolder;
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            return BLOCKS.get(modId).registerBlock(name, function, () -> properties);
        }
    }

    public static class FuelRegistry implements UnifiedRegistries.FuelRegistry {

        private static final Object2IntMap<ItemLike> ITEMS = new Object2IntLinkedOpenHashMap<>();

        static {
            NeoForge.EVENT_BUS.register(FuelRegistry.class);
        }

        @Override
        public void add(ItemLike item, int ticks) {
            ITEMS.put(item, ticks);
        }

        @Override
        public void add(TagKey<Item> tag, int ticks) {
            List<Holder<Item>> list = VanillaRegistries.createLookup().lookupOrThrow(Registries.ITEM).get(tag).map(HolderSet.Named::stream).orElse(Stream.empty()).toList();
            list.forEach(itemHolder -> ITEMS.put(itemHolder.value(), ticks));
        }

        @SubscribeEvent
        public static void event(FurnaceFuelBurnTimeEvent event) {
            if (event.getItemStack().isEmpty()) return;
            int time = ITEMS.getOrDefault(event.getItemStack().getItem(), Integer.MIN_VALUE);
            if (time != Integer.MIN_VALUE) {
                event.setBurnTime(time);
            }
        }
    }

    public static class CreativeRegistry implements UnifiedRegistries.CreativeRegistry {

        private static List<Pair<ItemStack, ResourceKey<CreativeModeTab>>> ADD_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> ADD_AFTER_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> ADD_BEFORE_ITEMS = new ArrayList<>();

        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                add(tab, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            List<ItemStack> itemList = Arrays.stream(items).toList();
            for (ItemStack item : itemList) {
                ADD_ITEMS.add(Pair.of(item, tab));
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
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                ADD_AFTER_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
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
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                ADD_BEFORE_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
        }

        @Override
        public ResourceKey<CreativeModeTab> registerTab(Identifier id, Supplier<? extends ItemLike> icon) {
            CREATIVE_TABS.get(id.getNamespace()).register(id.getPath(), () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
                    .icon(() -> new ItemStack(icon.get()))
                    .displayItems((params, output) -> {
                    })
                    .build());
            return ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), id);
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            for (Pair<ItemStack, ResourceKey<CreativeModeTab>> pair : ADD_ITEMS) {
                ItemStack item = pair.getFirst();
                ResourceKey<CreativeModeTab> tab = pair.getSecond();
                if (event.getTabKey().equals(tab)) {
                    event.accept(item);
                }
            }
            for (int x = ADD_AFTER_ITEMS.size() - 1; x >= 0; x--) {
                var triple = ADD_AFTER_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertAfter(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
            for (int x = ADD_BEFORE_ITEMS.size() - 1; x >= 0; x--) {
                var triple = ADD_BEFORE_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertBefore(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }

    public record ComponentRegistry(String modId) implements UnifiedRegistries.ComponentRegistry {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return DATA_COMPONENTS.get(modId).registerComponentType(string, unaryOperator);
        }
    }
}