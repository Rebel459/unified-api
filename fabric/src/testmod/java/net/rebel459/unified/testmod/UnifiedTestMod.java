package net.rebel459.unified.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.api.builder.StonePreset;
import net.rebel459.unified.api.builder.StoneSet;
import net.rebel459.unified.api.builder.WoodPreset;
import net.rebel459.unified.api.builder.WoodSet;
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

    public static final TagKey<Block> TEST_BLOCKS = TagKey.create(Registries.BLOCK, id("test_blocks"));
    public static final TagKey<Item> TEST_ITEMS = TagKey.create(Registries.ITEM, id("test_items"));

    public static final WoodSet TEST_SET =  DATA.sets().woodSet("test", WoodPreset.DEFAULT, new BlockItemTagId(TEST_BLOCKS, TEST_ITEMS), MapColor.WOOD, MapColor.COLOR_BROWN)
            .creativeInventoryPlacement(() -> Blocks.OAK_PLANKS, () -> Blocks.OAK_LOG, () -> Blocks.OAK_SHELF, () -> Blocks.OAK_SIGN)
            .build();

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        FabricUnifiedInitializer.register(this::onInitializeCommon);
    }

    private void onInitializeCommon() {}
}
