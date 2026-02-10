package net.rebel459.unified.platform;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeUnifiedRegistries {

    private static final Map<String, DeferredRegister.Items> ITEMS = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister.Blocks> BLOCKS = new ConcurrentHashMap<>();

    public static void init() {
        LogUtils.getLogger().info("NEOFORGE INIT");
        RegistryFactory.set(new RegistryFactory.Factory() {
            @Override
            public UnifiedRegistries.ItemRegistry createItemRegistry(String modId) {
                ITEMS.putIfAbsent(modId, DeferredRegister.createItems(modId));
                return new ItemRegistry(modId);
            }

            @Override
            public UnifiedRegistries.BlockRegistry createBlockRegistry(String modId) {
                BLOCKS.putIfAbsent(modId, DeferredRegister.createBlocks(modId));
                return new BlockRegistry(modId);
            }
        });
    }

    public static void registerBus(String modId, IEventBus modEventBus) {
        LogUtils.getLogger().info("NEOFORGE BUS");

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
}