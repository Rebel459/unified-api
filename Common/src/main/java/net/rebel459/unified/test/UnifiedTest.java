package net.rebel459.unified.test;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.registry.UnifiedComponents;
import net.rebel459.unified.util.PackInfo;

import java.util.function.Supplier;

public class UnifiedTest {

    public static UnifiedRegistries.ItemRegistry ITEMS = UnifiedRegistries.ItemRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.BlockRegistry BLOCKS = UnifiedRegistries.BlockRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.CreativeRegistry CREATIVE_TABS = UnifiedRegistries.CreativeRegistry.create();
    public static UnifiedRegistries.PackRegistry PACKS = UnifiedRegistries.PackRegistry.create(Unified.MOD_ID);

    public static final Supplier<Item> TEST_ITEM = ITEMS.register(
            "test_item",
            Item::new,
            () -> new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .component(UnifiedComponents.FURNACE_FUEL.get(), 100)
    );

    public static final Supplier<Block> TEST_BLOCK = BLOCKS.register("test_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(1.8F, 8F)
    );

    public static final ResourceKey<CreativeModeTab> TEST_TAB = CREATIVE_TABS.registerTab(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "test_tab"), UnifiedTest.TEST_ITEM);

    public static void init() {
        PACKS.register("test_pack", PackInfo.REQUIRED_DATA);
    }

    public static void afterInit() {
        ComposterBlock.COMPOSTABLES.put(UnifiedTest.TEST_ITEM.get(), 0.2F);
        CREATIVE_TABS.add(TEST_TAB, TEST_ITEM.get(), TEST_BLOCK.get());
    }
}
