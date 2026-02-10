package net.rebel459.unified.test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.CompostingRegistry;

import java.util.function.Supplier;

public class UnifiedTest {

    public static UnifiedRegistries.ItemRegistry ITEMS = UnifiedRegistries.ItemRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.BlockRegistry BLOCKS = UnifiedRegistries.BlockRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.FuelRegistry FUELS = UnifiedRegistries.FuelRegistry.create();

    public static void init() {}

    public static final Supplier<Item> TEST_ITEM = ITEMS.register(
            "test_item",
            Item::new,
            new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
    );

    public static final Supplier<Block> TEST_BLOCK = BLOCKS.register("test_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(1.8F, 8F)
    );
}
