package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.util.PackInfo;
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
        UnifiedFactory.setRegistries(new UnifiedFactory.Registries() {
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
            public UnifiedRegistries.CreativeRegistry createCreativeRegistry(String modId) {
                return new NeoForgeUnifiedRegistries.CreativeRegistry(modId);
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
        DeferredRegister<CreativeModeTab> creativeTabs = CREATIVE_TABS.computeIfAbsent(modId, string -> DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, modId));
        DeferredRegister.DataComponents components = DATA_COMPONENTS.computeIfAbsent(modId, string -> DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, modId));

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

    public record CreativeRegistry(String modId) implements UnifiedRegistries.CreativeRegistry {

        @Override
        public ResourceKey<CreativeModeTab> registerTab(String path, Supplier<? extends ItemLike> icon) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            CREATIVE_TABS.get(id.getNamespace()).register(id.getPath(), () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
                    .icon(() -> new ItemStack(icon.get()))
                    .displayItems((params, output) -> {
                    })
                    .build());
            return ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), id);
        }
    }

    public record ComponentRegistry(String modId) implements UnifiedRegistries.ComponentRegistry {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return DATA_COMPONENTS.get(modId).registerComponentType(string, unaryOperator);
        }
    }
}