package net.rebel459.unified;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.helper.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        UnifiedEvents.LootTables.modify(((table, key, provider) -> {
            if (BuiltInLootTables.SHIPWRECK_MAP == key) {
                table.editPool(item -> item.get() == Items.MAP, LootItem.lootTableItem(Items.NETHER_STAR), true);
            }
        }));
    }

    public static final String MOD_ID = "unified";
}
