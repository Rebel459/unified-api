package net.rebel459.unified;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.LootEntry;
import net.rebel459.unified.util.helper.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        UnifiedEvents.LootTables.modify((table, key, provider) -> {
                    if (key == BuiltInLootTables.SIMPLE_DUNGEON) {
                        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.NETHER_STAR).setWeight(1));
                        table.addPool(pool);
                    }
                    if (key == Blocks.DIAMOND_ORE.getLootTable().get()) {
                        table.editPool(item -> item == Items.DIAMOND, LootEntry.replace(LootItem.lootTableItem(Items.NETHER_STAR).setWeight(50)));
                    }
                    if (key == BuiltInLootTables.ABANDONED_MINESHAFT) {
                        table.editPool(item -> item == Items.IRON_INGOT, LootEntry.insert(LootItem.lootTableItem(Items.NETHER_STAR).setWeight(50)));
                    }
                    if (key == BuiltInLootTables.SHIPWRECK_MAP) {
                        table.editPool(item -> item == Items.MAP, LootEntry.remove());
                    }
                }
        );
    }

    public static final String MOD_ID = "unified";
}