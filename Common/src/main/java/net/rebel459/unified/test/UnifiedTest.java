package net.rebel459.unified.test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.rebel459.unified.platform.UnifiedRegistries;

public class UnifiedTest {

    public static UnifiedRegistries.ItemRegistry REGISTRY = UnifiedRegistries.ItemRegistry.create("unified");

    public static void init() {

    }

    public static final Item TEST_ITEM = REGISTRY.register(
            "test_item",
            Item::new,
            new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
    );
}
