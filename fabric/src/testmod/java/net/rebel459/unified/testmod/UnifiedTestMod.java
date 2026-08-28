package net.rebel459.unified.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.platform.UnifiedDataRegistries;
import net.rebel459.unified.registry.VanillaBlockTypes;
import net.rebel459.unified.registry.VanillaItemTypes;
import net.rebel459.unified.util.builder.BlockPreset;
import net.rebel459.unified.util.builder.BlockSet;
import net.rebel459.unified.util.datagen.BlockAssets;
import net.rebel459.unified.util.datagen.ItemAssets;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;

public final class UnifiedTestMod implements ModInitializer {
    public static final String MOD_ID = "unified_testmod";

    public static final UnifiedDataRegistries DATA = UnifiedDataRegistries.create(MOD_ID).autoName().build();
    public static final UnifiedDataRegistries.Blocks BLOCKS = DATA.blocks();
    public static final UnifiedDataRegistries.Items ITEMS = DATA.items();

    public static final TagKey<Block> TEST_BLOCKS = TagKey.create(Registries.BLOCK, id("test_blocks"));
    public static final TagKey<Item> TEST_ITEMS = TagKey.create(Registries.ITEM, id("test_items"));

    public static final BlockSet TEST_SET =  new BlockSet.RegistryBuilder(id("test_set"), MapColor.COLOR_GRAY, BlockPreset.STONE, BLOCKS, null).build();

    public static final SuppliedBlock TEST_BLOCK = BLOCKS.register(
            "test_block",
            VanillaBlockTypes.BLOCK.create(),
            block -> block
                    .properties(properties -> properties
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(2.0F)
                            .requiresCorrectToolForDrops(true)
                    )
                    .assets(assets -> assets
                            .name("Test Block")
                            .model(BlockAssets.SIMPLE_CUBE)
                    )
                    .data(data -> data
                            .dropSelf()
                            .tag(TEST_BLOCKS)
                    )
    );

    public static final SuppliedItem TEST_ITEM = ITEMS.register(
            "test_item",
            VanillaItemTypes.ITEM.create(),
            item -> item
                    .properties(properties -> properties
                            .stacksTo(16)
                            .rarity(Rarity.UNCOMMON))
                    .assets(assets -> assets
                            .name("Test Item")
                            .model(ItemAssets.GENERATED))
                    .data(data -> data.tag(TEST_ITEMS))
    );

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Static initialization registers fixtures before Fabric invokes datagen providers.
    }
}
