package net.rebel459.unified.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.api.builder.StonePreset;
import net.rebel459.unified.api.builder.StoneSet;
import net.rebel459.unified.api.core.*;
import net.rebel459.unified.api.codec.ExtensibleBlockCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.data.registry.BlockGenerator;
import net.rebel459.unified.api.data.registry.ItemGenerator;
import net.rebel459.unified.api.registry.VanillaBlockCodecs;
import net.rebel459.unified.api.registry.VanillaItemCodecs;
import net.rebel459.unified.api.asset.BlockAssets;
import net.rebel459.unified.api.asset.ItemAssets;
import net.rebel459.unified.fabric.FabricUnifiedInitializer;

public final class UnifiedTestMod implements ModInitializer {
    public static final String MOD_ID = "unified_testmod";

    public static final UnifiedData DATA = UnifiedData.create(MOD_ID).autoName().build();
    public static final BlockGenerator BLOCKS = DATA.registries().blocks();
    public static final ItemGenerator ITEMS = DATA.registries().items();
    public static final UnifiedRegistries.SoundEvents SOUNDS = UnifiedRegistries.SoundEvents.create(MOD_ID);
    public static final Supplied<SoundEvent> TEST_SOUND = SOUNDS.register("test_sound");
    public static final ExtensibleBlockCodec.Complex<SoundEvent> SOUND_BLOCK_TYPE = ExtensibleCodecs.BLOCK.register(
            id("sound_block"),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound"),
            sound -> properties -> new Block(properties.sound(new SoundType(1, 1, sound, sound, sound, sound, sound)))
    );

    public static final TagKey<Block> TEST_BLOCKS = TagKey.create(Registries.BLOCK, id("test_blocks"));
    public static final TagKey<Item> TEST_ITEMS = TagKey.create(Registries.ITEM, id("test_items"));

    public static final StoneSet TEST_SET =  new StoneSet.RegistryBuilder(id("test_set"), MapColor.COLOR_GRAY, StonePreset.STONE, BLOCKS, null).build();

    public static final SuppliedBlock TEST_BLOCK = BLOCKS.register(
            "test_block",
            VanillaBlockCodecs.BLOCK.create(),
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
            VanillaItemCodecs.ITEM.create(),
            item -> item
                    .properties(properties -> properties
                            .stacksTo(16)
                            .rarity(Rarity.UNCOMMON))
                    .assets(assets -> assets
                            .name("Test Item")
                            .model(ItemAssets.GENERATED))
                    .data(data -> data.tag(TEST_ITEMS))
    );

    public static final SuppliedBlock TEST_STAIRS = BLOCKS.register(
            "test_stairs",
            VanillaBlockCodecs.STAIRS.create(TEST_BLOCK::get),
            block -> block.properties(properties -> properties.copyFrom(TEST_BLOCK))
    );

    public static final SuppliedBlock TEST_SOUND_BLOCK = BLOCKS.register(
            "test_sound_block",
            SOUND_BLOCK_TYPE.create(TEST_SOUND::get),
            block -> block.properties(properties -> properties.strength(2.0F))
    );

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    static void initRegistryResources() {
        // Forces custom extensible-codec types and their staged declarations to exist before JSON indexing completes.
    }

    @Override
    public void onInitialize() {
        FabricUnifiedInitializer.register(this::onInitializeCommon);
    }

    private void onInitializeCommon() {
        TEST_BLOCK.get();
        TEST_ITEM.get();
        TEST_STAIRS.get();
        TEST_SOUND_BLOCK.get();
    }
}
