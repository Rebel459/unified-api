package net.rebel459.unified.test;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.registry.UnifiedComponents;
import net.rebel459.unified.util.PackInfo;

import java.util.function.Supplier;

public class UnifiedTest {

    public static UnifiedRegistries.ItemRegistry ITEMS = UnifiedRegistries.ItemRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.BlockRegistry BLOCKS = UnifiedRegistries.BlockRegistry.create(Unified.MOD_ID);
    public static UnifiedRegistries.CreativeRegistry CREATIVE_TABS = UnifiedRegistries.CreativeRegistry.create(Unified.MOD_ID);
    public static UnifiedEvents.CreativeEvent CREATIVE_INVENTORY = UnifiedEvents.CreativeEvent.create();
    public static UnifiedEvents.PackEvent PACKS = UnifiedEvents.PackEvent.create();
    public static UnifiedEvents.LootEvent LOOT_POOLS = UnifiedEvents.LootEvent.create();

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

    public static final ResourceKey<CreativeModeTab> TEST_TAB = CREATIVE_TABS.registerTab("test_tab", UnifiedTest.TEST_ITEM);

    public static void init() {}

    public static void afterInit() {
        PACKS.add(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "test_pack"), PackInfo.REQUIRED_DATA);
        ComposterBlock.COMPOSTABLES.put(UnifiedTest.TEST_ITEM.get(), 0.2F);
        CREATIVE_INVENTORY.add(TEST_TAB, TEST_ITEM.get(), TEST_BLOCK.get());
        LOOT_POOLS.addPool(
                BuiltInLootTables.SIMPLE_DUNGEON,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.DIAMOND_BLOCK).setWeight(1))
        );
    }
}
