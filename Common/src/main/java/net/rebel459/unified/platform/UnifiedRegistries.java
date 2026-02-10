package net.rebel459.unified.platform;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UnifiedRegistries {

    public interface ItemRegistry {
        String modId();

        Item register(String name, Function<Item.Properties, Item> function, Item.Properties properties);

        Item registerBlockItem(Block block, Item.Properties properties);

        static ItemRegistry create(String modId) {
            return RegistryFactory.get().createItemRegistry(modId);
        }
    }

    public interface BlockRegistry {
        String modId();

        <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties);

        <T extends Block> T registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);

        static BlockRegistry create(String modId) {
            return RegistryFactory.get().createBlockRegistry(modId);
        }
    }
}