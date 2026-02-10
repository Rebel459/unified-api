package net.rebel459.unified.platform;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeUnifiedRegistries {

    public static final Map<String, DeferredRegister.Items> ITEMS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.Blocks> BLOCKS = new ConcurrentHashMap<>();

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
                return null;
            }
        });
    }

    public static void registerBus(String modId, IEventBus modEventBus) {
        DeferredRegister.Items items = ITEMS.computeIfAbsent(modId, string -> DeferredRegister.createItems(modId));
        DeferredRegister.Blocks blocks = BLOCKS.computeIfAbsent(modId, string -> DeferredRegister.createBlocks(modId));

        items.register(modEventBus);
        blocks.register(modEventBus);
    }

    public record ItemRegistry(String modId) implements UnifiedRegistries.ItemRegistry {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
            return ITEMS.get(modId).registerItem(name, function, () -> properties);
        }

        @Override
        public Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> block, Item.Properties properties) {
            return ITEMS.get(modId).registerSimpleBlockItem(name, block, p -> properties);
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

        public void add(int time, ItemLike... items) {
            for (ItemLike item : items) {
                ITEMS.put(item, time);
            }
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
}