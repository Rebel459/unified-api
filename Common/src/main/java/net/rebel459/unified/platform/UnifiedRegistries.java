package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class UnifiedRegistries {

    public interface ItemRegistry {
        String modId();

        Supplier<Item> register(String name, Function<Item.Properties, Item> function, Item.Properties properties);

        Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> blockSupplier, Item.Properties properties);

        static ItemRegistry create(String modId) {
            return RegistryFactory.get().createItemRegistry(modId);
        }
    }

    public interface BlockRegistry {
        String modId();

        <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties);

        <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);

        static BlockRegistry create(String modId) {
            return RegistryFactory.get().createBlockRegistry(modId);
        }
    }

    public interface FuelRegistry {

        void add(int time, ItemLike... items);

        static FuelRegistry create() {
            return RegistryFactory.get().createFuelRegistry();
        }
    }
}