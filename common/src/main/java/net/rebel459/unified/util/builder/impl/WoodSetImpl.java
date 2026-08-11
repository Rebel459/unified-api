package net.rebel459.unified.util.builder.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.CreativeModeTabs;
import net.rebel459.unified.util.builder.BlockSet;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.builder.WoodSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WoodSetImpl {

    public static Map<Identifier, WoodSet.PrecedingCreativeEntries> CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Supplier<? extends ItemLike>> SAPLING_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, Supplier<? extends ItemLike>> LEAF_CREATIVE_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<WoodSet> woodSets) {
        creativeEntries(woodSets);
        for (WoodSet woodset : woodSets) {
            registerTypes(woodset);
            registerBlockProperties(woodset);
        }
    }
    public static void registerTypes(WoodSet woodSet) {
        WoodType woodtype = WoodType.register(woodSet.getWoodType().get());
        BlockSetType.register(woodtype.setType());
    }

    public static void registerBlockProperties(WoodSet woodSet) {
        UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodSet.getLog(), woodSet.getStrippedLog());

        if (woodSet.hasWood()){
            UnifiedHelpers.BLOCK_CONVERSIONS.addStrippable(woodSet.getWood(), woodSet.getStrippedWood());
        }

        if (woodSets.hasLeaves()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodSets.getLeaves(), 0.3F);
        if (woodSets.hasSapling()) UnifiedHelpers.DATA_COMPONENTS.addCompost(woodSets.getSapling(), 0.3F);

        if (woodSet.getSettings().isFlammable()) {
            addFlammable(woodSet.getLog(), 5, 5);
            addFlammable(woodSet.getStrippedLog(), 5, 5);

            if (woodSet.hasWood()) {
                addFlammable(woodSet.getWood(), 5, 5);
                addFlammable(woodSet.getStrippedWood(), 5, 5);
            }
            if (woodSet.hasMosaic()) {
                addFlammable(woodSet.getMosaic(), 5, 20);
                addFlammable(woodSet.getMosaicStairs(), 5, 20);
                addFlammable(woodSet.getMosaicSlab(), 5, 20);
            }
            if (woodSets.hasLeaves()) {
                addFlammable(woodSets.getLeaves(), 30, 60);
            }

            addFlammable(woodSet.getPlanks(), 5, 20);
            addFlammable(woodSet.getSlab(), 5, 20);
            addFlammable(woodSet.getStairs(), 5, 20);
            addFlammable(woodSet.getFence(), 5, 20);
            addFlammable(woodSet.getFenceGate(), 5, 20);

            addFlammable(woodSet.getSign(), 5, 20);
            addFlammable(woodSet.getWallSign(), 5, 20);

            addFlammable(woodSet.getHangingSign(), 5, 20);
            addFlammable(woodSet.getWallHangingSign(), 5, 20);
            addFlammable(woodSet.getShelf(), 30, 20);

            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getLog(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getStrippedLog(), 300);
            if (woodSet.hasMosaic()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getMosaic(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getMosaicSlab(), 150);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getMosaicStairs(), 300);
            }
            if (woodSet.hasWood()) {
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getWood(), 300);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getStrippedWood(), 300);
            }
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getPressurePlate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getButton(), 100);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getTrapdoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getDoor(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getFence(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getFenceGate(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getSignItem(), 300);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getHangingSignItem(), 800);
            UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getShelf(), 300);

            if (woodSet.hasBoats()){
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getBoatItem(), 1200);
                UnifiedHelpers.DATA_COMPONENTS.addFurnaceFuel(woodSet.getChestBoatItem(), 1200);
            }
        }
    }

    private static void addFlammable(SuppliedBlock block, int burn, int spread){
        ((FireBlock) Blocks.FIRE).setFlammable(block.get(), burn, spread);
    }

    private static void creativeEntries(List<WoodSet> woodSets) {
        for (WoodSet woodSet : woodSets) {
            WoodSet.PrecedingCreativeEntries precedingItems = CREATIVE_ENTRIES.get(woodSet.getId());
            if (precedingItems == null) continue;

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS,
                    precedingItems.building().get(),
                    woodSet.getPlanks(),
                    woodSet.getStairs(),
                    woodSet.getSlab(),
                    woodSet.getFence(),
                    woodSet.getFenceGate(),
                    woodSet.getDoor(),
                    woodSet.getTrapdoor(),
                    woodSet.getPressurePlate(),
                    woodSet.getButton()
            );
            if (woodSet.hasMosaic()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getMosaic(), woodSet.getMosaicStairs(), woodSet.getMosaicSlab());
            if (woodSet.hasWood()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getWood(), woodSet.getStrippedLog(), woodSet.getStrippedWood());
            else UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.BUILDING_BLOCKS, precedingItems.building().get(), woodSet.getLog(), woodSet.getStrippedLog());

            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, precedingItems.natural().get(), woodSet.getLog());

            if (woodSet.hasLeaves()) {
                Supplier<? extends ItemLike> item = LEAF_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, item.get(), woodSet.getLeaves());
            }
            if (woodSet.hasSapling()) {
                Supplier<? extends ItemLike> item = SAPLING_CREATIVE_ENTRIES.get(woodSet.getId());
                if (item != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.NATURAL_BLOCKS, item.get(), woodSet.getSapling());
            }
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, precedingItems.functionalShelf().get(), woodSet.getShelf());
            UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, precedingItems.functionalSign().get(), woodSet.getSignItem(), woodSet.getHangingSignItem());

            if (precedingItems.utilities() != null && woodSet.hasBoats()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.TOOLS_AND_UTILITIES, precedingItems.utilities().get(), woodSet.getBoatItem(), woodSet.getChestBoatItem());
        }
    }
}
