package net.rebel459.unified.test;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public static UnifiedRegistries.ParticleRegistry PARTICLES = UnifiedRegistries.ParticleRegistry.create(Unified.MOD_ID);

    public static UnifiedEvents.CreativeEvent CREATIVE_EVENT = UnifiedEvents.CreativeEvent.create();
    public static UnifiedEvents.PackEvent PACK_EVENT = UnifiedEvents.PackEvent.create();
    public static UnifiedEvents.LootEvent LOOT_EVENT = UnifiedEvents.LootEvent.create();
    public static UnifiedEvents.StrippableEvent STRIPPABLE_EVENT = UnifiedEvents.StrippableEvent.create();

    public static final Supplier<Item> TEST_ITEM = ITEMS.register(
            "test_item",
            Item::new,
            () -> new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .component(UnifiedComponents.FURNACE_FUEL.get(), 100)
    );

    public static final Supplier<RotatedPillarBlock> TEST_BLOCK = BLOCKS.register("test_block",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .strength(1.8F, 8F)
    );

    public static final Supplier<ShelfBlock> TEST_BLOCK_ENTITY = BLOCKS.register("test_block_entity",
            ShelfBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_SHELF),
            BlockEntityType.SHELF
    );

    public static final Supplier<SimpleParticleType> TEST_PARTICLE = PARTICLES.register("test_particle", new SimpleParticleType(false) {});

    public static final ResourceKey<CreativeModeTab> TEST_TAB = CREATIVE_TABS.registerTab("test_tab", UnifiedTest.TEST_ITEM);

    public static void init() {
    }

    public static void afterInit() {
        PACK_EVENT.add(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "test_pack"), PackInfo.REQUIRED_DATA);
        ComposterBlock.COMPOSTABLES.put(UnifiedTest.TEST_ITEM.get(), 0.2F);
        CREATIVE_EVENT.add(TEST_TAB, TEST_ITEM.get(), TEST_BLOCK.get());
        LOOT_EVENT.addPool(
                BuiltInLootTables.SIMPLE_DUNGEON,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.DIAMOND_BLOCK).setWeight(1))
        );
        FireBlock fireBlock = (FireBlock)Blocks.FIRE;
        fireBlock.setFlammable(TEST_BLOCK.get(), 5, 20);
        STRIPPABLE_EVENT.add(TEST_BLOCK.get(), Blocks.OAK_LOG);
    }
}
